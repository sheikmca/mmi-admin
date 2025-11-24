package sg.ncs.kp.admin.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sg.ncs.kp.admin.enums.AdminMsgEnum;
import sg.ncs.kp.admin.enums.DirectionEnum;
import sg.ncs.kp.admin.mapper.AdServerMapper;
import sg.ncs.kp.admin.po.AdServer;
import sg.ncs.kp.admin.service.KpUserGroupService;
import sg.ncs.kp.common.exception.pojo.ServiceException;
import sg.ncs.kp.uaa.client.util.SessionUtil;
import sg.ncs.kp.uaa.common.dto.UserDTO;
import sg.ncs.kp.uaa.common.dto.UserGroupAssignUserDTO;
import sg.ncs.kp.uaa.server.mapper.LoginLogMapper;
import sg.ncs.kp.uaa.server.po.LoginLog;
import sg.ncs.kp.uaa.server.po.UserGroup;
import sg.ncs.kp.uaa.server.service.UserGroupService;
import sg.ncs.kp.uaa.server.service.UserService;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @className KpUserGroupServiceImpl
 * @author chen xi
 * @version 1.0.0
 * @date 2023-08-21
 */
@Slf4j
@Service
public class KpUserGroupServiceImpl implements KpUserGroupService {
    @Autowired
    private AdServerMapper adServerMapper;
    @Autowired
    private UserGroupService userGroupService;
    @Autowired
    private LoginLogMapper loginLogMapper;
    @Autowired
    private UserService userService;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
    private static final String FULL_PATH_SPLIT_CHARACTER = "\\|";


    @Override
    public void judgeDirection(String direction, String userId){
        UserGroup userGroup = null;
        if(ObjectUtil.isNotEmpty(userId)) {
            userGroup = userGroupService.getByUserId(userId);
        }
        Integer index = DirectionEnum.getIndexByKey(direction);
        String value = DirectionEnum.getValueByKey(direction);
        if(ObjectUtil.isEmpty(index) || ObjectUtil.isEmpty(value)){
            throw new ServiceException(AdminMsgEnum.USER_GROUP_INVALID);
        }
        if(ObjectUtil.isNotEmpty(userGroup)) {
            String fullPath = userGroup.getFullPath();
            if (!fullPath.contains(index + "")) {
                throw new ServiceException(AdminMsgEnum.USER_GROUP_INVALID);
            }
        }
    }

    @Override
    public IPage<UserDTO> getUserListByGroupId(UserGroupAssignUserDTO userGroupAssignUserDTO){
        authJudgment(userGroupAssignUserDTO.getId(),SessionUtil.getUserId());
        if(!userGroupAssignUserDTO.isDisplayOwn()) {
            userGroupAssignUserDTO.setCurrentUserId(SessionUtil.getUserId());
        }
        IPage<UserDTO> page = userGroupService.assignedUserList(userGroupAssignUserDTO);
        List<UserDTO> records = page.getRecords();
        Map<String, Date> map = new HashMap<>();
        if (CollectionUtil.isNotEmpty(records)) {
            List<String> ids = records.stream().map(UserDTO::getId).collect(Collectors.toList());
            Set<String> userIds = new HashSet<>();
            userIds.addAll(records.stream().map(UserDTO::getCreatedId).collect(Collectors.toList()));
            userIds.addAll(records.stream().map(UserDTO::getLastUpdatedId).collect(Collectors.toList()));
            Map<String, String> userNameMap = new HashMap<>();
            userNameMap = userService.getUserNameMap(new ArrayList<>(userIds));
            List<LoginLog> loginLogs = loginLogMapper.getUserLastLogin(ids);
            if (CollectionUtil.isNotEmpty(loginLogs)) {
                for (LoginLog loginLog : loginLogs) {
                    map.put(loginLog.getUserId(), loginLog.getLoginTime());
                }
            }
            for (UserDTO record : records) {
                record.setCreatedBy(userNameMap.get(record.getCreatedId()));
                record.setLastUpdateBy(userNameMap.get(record.getLastUpdatedId()));
                record.setLastLoginTime(map.get(record.getId()));
            }
        }
        return page;
    }

    private void authJudgment(Integer groupId, String userId){
        UserGroup userGroup = userGroupService.getByUserId(userId);
        if(ObjectUtil.isNotEmpty(userGroup)){
            List<String> userGroupIdStr = Arrays.asList(userGroup.getFullPath().split(FULL_PATH_SPLIT_CHARACTER));
            List<Integer> userGroupIds = new ArrayList<>();
            userGroupIds = userGroupIdStr.stream()
                    .map(Integer::parseInt)
                    .collect(Collectors.toList());
            if(userGroupIds.contains(groupId) && !userGroupIds.get(userGroupIds.size()-1).equals(groupId)){
                throw new ServiceException(AdminMsgEnum.USER_GROUP_AUTH_ERROR);
            }
        }
    }
}
