package sg.ncs.kp.admin.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import sg.ncs.kp.uaa.client.session.UserSession;
import sg.ncs.kp.uaa.common.enums.StatusEnum;
import sg.ncs.kp.uaa.common.enums.UserLevelEnum;
import sg.ncs.kp.uaa.server.mapper.RoleMapper;
import sg.ncs.kp.uaa.server.po.Permission;
import sg.ncs.kp.uaa.server.po.Role;
import sg.ncs.kp.uaa.server.po.User;
import sg.ncs.kp.uaa.server.po.UserGroup;
import sg.ncs.kp.uaa.server.service.ApikeySessionService;
import sg.ncs.kp.uaa.server.service.PermissionService;
import sg.ncs.kp.uaa.server.service.UserGroupService;
import sg.ncs.kp.uaa.server.service.UserService;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * description:
 *
 * @author Wang Shujin
 * @date 2023/5/30 10:15
 */
@Service
@ConditionalOnProperty(prefix = "kp.uaa.resourceserver.apikey", name = "enabled", havingValue = "true")
public class ApikeySessionServiceImpl implements ApikeySessionService {

    @Autowired
    private UserService userService;

    @Autowired
    private RoleMapper roleMapper;

    @Autowired
    private UserGroupService userGroupService;

    @Autowired
    private PermissionService permissionService;


    @Override
    public Object loadByUserId(String userId) {
        User user = userService.getUser(userId);
        if (user == null) {
            return null;
        }
        UserSession userSession = userService.getUserSession(user);
        List<Role> roles = roleMapper.getRoles(user.getId(), StatusEnum.ACTIVE.getStatus());
        if (roles != null) {
            Set<Integer> roleIds = roles.stream().map(Role::getId).collect(Collectors.toSet());
            userSession.setRoleId(roleIds);
        }
        //session.setRoleId();
        UserGroup userGroup = userGroupService.getByUserId(user.getId());
        if (userGroup != null) {
            HashSet<Integer> set = new HashSet<>();
            set.add(userGroup.getId());
            userSession.setUserGroupId(set);
        }
        List<Permission> permissions = permissionService.getPermissions(user.getId(), UserLevelEnum.byValue(user.getLevel()));
        if (permissions != null) {
            userSession.setAuthorities(permissions.stream().map(Permission::getAuthorityKey).collect(Collectors.toList()));
        }
        return userSession;
    }
}
