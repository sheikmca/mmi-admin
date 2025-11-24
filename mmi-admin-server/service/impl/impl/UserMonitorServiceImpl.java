package sg.ncs.kp.admin.service.impl;
/**
 * @className UserMonitorServiceImpl
 * @author chen xi
 * @version 1.0.0
 * @date 2023-07-25
 */

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sg.ncs.kp.admin.dto.*;
import sg.ncs.kp.admin.mapper.UserMonitorMapper;
import sg.ncs.kp.admin.pojo.AdminConstants;
import sg.ncs.kp.admin.pojo.WSMsgTypEnum;
import sg.ncs.kp.admin.service.KpUserOnlineService;
import sg.ncs.kp.admin.service.UserMonitorService;
import sg.ncs.kp.uaa.client.util.SessionUtil;
import sg.ncs.kp.uaa.common.dto.*;
import sg.ncs.kp.uaa.common.enums.UserLevelEnum;
import sg.ncs.kp.uaa.server.mapper.LoginLogMapper;
import sg.ncs.kp.uaa.server.mapper.RoleMapper;
import sg.ncs.kp.uaa.server.po.LoginLog;
import sg.ncs.kp.uaa.server.po.Role;
import sg.ncs.kp.uaa.server.po.User;
import sg.ncs.kp.uaa.server.service.UserService;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class UserMonitorServiceImpl implements UserMonitorService {

    @Autowired
    private UserMonitorMapper userMonitorMapper;
    @Autowired
    private RoleMapper roleMapper;
    @Autowired
    private LoginLogMapper loginLogMapper;
    @Autowired
    private UserService userService;
    @Autowired
    private KpUserOnlineService kpUserOnlineService;

    @Override
    public IPage<UserMonitorDTO> selectUserMonitorList(UserMonitorQueryDTO userMonitorQueryDTO) {
        UserLevelEnum userLevel = SessionUtil.getUserLevel();
        if (UserLevelEnum.SUPER_ADMIN.equals(userLevel)) {
            userMonitorQueryDTO.setUserLevel(UserLevelEnum.TENANT_ADMIN.getValue());
        } else {
            userMonitorQueryDTO.setTenantId(SessionUtil.getTenantId());
            userMonitorQueryDTO.setUserLevel(UserLevelEnum.USER.getValue());
        }
        Page<User> page = new Page<>(userMonitorQueryDTO.getPageNo(),userMonitorQueryDTO.getPageSize());
        IPage<UserMonitorDTO> userDTOIPage = userMonitorMapper.userMonitorList(page, userMonitorQueryDTO);
        List<UserMonitorDTO> records = userDTOIPage.getRecords();
        if(CollectionUtil.isNotEmpty(records)){
            List<String> userIds = records.stream().map(UserMonitorDTO::getId).collect(Collectors.toList());
            List<RoleUserBasicDTO> roles = roleMapper.getRolesByUserIds(userIds);
            for (UserMonitorDTO record : records) {
                List<IdNameDTO> roleList = new ArrayList<>();
                for (RoleUserBasicDTO role : roles) {
                    if(StringUtils.equals(record.getId(),role.getUserId())){
                        IdNameDTO dto = new IdNameDTO();
                        dto.setId(role.getId());
                        dto.setName(role.getName());
                        roleList.add(dto);
                    }
                }
                record.setRole(roleList);
            }
        }
        for (UserMonitorDTO record : records) {
            List<Role> roles = roleMapper.getRoles(record.getId(), null);
            List<IdNameDTO> roleList = BeanUtil.copyToList(roles, IdNameDTO.class);
            if(ObjectUtil.isEmpty(record.getLastLogoutTime()) && ObjectUtil.isNotEmpty(record.getLastLoginTime())){
                record.setOnlineStatus(true);
            }else{
                record.setOnlineStatus(false);
            }
            if(ObjectUtil.isNotEmpty(record.getLastLoginTime())){
                record.setLastLogin(record.getLastLoginTime().getTime());
            }
            if(ObjectUtil.isNotEmpty(record.getLastLogoutTime())){
                record.setLastLogout(record.getLastLogoutTime().getTime());
            }
            record.setRole(roleList);
        }
        return userDTOIPage;
    }

    @Override
    public UserMonitorCountDTO getUserCount(){
        UserMonitorCountDTO count = new UserMonitorCountDTO();
        String tenantId = SessionUtil.getTenantId();
        count.setTotalUser(userService.countUsers(tenantId));
        count.setOnlineUser(loginLogMapper.getOnlineUserCount(UserLevelEnum.USER.getValue(), tenantId));
        count.setOfflineUser(count.getTotalUser()-count.getOnlineUser());
        return count;
    }

    @Override
    public List<HourOnlineCountDTO> countByTime(Date time){
        Date startTime = DateUtil.beginOfSecond(time);
        Long dayMis=1000*60*60*24-1+time.getTime();
        Date endTime = DateUtil.date(dayMis);
        return loginLogMapper.getHourOnlineUserCount(startTime, endTime);
    }
}
