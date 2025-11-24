package sg.ncs.kp.admin.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import sg.ncs.kp.admin.BaseTest;
import sg.ncs.kp.admin.dto.AssignUserDTO;
import sg.ncs.kp.admin.dto.RemoveUserFromGroupDTO;
import sg.ncs.kp.admin.po.RecipientGroup;
import sg.ncs.kp.admin.service.RecipientGroupService;
import sg.ncs.kp.common.i18n.util.MessageUtils;
import sg.ncs.kp.uaa.client.session.UserSession;
import sg.ncs.kp.uaa.client.util.SessionUtil;
import sg.ncs.kp.uaa.common.dto.UserGroupAssignUserDTO;

/**
 * @author Wang Shujin
 * @date 2022/8/28 14:31
 */
public class RecipientGroupControllerTest extends BaseTest {

    @Mock
    private RecipientGroupService recipientGroupService;

    @Mock
    private MessageUtils messageUtils;

    @InjectMocks
    private RecipientGroupController recipientGroupController;

    @Test
    void addTest() {
        try (MockedStatic<SessionUtil> mockedStatic = Mockito.mockStatic(SessionUtil.class)) {
            mockedStatic.when(SessionUtil::getUserSession).thenReturn(new UserSession());
            Mockito.when(recipientGroupService.insert(Mockito.any())).thenReturn(new RecipientGroup());
            Assertions.assertDoesNotThrow(() -> recipientGroupController.insertRecipientGroup(new RecipientGroup()));
        }
    }

    @Test
    void updateTest() {
        try (MockedStatic<SessionUtil> mockedStatic = Mockito.mockStatic(SessionUtil.class)) {
            mockedStatic.when(SessionUtil::getUserSession).thenReturn(new UserSession());
            Assertions.assertDoesNotThrow(() -> recipientGroupController.updateRecipientGroup(new RecipientGroup()));
        }
    }

    @Test
    void deleteRecipientGroupTest() {
        Assertions.assertDoesNotThrow(() -> recipientGroupController.deleteRecipientGroup(1));
    }

    @Test
    void getRecipientGroupTest() {
        try (MockedStatic<SessionUtil> mockedStatic = Mockito.mockStatic(SessionUtil.class)) {
            mockedStatic.when(SessionUtil::getUserSession).thenReturn(new UserSession());
            Mockito.when(recipientGroupService.selectAll(Mockito.anyString(), Mockito.anyString())).thenReturn(null);
            Assertions.assertDoesNotThrow(() -> recipientGroupController.getRecipientGroup(""));
        }
    }

    @Test
    void getAssignedUserListTest() {
        try (MockedStatic<SessionUtil> mockedStatic = Mockito.mockStatic(SessionUtil.class)) {
            mockedStatic.when(SessionUtil::getUserSession).thenReturn(new UserSession());
            IPage mock = Mockito.mock(IPage.class);
            Mockito.when(recipientGroupService.assignedUserList(Mockito.any(), Mockito.any())).thenReturn(mock);
            Assertions.assertDoesNotThrow(() -> recipientGroupController.getAssignedUserList(1,new UserGroupAssignUserDTO()));
        }
    }

    @Test
    void getNotAssignedUserListTest() {
        try (MockedStatic<SessionUtil> mockedStatic = Mockito.mockStatic(SessionUtil.class)) {
            mockedStatic.when(SessionUtil::getUserSession).thenReturn(new UserSession());
            IPage mock = Mockito.mock(IPage.class);
            Mockito.when(recipientGroupService.groupNotAssignedUserList(Mockito.any(), Mockito.any())).thenReturn(mock);
            Assertions.assertDoesNotThrow(() -> recipientGroupController.getNotAssignedUserList(1,new UserGroupAssignUserDTO()));
        }
    }

    @Test
    void assignUserTest() {
        Assertions.assertDoesNotThrow(() -> recipientGroupController.assignUser(1,new AssignUserDTO()));
    }

    @Test
    void removeUserTest() {
        Assertions.assertDoesNotThrow(() -> recipientGroupController.removeUser(1, new RemoveUserFromGroupDTO()));
    }

}
