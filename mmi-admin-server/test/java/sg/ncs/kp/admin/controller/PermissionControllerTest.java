package sg.ncs.kp.admin.controller;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import sg.ncs.kp.common.core.response.Result;
import sg.ncs.kp.common.i18n.util.MessageUtils;
import sg.ncs.kp.uaa.client.util.SessionUtil;
import sg.ncs.kp.uaa.common.dto.PermissionDTO;
import sg.ncs.kp.uaa.common.enums.UserLevelEnum;
import sg.ncs.kp.uaa.server.po.Permission;
import sg.ncs.kp.uaa.server.service.PermissionService;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @auther IVAN
 * @date 2022/9/7
 * @description
 */
class PermissionControllerTest {

    @InjectMocks
    private PermissionController permissionController;

    @Mock
    private PermissionService permissionService;

    @Mock
    private MessageUtils messageUtils;

    private AutoCloseable autoCloseable;

    @BeforeEach
    void setUp() {
        autoCloseable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void release() throws Exception {
        autoCloseable.close();
    }

    private Result result = new Result();

    /**
     * Method under test: {@link PermissionController#permission()}
     */
    @Test
    void permission() {
        try (MockedStatic<SessionUtil> sessionUtilMockedStatic = Mockito.mockStatic(SessionUtil.class)) {
            String userId = "user id";
            UserLevelEnum userLevel = UserLevelEnum.USER;
            sessionUtilMockedStatic.when(SessionUtil::getUserId).thenReturn(userId);
            sessionUtilMockedStatic.when(SessionUtil::getUserLevel).thenReturn(userLevel);
            List<PermissionDTO> permissions = new ArrayList<>();
            Mockito.when(permissionService.tree(userId,userLevel)).thenReturn(permissions);
            Mockito.when(messageUtils.succeed(permissions)).thenReturn(result);
            assertEquals(result,permissionController.permission());
        }

    }

    /**
     * Method under test: {@link PermissionController#permissionList(Integer)}
     */
    @Test
    void permissionList() {
        Integer roleId = 1;
        List<Permission> permissions = new ArrayList<>();
        Mockito.when(permissionService.getPermissionsByRole(roleId)).thenReturn(permissions);
        Mockito.when(messageUtils.succeed(permissions)).thenReturn(result);
        assertEquals(result,permissionController.permissionList(roleId));

    }
}