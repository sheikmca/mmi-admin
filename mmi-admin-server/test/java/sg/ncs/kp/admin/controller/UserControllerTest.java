package sg.ncs.kp.admin.controller;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.web.multipart.MultipartFile;
import sg.ncs.kp.admin.dto.*;
import sg.ncs.kp.admin.po.UploadRecord;
import sg.ncs.kp.admin.service.KpUserService;
import sg.ncs.kp.admin.service.impl.KpUserServiceImpl;
import sg.ncs.kp.admin.util.NotificationUtil;
import sg.ncs.kp.common.core.response.Result;
import sg.ncs.kp.common.i18n.util.MessageUtils;
import sg.ncs.kp.uaa.client.util.SessionUtil;
import sg.ncs.kp.uaa.common.dto.PasswordPolicyDTO;
import sg.ncs.kp.uaa.common.dto.PermissionDTO;
import sg.ncs.kp.uaa.common.dto.RoleUserBasicDTO;
import sg.ncs.kp.uaa.common.dto.UserDTO;
import sg.ncs.kp.uaa.common.dto.UserDetailDTO;
import sg.ncs.kp.uaa.common.dto.UserModifyDTO;
import sg.ncs.kp.uaa.common.dto.UserPasswordDTO;
import sg.ncs.kp.uaa.common.dto.UserQueryDTO;
import sg.ncs.kp.uaa.common.dto.UserUploadRecordQueryDTO;
import sg.ncs.kp.uaa.common.vo.UserGroupTreeVO;
import sg.ncs.kp.uaa.server.mapper.RoleMapper;
import sg.ncs.kp.uaa.server.po.PasswordPolicy;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

/**
 * @auther IVAN
 * @date 2022/9/7
 * @description
 */
class UserControllerTest {

    @InjectMocks
    private UserController userController;

    @Mock
    private KpUserService kpUserService;

    @Mock
    private MessageUtils messageUtils;

    private AutoCloseable autoCloseable;
    
    @Mock
    private RoleMapper roleMapper;
    
    @Mock
    NotificationUtil notificationUtil;

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
     * Method under test: {@link UserController#list(UserQueryDTO)}
     */
    @Test
    void list() {
        UserQueryDTO userQueryDTO = new UserQueryDTO();
        Page<UserDTO> page = new Page<>(1, 10);
        when(kpUserService.selectList(userQueryDTO)).thenReturn(page);
        when(messageUtils.pageResult((int) page.getCurrent(), (int) page.getSize(),
                page.getTotal(), BeanUtil.copyToList(page.getRecords(), UserDTO.class))).thenReturn(null);
        assertDoesNotThrow(()->userController.list(userQueryDTO));
    }

    /**
     * Method under test: {@link UserController#add(UserModifyDTO)}
     */
    @Test
    void add() {
        UserModifyDTO userModifyDTO = new UserModifyDTO();
        doNothing().when(kpUserService).add(userModifyDTO);
        when(messageUtils.addSucceed(null)).thenReturn(result);
        assertEquals(result,userController.add(userModifyDTO));
    }

    /**
     * Method under test: {@link UserController#add(UserModifyDTO)}
     */
    @Test
    void update() {
        List<RoleUserBasicDTO> roles = new ArrayList<>();
        RoleUserBasicDTO role = new RoleUserBasicDTO();
        role.setId(1);
        roles.add(role);
        when(roleMapper.getRolesByUserIds(Mockito.any())).thenReturn(roles);
        UserModifyDTO userModifyDTO = new UserModifyDTO();
        userModifyDTO.setId("1");
        Set<Integer> roleIds = new HashSet<>();
        roleIds.add(1);
        roleIds.add(2);
        userModifyDTO.setRoleIds(roleIds);
        doNothing().when(kpUserService).update(userModifyDTO);
        when(messageUtils.updateSucceed(null)).thenReturn(result);
        assertEquals(result,userController.update(userModifyDTO));
    }

    /**
     * Method under test: {@link UserController#delete(String)}
     */
    @Test
    void delete() {
        String id = "id";
        doNothing().when(kpUserService).delete(id);
        when(messageUtils.deleteSucceed(null)).thenReturn(result);
        assertEquals(result,userController.delete(id));
    }

    /**
     * Method under test: {@link UserController#delete(List)}
     */
    @Test
    void testDelete() {
        String id = "id";
        List<String> ids = Arrays.asList(id);
        doNothing().when(kpUserService).delete(ids);
        when(messageUtils.deleteSucceed(null)).thenReturn(result);
        assertEquals(result,userController.delete(ids));
    }

    /**
     * Method under test: {@link UserController#updateStatus(String, Integer)}
     */
    @Test
    void updateStatus() {
        String id = "id";
        Integer status = 1;
        doNothing().when(kpUserService).updateStatus(id,status);
        when(messageUtils.updateSucceed(null)).thenReturn(result);
        assertEquals(result,userController.updateStatus(id,status));
    }

    /**
     * Method under test: {@link UserController#group(String)}
     */
    @Test
    void group() {
        String groupName = "group name";
        UserGroupTreeVO userGroupTreeVO = new UserGroupTreeVO();
        when(kpUserService.group(groupName)).thenReturn(userGroupTreeVO);
        when(messageUtils.succeed(userGroupTreeVO)).thenReturn(result);
        assertEquals(result,userController.group(groupName));
    }

    /**
     * Method under test: {@link UserController#changeGroup(ChangeGroupDTO)}
     */
    @Test
    void changeGroup() {
        ChangeGroupDTO changeGroupDTO = new ChangeGroupDTO();
        String id = "id";
        List<String> userIds = new ArrayList<>();
        userIds.add(id);
        changeGroupDTO.setUserIds(userIds);
        Integer userGroupId = 1;
        changeGroupDTO.setUserGroupId(userGroupId);
        doNothing().when(kpUserService).changeGroup(changeGroupDTO);
        when(messageUtils.updateSucceed(null)).thenReturn(result);
        assertEquals(result,userController.changeGroup(changeGroupDTO));
    }

    /**
     * Method under test: {@link UserController#password(UserPasswordDTO)}
     */
    @Test
    void password() {
        UserPasswordDTO userPasswordDTO = new UserPasswordDTO();
        doNothing().when(kpUserService).password(userPasswordDTO);
        when(messageUtils.updateSucceed(null)).thenReturn(result);
        assertEquals(result,userController.password(userPasswordDTO));
    }

    /**
     * Method under test: {@link UserController#policy()}
     */
    @Test
    void policy() {
        List<PasswordPolicy> passwordPolicies = new ArrayList<>();
        when(kpUserService.policy()).thenReturn(passwordPolicies);
        when(messageUtils.succeed(passwordPolicies)).thenReturn(result);
        assertEquals(result,userController.policy());
    }

    /**
     * Method under test: {@link UserController#policy(List)}
     */
    @Test
    void testPolicy() {
        List<PasswordPolicyDTO> list = new ArrayList<>();
        doNothing().when(kpUserService).policy(list);
        when(messageUtils.updateSucceed(null)).thenReturn(result);
        assertEquals(result,userController.policy(list));
    }

    /**
     * Method under test: {@link UserController#resetPassword(ResetPasswordDTO)}
     */
    @Test
    void resetPassword() {
        ResetPasswordDTO resetPasswordDTO = new ResetPasswordDTO();
        resetPasswordDTO.setId("1");
        List<PasswordPolicyDTO> list = new ArrayList<>();
        doNothing().when(kpUserService).resetPassword(resetPasswordDTO);
        when(messageUtils.updateSucceed(null)).thenReturn(result);
        assertEquals(result,userController.resetPassword(resetPasswordDTO));
    }

    /**
     * Method under test: {@link UserController#myself()}
     */
    @Test
    void myself() {
        UserDetailDTO userDetailDTO;
        try (MockedStatic<SessionUtil> sessionUtilMockedStatic = mockStatic(SessionUtil.class)) {
            userDetailDTO = new UserDetailDTO();
            String userId = "user id";
            sessionUtilMockedStatic.when(SessionUtil::getUserId).thenReturn(userId);
            when(kpUserService.get(userId)).thenReturn(userDetailDTO);
            when(messageUtils.succeed(userDetailDTO)).thenReturn(result);
            assertEquals(result,userController.myself());
        }

    }

    /**
     * Method under test: {@link UserController#updateMyself(UserUpdateProfileDTO)}
     */
    @Test
    void updateMyself() {
        UserUpdateProfileDTO user = new UserUpdateProfileDTO();
        doNothing().when(kpUserService).updateMyself(user);
        when(messageUtils.updateSucceed()).thenReturn(result);
        assertEquals(result,userController.updateMyself(user));
    }

    /**
     * Method under test: {@link UserController#upload(MultipartFile)}
     */
    @Test
    void upload() {
        String path = "11.xlsx";
        MultipartFile file = mock(MultipartFile.class);
        when(kpUserService.upload(file)).thenReturn(path);
        UserUploadPathDTO userUploadPathDTO = new UserUploadPathDTO();
        userUploadPathDTO.setPath(path);
        result.setData(userUploadPathDTO);
        when(messageUtils.succeed(any(UserUploadPathDTO.class))).thenReturn(result);
        assertEquals(path,userController.upload(file).getData().getPath());
    }

    /**
     * Method under test: {@link UserController#view(UserViewQueryDTO)}
     */
    @Test
    void view() {
        UserViewQueryDTO userViewQueryDTO = new UserViewQueryDTO();
        Page<UserUploadViewDTO> page = new Page<>(1, 10);
        when(kpUserService.view(userViewQueryDTO)).thenReturn(page);
        when(messageUtils.pageResult((int) page.getCurrent(), (int) page.getSize(),
                page.getTotal(), BeanUtil.copyToList(page.getRecords(), UserDTO.class))).thenReturn(null);
        assertDoesNotThrow(()->userController.view(userViewQueryDTO));
    }

    /**
     * Method under test: {@link UserController#importFile(UserViewQueryDTO)}
     */
    @Test
    void importFile() {
        UserViewQueryDTO userViewQueryDTO = new UserViewQueryDTO();
        UploadResultDTO uploadResultDTO = new UploadResultDTO();
        when(kpUserService.importFile(userViewQueryDTO.getPath())).thenReturn(uploadResultDTO);
        when(messageUtils.succeed(uploadResultDTO)).thenReturn(result);
        assertEquals(result,userController.importFile(userViewQueryDTO));
    }

    /**
     * Method under test: {@link UserController#template(HttpServletResponse)}
     */
    @Test
    void template() throws IOException {
        /*try (MockedConstruction<XSSFWorkbook> xssfWorkbookMockedConstruction = mockConstruction(XSSFWorkbook.class)) {
            XSSFWorkbook workbook = new XSSFWorkbook();
            doNothing().when(workbook).write(any());
            HttpServletResponse response = mock(HttpServletResponse.class);
            assertDoesNotThrow(()->userController.template(response));
        }*/
    }

    /**
     * Method under test: {@link UserController#uploadRecord(UserUploadRecordQueryDTO)}
     */
    @Test
    void uploadRecord() {
        UserUploadRecordQueryDTO userUploadRecordQueryDTO = new UserUploadRecordQueryDTO();
        Page<UploadRecord> page = new Page<>(1, 10);
        when(kpUserService.uploadRecord(userUploadRecordQueryDTO)).thenReturn(page);
        when(messageUtils.pageResult((int) page.getCurrent(), (int) page.getSize(),
                page.getTotal(), BeanUtil.copyToList(page.getRecords(), UserDTO.class))).thenReturn(null);
        assertDoesNotThrow(()->userController.uploadRecord(userUploadRecordQueryDTO));
    }

    /**
     * Method under test: {@link UserController#downloadUploadUserFile(HttpServletResponse, UserViewQueryDTO)}
     */
    @Test
    void downloadUploadUserFile() {
        HttpServletResponse response = mock(HttpServletResponse.class);
        UserViewQueryDTO userViewQueryDTO = new UserViewQueryDTO();
        doNothing().when(kpUserService).downloadUploadFile(response,userViewQueryDTO.getPath(),"user_batch_add.xlsx");
        assertDoesNotThrow(()->userController.downloadUploadUserFile(response,userViewQueryDTO));
    }

    /**
     * Method under test: {@link UserController#export(List, HttpServletResponse)}
     */
    @Test
    void export() {
        List<String> ids = new ArrayList<>();
        HttpServletResponse response = mock(HttpServletResponse.class);
        doNothing().when(kpUserService).export(ids,response);
        assertDoesNotThrow(()->userController.export(ids,response));
    }

    /**
     * Method under test: {@link UserController#updateMyselfPassword(UserSelfPasswordUpdateDTO)}
     */
    @Test
    void updateMyselfPassword() {
        try (MockedStatic<SessionUtil> sessionUtilMockedStatic = mockStatic(SessionUtil.class)) {
            sessionUtilMockedStatic.when(SessionUtil::getRoles).thenReturn(null);
            UserSelfPasswordUpdateDTO userPasswordDTO = new UserSelfPasswordUpdateDTO();
            doNothing().when(kpUserService).updateMyselfPassword(userPasswordDTO);
            when(messageUtils.updateSucceed(null)).thenReturn(result);
            assertEquals(result,userController.updateMyselfPassword(userPasswordDTO));
        }
    }

    /**
     * Method under test: {@link UserController#notification(String, Integer)}
     */
    @Test
    void notification() {
        String id = "user id";
        Integer status = 1;
        doNothing().when(kpUserService).notification(id,status);
        assertDoesNotThrow(() -> userController.notification(id, status));
    }

    /**
     * Method under test: {@link UserController#menus()}
     */
    @Test
    void menus() {
        List<MenuDTO> list = new ArrayList<>();
        when(kpUserService.menus()).thenReturn(list);
        Result result = mock(Result.class);
        when(messageUtils.succeed(list)).thenReturn(result);
        assertEquals(result,userController.menus());
    }

}