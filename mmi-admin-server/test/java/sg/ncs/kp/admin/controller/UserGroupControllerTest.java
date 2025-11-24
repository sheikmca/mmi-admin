package sg.ncs.kp.admin.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import sg.ncs.kp.admin.service.KpUserGroupService;
import sg.ncs.kp.common.i18n.util.MessageUtils;
import sg.ncs.kp.uaa.client.session.UserSession;
import sg.ncs.kp.uaa.client.util.SessionUtil;
import sg.ncs.kp.uaa.common.dto.UserDTO;
import sg.ncs.kp.uaa.common.dto.UserGroupAssignUserDTO;
import sg.ncs.kp.uaa.common.dto.UserGroupDotAssignUserDTO;
import sg.ncs.kp.uaa.server.po.UserGroup;
import sg.ncs.kp.uaa.server.service.UserGroupService;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

class UserGroupControllerTest {

    @InjectMocks
    UserGroupController userGroupController;
    @Mock
    UserGroupService userGroupService;
    @Mock
    KpUserGroupService kpUserGroupService;

    @Mock
    MessageUtils messageUtils;

    private AutoCloseable autoCloseable;

    @BeforeEach
    void setUp() {
        autoCloseable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void release() throws Exception {
        autoCloseable.close();
    }


    @Test
    void insertUserGroup() {
        try (MockedStatic<SessionUtil> sessionUtilMockedStatic = mockStatic(SessionUtil.class)) {
            UserSession userSession = new UserSession();
            userSession.setTenantId("tenant id");
            userSession.setId("test");
            sessionUtilMockedStatic.when(SessionUtil::getUserSession).thenReturn(userSession);
            UserGroup userGroup = new UserGroup();
            Assertions.assertDoesNotThrow(() -> userGroupController.insertUserGroup(userGroup));
        }
    }

    @Test
    void updateUserGroup() {
        try (MockedStatic<SessionUtil> sessionUtilMockedStatic = mockStatic(SessionUtil.class)) {
            UserSession userSession = new UserSession();
            userSession.setTenantId("tenant id");
            userSession.setId("test");
            sessionUtilMockedStatic.when(SessionUtil::getUserSession).thenReturn(userSession);
            UserGroup userGroup = new UserGroup();
            Assertions.assertDoesNotThrow(() -> userGroupController.updateUserGroup(userGroup));
        }
    }

    @Test
    void deleteUserGroup() {
        Assertions.assertDoesNotThrow(() -> userGroupController.deleteUserGroup(anyInt()));
    }

    @Test
    void getUserGroupTree() {
        try (MockedStatic<SessionUtil> sessionUtilMockedStatic = mockStatic(SessionUtil.class)) {
            UserSession userSession = new UserSession();
            userSession.setTenantId("tenant id");
            userSession.setId("test");
            sessionUtilMockedStatic.when(SessionUtil::getUserSession).thenReturn(userSession);
            Assertions.assertDoesNotThrow(() -> userGroupController.getUserGroupTree("test"));
        }
    }

    @Test
    void getAssignedUserList() {
        try (MockedStatic<SessionUtil> sessionUtilMockedStatic = mockStatic(SessionUtil.class)) {
            UserSession userSession = new UserSession();
            userSession.setTenantId("tenant id");
            userSession.setId("test");
            sessionUtilMockedStatic.when(SessionUtil::getUserSession).thenReturn(userSession);
            UserGroupAssignUserDTO userGroupAssignUserDTO = new UserGroupAssignUserDTO();
            Page<UserDTO> page = new Page<>(1, 10);
            when(kpUserGroupService.getUserListByGroupId(any())).thenReturn(page);
            Assertions.assertDoesNotThrow(() -> userGroupController.getAssignedUserList(userGroupAssignUserDTO));
        }
    }

    @Test
    void getNotAssignedUserList() {
        try (MockedStatic<SessionUtil> sessionUtilMockedStatic = mockStatic(SessionUtil.class)) {
            UserSession userSession = new UserSession();
            userSession.setTenantId("tenant id");
            userSession.setId("test");
            sessionUtilMockedStatic.when(SessionUtil::getUserSession).thenReturn(userSession);
            UserGroupDotAssignUserDTO userGroupAssignUserDTO = new UserGroupDotAssignUserDTO();
            Page<UserDTO> page = new Page<>(1, 10);
            when(userGroupService.dotAssignedUserList(any())).thenReturn(page);
            Assertions.assertDoesNotThrow(() -> userGroupController.getNotAssignedUserList(userGroupAssignUserDTO));
        }
    }

    @Test
    void assignUser() {
        Assertions.assertDoesNotThrow(() -> userGroupController.assignUser(any()));
    }

    @Test
    void removeUser() {
        Assertions.assertDoesNotThrow(() -> userGroupController.removeUser(1,"1"));
    }

    @Test
    void getUserGroupList() {
        try (MockedStatic<SessionUtil> sessionUtilMockedStatic = mockStatic(SessionUtil.class)) {
            UserSession userSession = new UserSession();
            userSession.setTenantId("tenant id");
            userSession.setId("test");
            sessionUtilMockedStatic.when(SessionUtil::getUserSession).thenReturn(userSession);
            Assertions.assertDoesNotThrow(() -> userGroupController.getUserGroupList("1"));
        }
    }
}