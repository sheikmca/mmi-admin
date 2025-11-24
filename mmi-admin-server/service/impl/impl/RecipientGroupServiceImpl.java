package sg.ncs.kp.admin.service.impl;

import cn.hutool.core.util.ArrayUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sg.ncs.kp.admin.dto.AssignUserDTO;
import sg.ncs.kp.admin.dto.RemoveUserFromGroupDTO;
import sg.ncs.kp.admin.mapper.RecipientGroupMapper;
import sg.ncs.kp.admin.mapper.UserRecipientGroupMappingMapper;
import sg.ncs.kp.admin.po.RecipientGroup;
import sg.ncs.kp.admin.po.UserRecipientGroupMapping;
import sg.ncs.kp.admin.pojo.AdminConstants;
import sg.ncs.kp.admin.service.RecipientGroupService;
import sg.ncs.kp.common.exception.pojo.ClientServiceException;
import sg.ncs.kp.uaa.client.session.UserSession;
import sg.ncs.kp.uaa.client.util.SessionUtil;
import sg.ncs.kp.uaa.common.dto.UserDTO;
import sg.ncs.kp.uaa.common.dto.UserGroupAssignUserDTO;
import sg.ncs.kp.uaa.server.common.UaaServerMsgEnum;
import sg.ncs.kp.uaa.server.po.User;

import java.util.*;
import java.util.stream.Collectors;

/**
 * description: TODO
 *
 * @author Wang Shujin
 * @date 2022/8/24 16:14
 */
@Service
public class RecipientGroupServiceImpl implements RecipientGroupService {

    @Autowired
    private RecipientGroupMapper recipientGroupMapper;

    @Autowired
    private UserRecipientGroupMappingMapper userRecipientGroupMappingMapper;


    @Autowired
    private StringRedisTemplate redisTemplate;

    
    @Override
    public RecipientGroup insert(RecipientGroup recipientGroup) {
        RecipientGroup old = recipientGroupMapper.selectOne(Wrappers
                .<RecipientGroup>lambdaQuery()
                .eq(RecipientGroup::getTenantId, recipientGroup.getTenantId())
                .eq(RecipientGroup::getName, recipientGroup.getName()));
        if (old != null) {
            throw new ClientServiceException(UaaServerMsgEnum.RECIPIENT_GROUP_NAME_EXIST);
        }

        recipientGroupMapper.insert(recipientGroup);

        return recipientGroup;
    }

    @Override
    public void update(RecipientGroup recipientGroup) {
        RecipientGroup old = recipientGroupMapper.selectById(recipientGroup.getId());
        if (old == null || ObjectUtils.notEqual(old.getTenantId(), SessionUtil.getTenantId())) {
            throw new ClientServiceException(UaaServerMsgEnum.RECIPIENT_GROUP_NOT_EXIST);
        }
        if (!StringUtils.equals(old.getName(), recipientGroup.getName())) {
            Long count = recipientGroupMapper.selectCount(Wrappers
                    .<RecipientGroup>lambdaQuery()
                    .eq(RecipientGroup::getTenantId, old.getTenantId())
                    .eq(RecipientGroup::getName, recipientGroup.getName()));
            if (count != null && count > 0) {
                throw new ClientServiceException(UaaServerMsgEnum.RECIPIENT_GROUP_NAME_EXIST);
            }
        }
        old.setName(recipientGroup.getName());
        old.setLastUpdatedId(recipientGroup.getLastUpdatedId());
        recipientGroupMapper.updateById(old);
    }

    @Override
    @Transactional
    public void delete(Integer id) {
        recipientGroupMapper.deleteById(id);
        userRecipientGroupMappingMapper.delete(Wrappers.<UserRecipientGroupMapping>lambdaQuery().eq(UserRecipientGroupMapping::getRecipientGroupId, id));
        redisTemplate.opsForHash().delete(AdminConstants.RECIPIENT_REDIS_CACHE_KEY, id.toString());
    }

    @Override
    public List<RecipientGroup> selectAll(String tenantId, String name) {
        return recipientGroupMapper.selectList(Wrappers
                .<RecipientGroup>lambdaQuery()
                .eq(RecipientGroup::getTenantId, tenantId)
                .like(StringUtils.isNotBlank(name), RecipientGroup::getName, name));
    }

    @Override
    public IPage<UserDTO> assignedUserList(Integer id, UserGroupAssignUserDTO dto) {
        Page<User> page = new Page<>(dto.getPageNo(), dto.getPageSize());
        return recipientGroupMapper.assignedUserList(page, id, dto);
    }

    @Override
    public IPage<UserDTO> groupNotAssignedUserList(Integer id, UserGroupAssignUserDTO dto) {
        Page<User> page = new Page<>(dto.getPageNo(), dto.getPageSize());
        return recipientGroupMapper.notAssignedUserList(page, id, dto);
    }

    @Override
    @Transactional
    public void assignUserToRecipientGroup(AssignUserDTO dto) {
        if (dto.getGroupId() == null || dto.getUserIds() == null || dto.getUserIds().size() <= 0) {
            return;
        }
        RecipientGroup recipientGroup = recipientGroupMapper.selectById(dto.getGroupId());
        if (recipientGroup == null || ObjectUtils.notEqual(recipientGroup.getTenantId(), SessionUtil.getTenantId())) {
            throw new ClientServiceException(UaaServerMsgEnum.RECIPIENT_GROUP_NOT_EXIST);
        }

        for (String userId : dto.getUserIds()) {
            Long count = userRecipientGroupMappingMapper.selectCount(Wrappers
                    .<UserRecipientGroupMapping>lambdaQuery()
                    .eq(UserRecipientGroupMapping::getUserId, userId)
                    .eq(UserRecipientGroupMapping::getRecipientGroupId, dto.getGroupId()));
            if (count <= 0) {
                userRecipientGroupMappingMapper.insert(new UserRecipientGroupMapping(userId, dto.getGroupId()));
            }
        }
        redisTemplate.opsForHash().delete(AdminConstants.RECIPIENT_REDIS_CACHE_KEY, dto.getGroupId().toString());
    }

    @Override
    @Transactional
    public void removeUser(RemoveUserFromGroupDTO dto) {
        if (dto.getGroupId() == null || dto.getUserIds() == null || dto.getUserIds().size() <= 0) {
            return;
        }
        for (String userId : dto.getUserIds()) {
            userRecipientGroupMappingMapper.delete(Wrappers
                    .<UserRecipientGroupMapping>lambdaQuery()
                    .eq(UserRecipientGroupMapping::getUserId, userId)
                    .eq(UserRecipientGroupMapping::getRecipientGroupId, dto.getGroupId()));
        }
        redisTemplate.opsForHash().delete(AdminConstants.RECIPIENT_REDIS_CACHE_KEY, dto.getGroupId().toString());
    }

    @Override
    public List<Integer> ownedRecipientGroup() {
        UserSession userSession = SessionUtil.getUserSession();
        List<UserRecipientGroupMapping> userRecipientGroupMappings = userRecipientGroupMappingMapper.selectList(Wrappers.<UserRecipientGroupMapping>lambdaQuery().eq(UserRecipientGroupMapping::getUserId, userSession.getId()));
        if (userRecipientGroupMappings == null) {
            userRecipientGroupMappings = new ArrayList<>();
        }
        return userRecipientGroupMappings.stream().map(UserRecipientGroupMapping::getRecipientGroupId).collect(Collectors.toList());
    }



    @Override
    public Set<String> hasUsers(Collection<Integer> recipientGroupIds) {
        Set<String> res = new HashSet<>();
        if (ArrayUtil.isEmpty(recipientGroupIds)) {
            return res;
        }
        HashOperations<String, String, String> hashOperations = redisTemplate.opsForHash();
        for (Integer groupId : recipientGroupIds) {
            List<String> userIds = null;
            if (hashOperations.hasKey(AdminConstants.RECIPIENT_REDIS_CACHE_KEY, groupId.toString())) {
                String o = hashOperations.get(AdminConstants.RECIPIENT_REDIS_CACHE_KEY, groupId.toString());
                userIds = JSON.parseArray(o, String.class);
            }
            if (userIds == null || userIds.isEmpty()) {
                List<UserRecipientGroupMapping> groupMappings = userRecipientGroupMappingMapper
                        .selectList(Wrappers
                                .<UserRecipientGroupMapping>lambdaQuery()
                                .eq(UserRecipientGroupMapping::getRecipientGroupId, groupId));
                userIds = groupMappings.stream().map(UserRecipientGroupMapping::getUserId).distinct().collect(Collectors.toList());
                hashOperations.put(AdminConstants.RECIPIENT_REDIS_CACHE_KEY, groupId.toString(), JSON.toJSONString(userIds));
            }
            res.addAll(userIds);
        }

        return res;
    }
}
