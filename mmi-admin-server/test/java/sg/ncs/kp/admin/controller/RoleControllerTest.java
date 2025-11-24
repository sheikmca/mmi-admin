package sg.ncs.kp.admin.controller;

import java.util.Collections;
import java.util.HashSet;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import sg.ncs.kp.admin.BaseTest;
import sg.ncs.kp.admin.dto.ModifyRoleDTO;
import sg.ncs.kp.admin.service.Control2FAService;
import sg.ncs.kp.admin.util.NotificationUtil;
import sg.ncs.kp.common.i18n.util.MessageUtils;
import sg.ncs.kp.uaa.client.session.UserSession;
import sg.ncs.kp.uaa.client.util.SessionUtil;
import sg.ncs.kp.uaa.common.dto.RoleAddUpdateDTO;
import sg.ncs.kp.uaa.common.dto.RoleQueryDTO;
import sg.ncs.kp.uaa.common.enums.UserLevelEnum;
import sg.ncs.kp.uaa.common.vo.RoleVO;
import sg.ncs.kp.uaa.server.mapper.UserMapper;
import sg.ncs.kp.uaa.server.po.Role;
import sg.ncs.kp.uaa.server.service.RoleService;

class RoleControllerTest extends BaseTest{
    @Mock
    private RoleService roleService;
    
    @Mock
    private MessageUtils messageUtils;
    
    @InjectMocks
    private RoleController roleController;
    
    @Mock
    NotificationUtil notificationUtil;
    @Mock
    private Control2FAService control2FAService;
    
    @Mock 
    UserMapper userMapper;
    
    @Test
    void pageListRolesTest() {
        UserSession userSession = new UserSession();
        userSession.setTenantId("tenantId1");
        userSession.setUserLevel(UserLevelEnum.TENANT_ADMIN);
        userSession.setRoleId(new HashSet<>());
        RoleQueryDTO roleQueryDTO = new RoleQueryDTO();
        
        IPage<RoleVO> roles = new Page<>();
        Mockito.when(roleService.rolePageList(Mockito.any(RoleQueryDTO.class))).thenReturn(roles);
        
        try (MockedStatic<SessionUtil> sessionUtilMock = Mockito.mockStatic(SessionUtil.class)) {
            sessionUtilMock.when(() -> SessionUtil.getUserSession()).thenReturn(userSession);
            Assertions.assertDoesNotThrow(()->roleController.pageListRoles(roleQueryDTO));
        }
    }
    
    @Test
    void listRolesTest() {
        UserSession userSession = new UserSession();
        userSession.setTenantId("tenantId1");
        userSession.setUserLevel(UserLevelEnum.TENANT_ADMIN);
        userSession.setRoleId(new HashSet<>());
        Mockito.when(roleService.getRoles(Mockito.any(),Mockito.any())).thenReturn(Collections.emptyList());
        
        try (MockedStatic<SessionUtil> sessionUtilMock = Mockito.mockStatic(SessionUtil.class)) {
            sessionUtilMock.when(() -> SessionUtil.getUserSession()).thenReturn(userSession);
            Assertions.assertDoesNotThrow(()->roleController.listRoles());
        }
    }
    
    @Test
    void addOrUpdateRoleTest() {
        ModifyRoleDTO roleAddUpdateDTO = new ModifyRoleDTO();
        UserSession userSession = new UserSession();
        userSession.setTenantId("tenantId1");
        userSession.setUserLevel(UserLevelEnum.TENANT_ADMIN);
        userSession.setRoleId(new HashSet<>());
        Role role = new Role();
        Mockito.when(roleService.addOrUpdateRole(Mockito.any())).thenReturn(role);
        
        try (MockedStatic<SessionUtil> sessionUtilMock = Mockito.mockStatic(SessionUtil.class)) {
            sessionUtilMock.when(() -> SessionUtil.getUserSession()).thenReturn(userSession);
            Assertions.assertDoesNotThrow(()->roleController.addOrUpdateRole(roleAddUpdateDTO));
        }
    }
    
    @Test
    void deleteRoleTest() {
        UserSession userSession = new UserSession();
        userSession.setTenantId("tenantId1");
        userSession.setUserLevel(UserLevelEnum.TENANT_ADMIN);
        userSession.setRoleId(new HashSet<>());

        try (MockedStatic<SessionUtil> sessionUtilMock = Mockito.mockStatic(SessionUtil.class)) {
            sessionUtilMock.when(() -> SessionUtil.getUserSession()).thenReturn(userSession);
            Assertions.assertDoesNotThrow(()->roleController.deleteRole(1));
        }
    }
    
}
