package sg.ncs.kp.admin.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.exceptions.DependencyException;
import cn.hutool.poi.excel.ExcelReader;
import cn.hutool.poi.excel.ExcelUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.web.multipart.MultipartFile;
import sg.ncs.kp.admin.dto.*;
import sg.ncs.kp.admin.enums.AdminMsgEnum;
import sg.ncs.kp.admin.po.UploadRecord;
import sg.ncs.kp.admin.service.Control2FAService;
import sg.ncs.kp.admin.service.KpUserOnlineService;
import sg.ncs.kp.admin.service.UploadRecordService;
import sg.ncs.kp.admin.strategy.NationStrategy;
import sg.ncs.kp.common.exception.pojo.ClientServiceException;
import sg.ncs.kp.common.i18n.util.MessageUtils;
import sg.ncs.kp.common.oss.component.OssComponent;
import sg.ncs.kp.common.uti.poi.ReadExcelUtlis;
import sg.ncs.kp.uaa.client.session.UserSession;
import sg.ncs.kp.uaa.client.util.SessionUtil;
import sg.ncs.kp.uaa.common.constant.CommonConstant;
import sg.ncs.kp.uaa.common.dto.IdNameDTO;
import sg.ncs.kp.uaa.common.dto.PasswordPolicyDTO;
import sg.ncs.kp.uaa.common.dto.UserDTO;
import sg.ncs.kp.uaa.common.dto.UserDetailDTO;
import sg.ncs.kp.uaa.common.dto.UserModifyDTO;
import sg.ncs.kp.uaa.common.dto.UserPasswordDTO;
import sg.ncs.kp.uaa.common.dto.UserQueryDTO;
import sg.ncs.kp.uaa.common.dto.UserUploadRecordQueryDTO;
import sg.ncs.kp.uaa.common.enums.StatusEnum;
import sg.ncs.kp.uaa.common.enums.UserLevelEnum;
import sg.ncs.kp.uaa.common.vo.UserGroupTreeVO;
import sg.ncs.kp.uaa.server.common.UaaServerMsgEnum;
import sg.ncs.kp.uaa.server.mapper.LoginLogMapper;
import sg.ncs.kp.uaa.server.mapper.RoleMapper;
import sg.ncs.kp.uaa.server.mapper.UserGroupMapper;
import sg.ncs.kp.uaa.server.po.LoginLog;
import sg.ncs.kp.uaa.server.po.PasswordPolicy;
import sg.ncs.kp.uaa.server.po.Permission;
import sg.ncs.kp.uaa.server.po.Role;
import sg.ncs.kp.uaa.server.po.User;
import sg.ncs.kp.uaa.server.po.UserGroup;
import sg.ncs.kp.uaa.server.po.UserRoleMapping;
import sg.ncs.kp.uaa.server.service.PasswordPolicyService;
import sg.ncs.kp.uaa.server.service.PermissionService;
import sg.ncs.kp.uaa.server.service.RoleService;
import sg.ncs.kp.uaa.server.service.UserGroupService;
import sg.ncs.kp.uaa.server.service.UserRoleMappingService;
import sg.ncs.kp.uaa.server.service.UserService;

import javax.servlet.http.HttpServletResponse;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

/**
 * @auther IVAN
 * @date 2022/9/5
 * @description
 */
class KpUserServiceImplTest {

    @InjectMocks
    private KpUserServiceImpl kpUserService;

    @Mock
    private UserService userService;

    @Mock
    private UserGroupService userGroupService;

    @Mock
    private PasswordPolicyService passwordPolicyService;

    @Mock
    private Map<String, NationStrategy> nationStrategy;

    @Mock
    private PermissionService permissionService;

    @Mock
    private UploadRecordService uploadRecordService;

    @Mock
    private OssComponent ossComponent;

    @Mock
    private RedisTemplate redisTemplate;

    @Mock
    private MessageUtils messageUtils;

    @Mock
    private RoleService roleService;

    @Mock
    private UserGroupMapper userGroupMapper;

    @Mock
    private LoginLogMapper loginLogMapper;

    @Mock
    private UserRoleMappingService userRoleMappingService;

    @Mock
    private RoleMapper roleMapper;

    @Mock
    private Control2FAService control2FAService;

    private AutoCloseable autoCloseable;
    @Mock
    private KpUserOnlineService kpUserOnlineService;

    @BeforeEach
    void setUp() {
        autoCloseable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void release() throws Exception {
        autoCloseable.close();
    }

    /**
     * Method under test: {@link KpUserServiceImpl#selectList(UserQueryDTO)}
     */
    @Test
    void selectList() {
        try (MockedStatic<SessionUtil> sessionUtilMockedStatic = mockStatic(SessionUtil.class)) {
            sessionUtilMockedStatic.when(SessionUtil::getTenantId).thenReturn("tenant id");
            sessionUtilMockedStatic.when(SessionUtil::getUserLevel).thenReturn(UserLevelEnum.SUPER_ADMIN);

            UserQueryDTO userQueryDTO = new UserQueryDTO();
            IPage<UserDTO> page = new Page<>(1, 10);
            List<UserDTO> list = new ArrayList<>();
            UserDTO userDTO = new UserDTO();
            userDTO.setId("user id");
            list.add(userDTO);
            page.setRecords(list);
            when(userService.selectList(userQueryDTO)).thenReturn(page);

            List<LoginLog> loginLogs = new ArrayList<>();
            LoginLog loginLog = new LoginLog();
            loginLog.setUserId("user id");
            loginLog.setLoginTime(new Date());
            loginLogs.add(loginLog);
            when(loginLogMapper.getUserLastLogin(any())).thenReturn(loginLogs);

            assertEquals(page, kpUserService.selectList(userQueryDTO));
        }

    }

    /**
     * when the length of username is up to 50
     * Method under test: {@link KpUserServiceImpl#add(UserModifyDTO)}
     */
    @Test
    void add() {
        try (MockedStatic<SessionUtil> sessionUtilMockedStatic = mockStatic(SessionUtil.class)) {
            sessionUtilMockedStatic.when(SessionUtil::getUserLevel).thenReturn(UserLevelEnum.TENANT_ADMIN);
            UserModifyDTO userModifyDTO = new UserModifyDTO();
            userModifyDTO.setUserName("123456789012345678901234567890123456789012345678901");
            ClientServiceException clientServiceException = assertThrows(ClientServiceException.class, () -> kpUserService.add(userModifyDTO));
            assertEquals(clientServiceException.getParameter()[0],"User Name");
            assertEquals(clientServiceException.getParameter()[1],"50");
        }

    }

    /**
     * when the length of full name is up to 25
     * Method under test: {@link KpUserServiceImpl#add(UserModifyDTO)}
     */
    @Test
    void add1() {
        try (MockedStatic<SessionUtil> sessionUtilMockedStatic = mockStatic(SessionUtil.class)) {
            sessionUtilMockedStatic.when(SessionUtil::getUserLevel).thenReturn(UserLevelEnum.TENANT_ADMIN);
            UserModifyDTO userModifyDTO = new UserModifyDTO();
            userModifyDTO.setName("12345678901234567890123456");
            ClientServiceException clientServiceException = assertThrows(ClientServiceException.class, () -> kpUserService.add(userModifyDTO));
            assertEquals(clientServiceException.getParameter()[0],"Full Name");
            assertEquals(clientServiceException.getParameter()[1],"25");
        }
    }

    /**
     * when the phone is not blank and the country code is blank
     * Method under test: {@link KpUserServiceImpl#add(UserModifyDTO)}
     */
    @Test
    void add2() {
        try (MockedStatic<SessionUtil> sessionUtilMockedStatic = mockStatic(SessionUtil.class)) {
            sessionUtilMockedStatic.when(SessionUtil::getUserLevel).thenReturn(UserLevelEnum.TENANT_ADMIN);
            UserModifyDTO userModifyDTO = new UserModifyDTO();
            userModifyDTO.setPhone("152819191912");
            ClientServiceException clientServiceException = assertThrows(ClientServiceException.class, () -> kpUserService.add(userModifyDTO));
            assertEquals(clientServiceException.getCode(), AdminMsgEnum.COUNTRY_CODE_EMPTY);
        }

    }

    /**
     * when the country code is illegal
     * Method under test: {@link KpUserServiceImpl#add(UserModifyDTO)}
     */
    @Test
    void add3() {
        try (MockedStatic<SessionUtil> sessionUtilMockedStatic = mockStatic(SessionUtil.class)) {
            sessionUtilMockedStatic.when(SessionUtil::getUserLevel).thenReturn(UserLevelEnum.TENANT_ADMIN);
            UserModifyDTO userModifyDTO = new UserModifyDTO();
            userModifyDTO.setPhone("152819191912");
            userModifyDTO.setCountryCode("111");
            ClientServiceException clientServiceException = assertThrows(ClientServiceException.class, () -> kpUserService.add(userModifyDTO));
            assertEquals(clientServiceException.getCode(), AdminMsgEnum.COUNTRY_CODE_INVALID);
        }
    }

    /**
     * when the phone is illegal
     * Method under test: {@link KpUserServiceImpl#add(UserModifyDTO)}
     */
    @Test
    void add4() {
        try (MockedStatic<SessionUtil> sessionUtilMockedStatic = mockStatic(SessionUtil.class)) {
            sessionUtilMockedStatic.when(SessionUtil::getUserLevel).thenReturn(UserLevelEnum.TENANT_ADMIN);
            UserModifyDTO userModifyDTO = new UserModifyDTO();
            userModifyDTO.setPhone("152819191912");
            userModifyDTO.setCountryCode("86");
            NationStrategy nationStrategy = mock(NationStrategy.class);
            when(this.nationStrategy.get(userModifyDTO.getCountryCode())).thenReturn(nationStrategy);
            when(nationStrategy.verifyPhone(userModifyDTO.getPhone())).thenReturn(false);
            ClientServiceException clientServiceException = assertThrows(ClientServiceException.class, () -> kpUserService.add(userModifyDTO));
            assertEquals(clientServiceException.getCode(), AdminMsgEnum.PHONE_INVALID);
        }
    }

    /**
     * when the email is illegal
     * Method under test: {@link KpUserServiceImpl#add(UserModifyDTO)}
     */
    @Test
    void add5() {
        try (MockedStatic<SessionUtil> sessionUtilMockedStatic = mockStatic(SessionUtil.class)) {
            sessionUtilMockedStatic.when(SessionUtil::getUserLevel).thenReturn(UserLevelEnum.TENANT_ADMIN);
            UserModifyDTO userModifyDTO = new UserModifyDTO();
            userModifyDTO.setEmail("111");
            ClientServiceException clientServiceException = assertThrows(ClientServiceException.class, () -> kpUserService.add(userModifyDTO));
            assertEquals(clientServiceException.getCode(), AdminMsgEnum.EMAIL_INVALID);
        }
    }

    /**
     * when the validity period is not completeness
     * Method under test: {@link KpUserServiceImpl#add(UserModifyDTO)}
     */
    @Test
    void add6() {
        try (MockedStatic<SessionUtil> sessionUtilMockedStatic = mockStatic(SessionUtil.class)) {
            sessionUtilMockedStatic.when(SessionUtil::getUserLevel).thenReturn(UserLevelEnum.TENANT_ADMIN);
            UserModifyDTO userModifyDTO = new UserModifyDTO();
            userModifyDTO.setValidityEndTime(new Date());
            ClientServiceException clientServiceException = assertThrows(ClientServiceException.class, () -> kpUserService.add(userModifyDTO));
            assertEquals(clientServiceException.getCode(), AdminMsgEnum.VALIDITY_TIME_LACK);
        }
    }

    /**
     * when the validity start time is greater than the validity end time
     * Method under test: {@link KpUserServiceImpl#add(UserModifyDTO)}
     */
    @Test
    void add7() {
        try (MockedStatic<SessionUtil> sessionUtilMockedStatic = mockStatic(SessionUtil.class)) {
            sessionUtilMockedStatic.when(SessionUtil::getUserLevel).thenReturn(UserLevelEnum.TENANT_ADMIN);
            UserModifyDTO userModifyDTO = new UserModifyDTO();
            userModifyDTO.setValidityEndTime(new Date());
            userModifyDTO.setValidityStartTime(new Date());
            ClientServiceException clientServiceException = assertThrows(ClientServiceException.class, () -> kpUserService.add(userModifyDTO));
            assertEquals(clientServiceException.getCode(), AdminMsgEnum.VALIDITY_TIME_ERROR);
        }
    }

    /**
     * when the ip end is null and ip start is not null
     * Method under test: {@link KpUserServiceImpl#add(UserModifyDTO)}
     */
    @Test
    void add8() {
        try (MockedStatic<SessionUtil> sessionUtilMockedStatic = mockStatic(SessionUtil.class)) {
            sessionUtilMockedStatic.when(SessionUtil::getUserLevel).thenReturn(UserLevelEnum.TENANT_ADMIN);
            UserModifyDTO userModifyDTO = new UserModifyDTO();
            userModifyDTO.setIpStart("11");
            ClientServiceException clientServiceException = assertThrows(ClientServiceException.class, () -> kpUserService.add(userModifyDTO));
            assertEquals(clientServiceException.getCode(), AdminMsgEnum.IP_END_LACK);
        }
    }

    /**
     * when the ip end is not null and ip start is null
     * Method under test: {@link KpUserServiceImpl#add(UserModifyDTO)}
     */
    @Test
    void add9() {
        try (MockedStatic<SessionUtil> sessionUtilMockedStatic = mockStatic(SessionUtil.class)) {
            sessionUtilMockedStatic.when(SessionUtil::getUserLevel).thenReturn(UserLevelEnum.TENANT_ADMIN);
            UserModifyDTO userModifyDTO = new UserModifyDTO();
            userModifyDTO.setIpEnd("11");
            ClientServiceException clientServiceException = assertThrows(ClientServiceException.class, () -> kpUserService.add(userModifyDTO));
            assertEquals(clientServiceException.getCode(), AdminMsgEnum.IP_START_LACK);
        }
    }

    /**
     * when the ip start is greater than ip end
     * Method under test: {@link KpUserServiceImpl#add(UserModifyDTO)}
     */
    @Test
    void add10() {
        try (MockedStatic<SessionUtil> sessionUtilMockedStatic = mockStatic(SessionUtil.class)) {
            sessionUtilMockedStatic.when(SessionUtil::getUserLevel).thenReturn(UserLevelEnum.TENANT_ADMIN);
            UserModifyDTO userModifyDTO = new UserModifyDTO();
            userModifyDTO.setIpStart("192.168.1.6");
            userModifyDTO.setIpEnd("192.168.1.1");
            ClientServiceException clientServiceException = assertThrows(ClientServiceException.class, () -> kpUserService.add(userModifyDTO));
            assertEquals(clientServiceException.getCode(), AdminMsgEnum.IP_START_GT_IP_END);
        }
    }

    /**
     * when the ip is invalid
     * Method under test: {@link KpUserServiceImpl#add(UserModifyDTO)}
     */
    @Test
    void add11() {
        try (MockedStatic<SessionUtil> sessionUtilMockedStatic = mockStatic(SessionUtil.class)) {
            sessionUtilMockedStatic.when(SessionUtil::getUserLevel).thenReturn(UserLevelEnum.TENANT_ADMIN);
            UserModifyDTO userModifyDTO = new UserModifyDTO();
            userModifyDTO.setIpStart("11");
            userModifyDTO.setIpEnd("192.168.1.1");
            ClientServiceException clientServiceException = assertThrows(ClientServiceException.class, () -> kpUserService.add(userModifyDTO));
            assertEquals(clientServiceException.getCode(), AdminMsgEnum.IP_INVALID);
        }
    }

    /**
     * add successfully
     * Method under test: {@link KpUserServiceImpl#add(UserModifyDTO)}
     */
    @Test
    void add12() {
        try (MockedStatic<SessionUtil> sessionUtilMockedStatic = mockStatic(SessionUtil.class)) {
            sessionUtilMockedStatic.when(SessionUtil::getUserId).thenReturn("user id");
            UserSession userSession = new UserSession();
            userSession.setTenantId("tenant id");
            sessionUtilMockedStatic.when(SessionUtil::getUserSession).thenReturn(userSession);
            UserModifyDTO userModifyDTO = new UserModifyDTO();
            when(userService.add(any())).thenReturn(true);
            assertDoesNotThrow(() -> kpUserService.add(userModifyDTO));
        }
    }

    /**
     * Method under test: {@link KpUserServiceImpl#update(UserModifyDTO)}
     */
    @Test
    void update() {
        try (MockedStatic<SessionUtil> sessionUtilMockedStatic = mockStatic(SessionUtil.class)) {
            sessionUtilMockedStatic.when(SessionUtil::getUserId).thenReturn("user id");
            UserSession userSession = new UserSession();
            userSession.setTenantId("tenant id");
            sessionUtilMockedStatic.when(SessionUtil::getUserSession).thenReturn(userSession);
            UserModifyDTO userModifyDTO = new UserModifyDTO();
            when(userService.update(any(UserModifyDTO.class))).thenReturn(true);
            assertDoesNotThrow(() -> kpUserService.update(userModifyDTO));
        }
    }

    /**
     * when the user has children
     * Method under test: {@link KpUserServiceImpl#delete(String)}
     */
    @Test
    void delete() {
        try (MockedStatic<SessionUtil> sessionUtilMockedStatic = mockStatic(SessionUtil.class)) {
            sessionUtilMockedStatic.when(SessionUtil :: getUserLevel).thenReturn(UserLevelEnum.TENANT_ADMIN);
            when(userService.count(any())).thenReturn(1L);
            User user = new User();
            user.setUserName("username");
            when(userService.getById("id")).thenReturn(user);
            ClientServiceException exception = assertThrows(ClientServiceException.class, () -> kpUserService.delete("id"));
            assertEquals(AdminMsgEnum.USER_HAVE_CHILD_USER,exception.getCode());
        }
    }

    /**
     * delete user successfully
     * Method under test: {@link KpUserServiceImpl#delete(String)}
     */
    @Test
    void delete1() {
        try (MockedStatic<SessionUtil> sessionUtilMockedStatic = mockStatic(SessionUtil.class)) {
            sessionUtilMockedStatic.when(SessionUtil :: getUserLevel).thenReturn(UserLevelEnum.TENANT_ADMIN);
            sessionUtilMockedStatic.when(SessionUtil :: getUserId).thenReturn("1");
            when(userService.count(any())).thenReturn(0L);
            when(userService.delete(any())).thenReturn(true);
            assertDoesNotThrow(()->kpUserService.delete("id"));
        }
    }

    /**
     * Method under test: {@link KpUserServiceImpl#delete(List)}
     */
    @Test
    void testDelete() {
        try (MockedStatic<SessionUtil> sessionUtilMockedStatic = mockStatic(SessionUtil.class)) {
            List<String> ids = new ArrayList<>();
            ids.add("id");
            sessionUtilMockedStatic.when(SessionUtil :: getUserLevel).thenReturn(UserLevelEnum.TENANT_ADMIN);
            when(userService.count(any())).thenReturn(0L);
            when(userService.delete(any())).thenReturn(true);
            assertDoesNotThrow(()->kpUserService.delete(ids));
        }
    }

    /**
     * Method under test: {@link KpUserServiceImpl#updateStatus(String, Integer)}
     */
    @Test
    void updateStatus() {
        try (MockedStatic<SessionUtil> sessionUtilMockedStatic = mockStatic(SessionUtil.class)) {
            sessionUtilMockedStatic.when(SessionUtil :: getUserId).thenReturn("user id");
            String id = "id";
            Integer status = StatusEnum.ACTIVE.getStatus();
            when(userService.updateStatus(id,StatusEnum.value(status))).thenReturn(true);
            assertDoesNotThrow(()->kpUserService.updateStatus(id,status));
        }
    }

    /**
     * Method under test: {@link KpUserServiceImpl#group(String)}
     */
    @Test
    void group() {
        try (MockedStatic<SessionUtil> sessionUtilMockedStatic = mockStatic(SessionUtil.class)) {
            UserSession userSession = new UserSession();
            userSession.setTenantId("tenant id");
            sessionUtilMockedStatic.when(SessionUtil :: getUserSession).thenReturn(userSession);
            String groupName = "group name";
            UserGroupTreeVO userGroupTreeVO = new UserGroupTreeVO();
            when(userGroupService.selectAllTree(userSession.getTenantId(),groupName)).thenReturn(userGroupTreeVO);
            assertEquals(userGroupTreeVO,kpUserService.group(groupName));
        }

    }

    /**
     * Method under test: {@link KpUserServiceImpl#changeGroup(ChangeGroupDTO)}
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
        doNothing().when(userService).changeGroup(id,userGroupId);
        assertDoesNotThrow(()->kpUserService.changeGroup(changeGroupDTO));
    }

    /**
     * Method under test: {@link KpUserServiceImpl#password(UserPasswordDTO)}
     */
    @Test
    void password() {
        try (MockedStatic<SessionUtil> sessionUtilMockedStatic = mockStatic(SessionUtil.class)) {
            UserSession userSession = new UserSession();
            userSession.setTenantId("tenant id");
            sessionUtilMockedStatic.when(SessionUtil::getUserSession).thenReturn(userSession);
            UserPasswordDTO userPasswordDTO = new UserPasswordDTO();
    //        doNothing().when(userService).changePassword(userSession.getId(),userPasswordDTO.getNewPassword(),userPasswordDTO.getOldPassword(),null);
            assertDoesNotThrow(()->kpUserService.password(userPasswordDTO));
        }
    }

    /**
     * Method under test: {@link KpUserServiceImpl#policy()}
     */
    @Test
    void policy() {
        try (MockedStatic<SessionUtil> sessionUtilMockedStatic = mockStatic(SessionUtil.class)) {
            UserSession userSession = new UserSession();
            userSession.setTenantId("tenant id");
            sessionUtilMockedStatic.when(SessionUtil::getUserSession).thenReturn(userSession);
            List<PasswordPolicy> list = new ArrayList<>();
            when(passwordPolicyService.get(userSession.getTenantId())).thenReturn(list);
            assertEquals(list,kpUserService.policy());
        }
    }

    /**
     * Method under test: {@link KpUserServiceImpl#policy(List)}
     */
    @Test
    void testPolicy() {
        try (MockedStatic<SessionUtil> sessionUtilMockedStatic = mockStatic(SessionUtil.class)) {
            sessionUtilMockedStatic.when(SessionUtil::getUserId).thenReturn("user id");
            List<PasswordPolicyDTO> list = new ArrayList<>();
            PasswordPolicyDTO passwordPolicyDTO = new PasswordPolicyDTO();
            list.add(passwordPolicyDTO);
            doNothing().when(passwordPolicyService).update(any(List.class));
            assertDoesNotThrow(()->kpUserService.policy(list));
        }
    }

    /**
     * Method under test: {@link KpUserServiceImpl#resetPassword(ResetPasswordDTO)}
     */
    @Test
    void resetPassword() {
        try (MockedStatic<SessionUtil> sessionUtilMockedStatic = mockStatic(SessionUtil.class)) {
            ResetPasswordDTO resetPasswordDTO = new ResetPasswordDTO();
            String userId = "user id";
            resetPasswordDTO.setId(userId);
            UserSession userSession = new UserSession();
            userSession.setTenantId("tenant id");
            sessionUtilMockedStatic.when(SessionUtil::getUserSession).thenReturn(userSession);
        //    doNothing().when(userService).resetPassword(userId,null,userSession.getTenantId());
            doNothing().when(kpUserOnlineService).forceLogout(any());
            assertDoesNotThrow(()->kpUserService.resetPassword(resetPasswordDTO));
        }

    }

    /**
     * Method under test: {@link KpUserServiceImpl#get(String)}
     */
    @Test
    void get() {
        String userId = "user id";
        UserDTO userDTO = new UserDTO();
        userDTO.setLevel(1);
        when(userService.get(userId)).thenReturn(userDTO);
        List<Permission> permissions = new ArrayList<>();
        Permission permission = new Permission();
        permission.setAuthorityKey("health_monitor");
        permissions.add(permission);
        when(permissionService.getPermissions(userId,UserLevelEnum.byValue(userDTO.getLevel()))).thenReturn(permissions);
        UserDetailDTO userDetailDTO = new UserDetailDTO();
        BeanUtils.copyProperties(userDTO,userDetailDTO);
        List<String> list = permissions.stream().map(Permission::getAuthorityKey).collect(Collectors.toList());
        userDetailDTO.setPermissions(list);
        userDetailDTO.setUserLevel(UserLevelEnum.SUPER_ADMIN);
        assertEquals(JSONObject.toJSONString(userDetailDTO),JSONObject.toJSONString(kpUserService.get(userId)));
    }

    /**
     * when the file id empty
     * Method under test: {@link KpUserServiceImpl#upload(MultipartFile)}
     */
    @Test
    void upload() {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(true);
        ClientServiceException exception = assertThrows(ClientServiceException.class, () -> kpUserService.upload(file));
        assertEquals(AdminMsgEnum.FILE_IS_EMPTY,exception.getCode());
    }

    /**
     * when the format of file is unsupportable
     * Method under test: {@link KpUserServiceImpl#upload(MultipartFile)}
     */
    @Test
    void upload1() {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getOriginalFilename()).thenReturn("aaa.1");
        ClientServiceException exception = assertThrows(ClientServiceException.class, () -> kpUserService.upload(file));
        assertEquals(AdminMsgEnum.UN_SUPPORT_FILE_FORMAT,exception.getCode());
    }

    /**
     * when the file doesn't have data
     * Method under test: {@link KpUserServiceImpl#upload(MultipartFile)}
     */
    @Test
    void upload2() throws IOException {
        try (MockedStatic<ExcelUtil> excelUtilMockedStatic = mockStatic(ExcelUtil.class)) {
            MultipartFile file = mock(MultipartFile.class);
            when(file.isEmpty()).thenReturn(false);
            when(file.getOriginalFilename()).thenReturn("aaa.xlsx");
            InputStream inputStream = mock(InputStream.class);
            when(file.getInputStream()).thenReturn(inputStream);
            ExcelReader reader = mock(ExcelReader.class);
            excelUtilMockedStatic.when(() -> ExcelUtil.getReader(inputStream, 0)).thenReturn(reader);
            List<List<Object>> list = new ArrayList<>();
            when(reader.read(1)).thenReturn(list);
            ClientServiceException exception = assertThrows(ClientServiceException.class, () -> kpUserService.upload(file));
            assertEquals(AdminMsgEnum.EXCEL_NO_DATA,exception.getCode());
        }
    }

    /**
     * when the row of file is up to 10000
     * Method under test: {@link KpUserServiceImpl#upload(MultipartFile)}
     */
    @Test
    void upload3() throws IOException {
        try (MockedStatic<ExcelUtil> excelUtilMockedStatic = mockStatic(ExcelUtil.class)) {
            MultipartFile file = mock(MultipartFile.class);
            when(file.isEmpty()).thenReturn(false);
            when(file.getOriginalFilename()).thenReturn("aaa.xlsx");
            InputStream inputStream = mock(InputStream.class);
            when(file.getInputStream()).thenReturn(inputStream);
            ExcelReader reader = mock(ExcelReader.class);
            excelUtilMockedStatic.when(() -> ExcelUtil.getReader(inputStream, 0)).thenReturn(reader);
            List<List<Object>> list = mock(List.class);
            when(reader.read(1)).thenReturn(list);
            when(list.size()).thenReturn(10001);
            ClientServiceException exception = assertThrows(ClientServiceException.class, () -> kpUserService.upload(file));
            assertEquals(AdminMsgEnum.EXCEL_ROW_LIMIT,exception.getCode());
        }
    }

    /**
     * when occur exception
     * Method under test: {@link KpUserServiceImpl#upload(MultipartFile)}
     */
    @Test
    void upload4() throws IOException {
        try (MockedStatic<ExcelUtil> excelUtilMockedStatic = mockStatic(ExcelUtil.class)) {
            MultipartFile file = mock(MultipartFile.class);
            when(file.isEmpty()).thenReturn(false);
            when(file.getOriginalFilename()).thenReturn("aaa.xlsx");
            InputStream inputStream = mock(InputStream.class);
            when(file.getInputStream()).thenThrow(IOException.class);
            ExcelReader reader = mock(ExcelReader.class);
            excelUtilMockedStatic.when(() -> ExcelUtil.getReader(inputStream, 0)).thenReturn(reader);
            ClientServiceException exception = assertThrows(ClientServiceException.class, () -> kpUserService.upload(file));
            assertEquals(AdminMsgEnum.FILE_LOAD_FAIL,exception.getCode());
        }
    }

    /**
     * throw exception when upload
     * Method under test: {@link KpUserServiceImpl#upload(MultipartFile)}
     */
    @Test
    void upload5() throws IOException {
        try (
                MockedStatic<ExcelUtil> excelUtilMockedStatic = mockStatic(ExcelUtil.class);
                MockedStatic<SessionUtil> sessionUtilMockedStatic = mockStatic(SessionUtil.class)
        ) {
            MultipartFile file = mock(MultipartFile.class);
            when(file.isEmpty()).thenReturn(false);
            when(file.getOriginalFilename()).thenReturn("aaa.xlsx");
            InputStream inputStream = mock(InputStream.class);
            when(file.getInputStream()).thenReturn(inputStream);
            ExcelReader reader = mock(ExcelReader.class);
            excelUtilMockedStatic.when(() -> ExcelUtil.getReader(inputStream, 0)).thenReturn(reader);
            List<List<Object>> list = mock(List.class);
            when(reader.read(1)).thenReturn(list);
            when(list.size()).thenReturn(1);
            when(file.getBytes()).thenThrow(IOException.class);
            sessionUtilMockedStatic.when(SessionUtil :: getUserId).thenReturn("user id");
            when(ossComponent.upload(any(),any(),any(),any())).thenReturn("path");
            assertNull(kpUserService.upload(file));
        }
    }

    /**
     * upload successfully
     * Method under test: {@link KpUserServiceImpl#upload(MultipartFile)}
     */
    @Test
    void upload6() throws IOException {
        try (
                MockedStatic<ExcelUtil> excelUtilMockedStatic = mockStatic(ExcelUtil.class);
                MockedStatic<SessionUtil> sessionUtilMockedStatic = mockStatic(SessionUtil.class)
        ) {
            MultipartFile file = mock(MultipartFile.class);
            when(file.isEmpty()).thenReturn(false);
            String fileName = "aaa.xlsx";
            when(file.getOriginalFilename()).thenReturn(fileName);
            InputStream inputStream = mock(InputStream.class);
            when(file.getInputStream()).thenReturn(inputStream);
            ExcelReader reader = mock(ExcelReader.class);
            excelUtilMockedStatic.when(() -> ExcelUtil.getReader(inputStream, 0)).thenReturn(reader);
            List<List<Object>> list = mock(List.class);
            when(reader.read(1)).thenReturn(list);
            when(list.size()).thenReturn(1);
            when(file.getBytes()).thenReturn(new byte[1]);
            sessionUtilMockedStatic.when(SessionUtil :: getUserId).thenReturn("user id");
            String path = "path";
            when(ossComponent.upload(any(),any(),any(),any())).thenReturn(path);
            ValueOperations valueOperations = mock(ValueOperations.class);
            when(redisTemplate.opsForValue()).thenReturn(valueOperations);
            doNothing().when(valueOperations).set(CommonConstant.BATCH_ADD_USER_UPLOAD_KEY + path, fileName, 1, TimeUnit.HOURS);
            assertEquals(path,kpUserService.upload(file));
        }
    }

    /**
     * Method under test: {@link KpUserServiceImpl#uploadRecord(UserUploadRecordQueryDTO)}
     */
    @Test
    void uploadRecord() {
        try (MockedStatic<SessionUtil> sessionUtilMockedStatic = mockStatic(SessionUtil.class)) {
            sessionUtilMockedStatic.when(SessionUtil::getUserId).thenReturn("user id");
            UserUploadRecordQueryDTO userUploadRecordQueryDTO = new UserUploadRecordQueryDTO();
            Page<UploadRecord> page = mock(Page.class);
            when(uploadRecordService.page(any(),any())).thenReturn(page);
            assertEquals(page,kpUserService.uploadRecord(userUploadRecordQueryDTO));
        }
    }

    /**
     * when the user file is error
     * Method under test: {@link KpUserServiceImpl#view(UserViewQueryDTO)}
     */
    @Test
    void view() {
        try (MockedStatic<ExcelUtil> excelUtilMockedStatic = mockStatic(ExcelUtil.class)){
            UserViewQueryDTO userViewQueryDTO = new UserViewQueryDTO();
            ValueOperations valueOperations = mock(ValueOperations.class);
            when(redisTemplate.opsForValue()).thenReturn(valueOperations);
            when(valueOperations.get(userViewQueryDTO.getPath())).thenReturn(null);
            InputStream inputStream = mock(InputStream.class);
            when(ossComponent.downloadInputStream(userViewQueryDTO.getPath())).thenReturn(inputStream);
            excelUtilMockedStatic.when(() -> ExcelUtil.getReader(inputStream, 0)).thenThrow(DependencyException.class);
            assertThrows(ClientServiceException.class,()->kpUserService.view(userViewQueryDTO));
        }
    }

    /**
     * when the user file is error
     * Method under test: {@link KpUserServiceImpl#view(UserViewQueryDTO)}
     */
    @Test
    void view1() {
        /*try (
                MockedStatic<ExcelUtil> excelUtilMockedStatic = mockStatic(ExcelUtil.class);
                MockedConstruction<ReadExcelUtlis> excelUtilMockedConstruction = mockConstruction(ReadExcelUtlis.class, (mock, context) -> {
                    when(mock.importExcel(any(), any(), any(), any(),any())).thenThrow(IOException.class);
                })
        ){
            UserViewQueryDTO userViewQueryDTO = new UserViewQueryDTO();
            userViewQueryDTO.setPath("111.xlsx");
            ValueOperations valueOperations = mock(ValueOperations.class);
            when(redisTemplate.opsForValue()).thenReturn(valueOperations);
            when(valueOperations.get(userViewQueryDTO.getPath())).thenReturn(null);
            InputStream inputStream = mock(InputStream.class);
            when(ossComponent.downloadInputStream(userViewQueryDTO.getPath())).thenReturn(inputStream);
            ExcelReader reader = mock(ExcelReader.class);
            excelUtilMockedStatic.when(() -> ExcelUtil.getReader(inputStream, 0)).thenReturn(reader);
            List<List<Object>> list = mock(List.class);
            when(reader.read(1)).thenReturn(list);
            when(ossComponent.downloadInputStream(userViewQueryDTO.getPath())).thenReturn(inputStream);
            ServiceException serviceException = assertThrows(ServiceException.class, () -> kpUserService.view(userViewQueryDTO));
            assertEquals(AdminMsgEnum.USER_FILE_ERROR,serviceException.getCode());
        }*/

    }

    /**
     * successfully
     * Method under test: {@link KpUserServiceImpl#view(UserViewQueryDTO)}
     */
    @Test
    void view2() {
        List<UserUploadViewDTO> resultList = new ArrayList<>();
        try (
                MockedStatic<ExcelUtil> excelUtilMockedStatic = mockStatic(ExcelUtil.class);
                MockedConstruction<ReadExcelUtlis> excelUtilMockedConstruction = mockConstruction(ReadExcelUtlis.class, (mock, context) -> {
                    when(mock.importExcel(any(), any(), any(), any(),any())).thenReturn(resultList);
                })
        ){
            UserViewQueryDTO userViewQueryDTO = new UserViewQueryDTO();
            userViewQueryDTO.setPath("111.xlsx");
            ValueOperations valueOperations = mock(ValueOperations.class);
            when(redisTemplate.opsForValue()).thenReturn(valueOperations);
            when(valueOperations.get(userViewQueryDTO.getPath())).thenReturn(null);
            InputStream inputStream = mock(InputStream.class);
            when(ossComponent.downloadInputStream(userViewQueryDTO.getPath())).thenReturn(inputStream);
            ExcelReader reader = mock(ExcelReader.class);
            excelUtilMockedStatic.when(() -> ExcelUtil.getReader(inputStream, 0)).thenReturn(reader);
            List<List<Object>> list = mock(List.class);
            when(reader.read(1)).thenReturn(list);
            when(ossComponent.downloadInputStream(userViewQueryDTO.getPath())).thenReturn(inputStream);
            Page<UserUploadViewDTO> page = new Page<>(userViewQueryDTO.getPageNo(), userViewQueryDTO.getPageSize(), Long.parseLong(String.valueOf(list.size())));
            page.setRecords(resultList);
            assertEquals(JSONObject.toJSONString(page),JSONObject.toJSONString(kpUserService.view(userViewQueryDTO)));
        }

    }

    /**
     * when the file name is null
     * Method under test: {@link KpUserServiceImpl#importFile(String)}
     */
    @Test
    void importFile() {
        String path = "path";
        ValueOperations valueOperations = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(CommonConstant.BATCH_ADD_USER_UPLOAD_KEY + path)).thenReturn(null);
        assertThrows(ClientServiceException.class,() -> kpUserService.importFile(path));
    }

    /**
     *  when the user file is error
     * Method under test: {@link KpUserServiceImpl#importFile(String)}
     */
    @Test
    void importFile1() {
        /*String path = "path";
        String fileName = "file name";
        try (
                MockedStatic<SessionUtil> sessionUtilMockedStatic = mockStatic(SessionUtil.class);
                MockedConstruction<ReadExcelUtlis> excelUtilMockedConstruction = mockConstruction(ReadExcelUtlis.class, (mock, context) -> {
                    when(mock.importExcel(any(), any(), any(), any(),any())).thenThrow(IOException.class);
                })
        ) {
            sessionUtilMockedStatic.when(() -> SessionUtil.getUserId()).thenReturn("user id");
            ValueOperations valueOperations = mock(ValueOperations.class);
            when(redisTemplate.opsForValue()).thenReturn(valueOperations);
            when(valueOperations.get(CommonConstant.BATCH_ADD_USER_UPLOAD_KEY + path)).thenReturn(fileName);
            when(ossComponent.downloadByte(path)).thenReturn(new byte[16]);
            InputStream inputStream = mock(InputStream.class);
            when(ossComponent.downloadInputStream(path)).thenReturn(inputStream);
            ClientServiceException exception = assertThrows(ClientServiceException.class, () -> kpUserService.importFile(path));
            assertEquals(AdminMsgEnum.USER_FILE_ERROR,exception.getCode());
        }*/
    }

    /**
     *  when the user file has invalid data
     * Method under test: {@link KpUserServiceImpl#importFile(String)}
     */
    @Test
    void importFile2() {
        String path = "11.xlsx";
        String fileName = "file name";
        List<UserUploadViewDTO> resultList = new ArrayList<>();
        try (
                MockedStatic<SessionUtil> sessionUtilMockedStatic = mockStatic(SessionUtil.class);
                MockedConstruction<ReadExcelUtlis> excelUtilMockedConstruction = mockConstruction(ReadExcelUtlis.class, (mock, context) -> {
                    when(mock.importExcel(any(), any(), any(), any(),any())).thenReturn(resultList);
                })
        ) {
            sessionUtilMockedStatic.when(() -> SessionUtil.getUserId()).thenReturn("user id");
            ValueOperations valueOperations = mock(ValueOperations.class);
            when(redisTemplate.opsForValue()).thenReturn(valueOperations);
            when(valueOperations.get(CommonConstant.BATCH_ADD_USER_UPLOAD_KEY + path)).thenReturn(fileName);
            when(ossComponent.downloadByte(path)).thenReturn(new byte[16]);
            InputStream inputStream = mock(InputStream.class);
            when(ossComponent.downloadInputStream(path)).thenReturn(inputStream);
            KpUserServiceImpl kpUserServiceSpy = spy(this.kpUserService);
            ClientServiceException clientServiceException = new ClientServiceException(AdminMsgEnum.USER_HAVE_CHILD_USER);
            doThrow(clientServiceException).when(kpUserServiceSpy).importUploadBatchAddUser(any(),any());
            when(messageUtils.getMessage(any())).thenReturn("remark");
            when(uploadRecordService.save(any())).thenReturn(true);
            ClientServiceException exception = assertThrows(ClientServiceException.class, () -> kpUserServiceSpy.importFile(path));
            assertEquals(AdminMsgEnum.USER_FILE_ERROR,exception.getCode());
        }
    }

    /**
     *  import data all failed
     * Method under test: {@link KpUserServiceImpl#importFile(String)}
     */
    @Test
    void importFile3() {
        String path = "11.xlsx";
        String fileName = "file name";
        int count = 0;
        List<UserUploadViewDTO> resultList = new ArrayList<>();
        try (
                MockedStatic<SessionUtil> sessionUtilMockedStatic = mockStatic(SessionUtil.class);
                MockedConstruction<ReadExcelUtlis> excelUtilMockedConstruction = mockConstruction(ReadExcelUtlis.class, (mock, context) -> {
                    when(mock.importExcel(any(), any(), any(), any(),any())).thenReturn(resultList);
                })
        ) {
            sessionUtilMockedStatic.when(() -> SessionUtil.getUserId()).thenReturn("user id");
            ValueOperations valueOperations = mock(ValueOperations.class);
            when(redisTemplate.opsForValue()).thenReturn(valueOperations);
            when(valueOperations.get(CommonConstant.BATCH_ADD_USER_UPLOAD_KEY + path)).thenReturn(fileName);
            when(ossComponent.downloadByte(path)).thenReturn(new byte[16]);
            InputStream inputStream = mock(InputStream.class);
            when(ossComponent.downloadInputStream(path)).thenReturn(inputStream);
            KpUserServiceImpl kpUserServiceSpy = spy(this.kpUserService);
            doReturn(count).when(kpUserServiceSpy).importUploadBatchAddUser(any(),any());
            when(messageUtils.getMessage(any())).thenReturn("remark");
            when(uploadRecordService.save(any())).thenReturn(true);
            UploadResultDTO uploadResultDTO = new UploadResultDTO();
            uploadResultDTO.setTotal(resultList.size());
            uploadResultDTO.setSuccess(count);
            uploadResultDTO.setFail(resultList.size() - count);
            assertEquals(JSONObject.toJSONString(uploadResultDTO),JSONObject.toJSONString(kpUserServiceSpy.importFile(path)));
        }
    }

    /**
     *  import data abnormal
     * Method under test: {@link KpUserServiceImpl#importFile(String)}
     */
    @Test
    void importFile4() {
        String path = "11.xlsx";
        String fileName = "file name";
        int count = 1;
        int resultSize = 3;
        List<UserUploadViewDTO> resultList = mock(List.class);
        try (
                MockedStatic<SessionUtil> sessionUtilMockedStatic = mockStatic(SessionUtil.class);
                MockedConstruction<ReadExcelUtlis> excelUtilMockedConstruction = mockConstruction(ReadExcelUtlis.class, (mock, context) -> {
                    when(mock.importExcel(any(), any(), any(), any(),any())).thenReturn(resultList);
                })
        ) {
            sessionUtilMockedStatic.when(() -> SessionUtil.getUserId()).thenReturn("user id");
            ValueOperations valueOperations = mock(ValueOperations.class);
            when(redisTemplate.opsForValue()).thenReturn(valueOperations);
            when(valueOperations.get(CommonConstant.BATCH_ADD_USER_UPLOAD_KEY + path)).thenReturn(fileName);
            when(ossComponent.downloadByte(path)).thenReturn(new byte[16]);
            InputStream inputStream = mock(InputStream.class);
            when(ossComponent.downloadInputStream(path)).thenReturn(inputStream);
            KpUserServiceImpl kpUserServiceSpy = spy(this.kpUserService);
            doReturn(count).when(kpUserServiceSpy).importUploadBatchAddUser(any(),any());
            when(messageUtils.getMessage(any())).thenReturn("remark");
            when(uploadRecordService.save(any())).thenReturn(true);
            UploadResultDTO uploadResultDTO = new UploadResultDTO();
            uploadResultDTO.setTotal(resultSize);
            uploadResultDTO.setSuccess(count);
            uploadResultDTO.setFail(resultSize - count);
            when(resultList.size()).thenReturn(resultSize);
            assertEquals(JSONObject.toJSONString(uploadResultDTO),JSONObject.toJSONString(kpUserServiceSpy.importFile(path)));
        }
    }

    /**
     *  import data all successfully
     * Method under test: {@link KpUserServiceImpl#importFile(String)}
     */
    @Test
    void importFile5() {
        String path = "11.xlsx";
        String fileName = "file name";
        int count = 3;
        int resultSize = count;
        List<UserUploadViewDTO> resultList = mock(List.class);
        try (
                MockedStatic<SessionUtil> sessionUtilMockedStatic = mockStatic(SessionUtil.class);
                MockedConstruction<ReadExcelUtlis> excelUtilMockedConstruction = mockConstruction(ReadExcelUtlis.class, (mock, context) -> {
                    when(mock.importExcel(any(), any(), any(), any(),any())).thenReturn(resultList);
                })
        ) {
            sessionUtilMockedStatic.when(() -> SessionUtil.getUserId()).thenReturn("user id");
            ValueOperations valueOperations = mock(ValueOperations.class);
            when(redisTemplate.opsForValue()).thenReturn(valueOperations);
            when(valueOperations.get(CommonConstant.BATCH_ADD_USER_UPLOAD_KEY + path)).thenReturn(fileName);
            when(ossComponent.downloadByte(path)).thenReturn(new byte[16]);
            InputStream inputStream = mock(InputStream.class);
            when(ossComponent.downloadInputStream(path)).thenReturn(inputStream);
            KpUserServiceImpl kpUserServiceSpy = spy(this.kpUserService);
            doReturn(count).when(kpUserServiceSpy).importUploadBatchAddUser(any(),any());
            when(messageUtils.getMessage(any())).thenReturn("remark");
            when(uploadRecordService.save(any())).thenReturn(true);
            UploadResultDTO uploadResultDTO = new UploadResultDTO();
            uploadResultDTO.setTotal(resultSize);
            uploadResultDTO.setSuccess(count);
            uploadResultDTO.setFail(resultSize - count);
            when(resultList.size()).thenReturn(3);
            assertEquals(JSONObject.toJSONString(uploadResultDTO),JSONObject.toJSONString(kpUserServiceSpy.importFile(path)));
        }
    }

    /**
     * Method under test: {@link KpUserServiceImpl#downloadUploadFile(HttpServletResponse, String, String)}
     */
    @Test
    void downloadUploadFile() {
        String path = "11.xlsx";
        String fileName = path;
        try (MockedConstruction<XSSFWorkbook> excelUtilMockedConstruction = mockConstruction(XSSFWorkbook.class)) {
            InputStream inputStream = mock(InputStream.class);
            when(ossComponent.downloadInputStream(path)).thenReturn(inputStream);
            assertDoesNotThrow(()->kpUserService.downloadUploadFile(mock(HttpServletResponse.class),path,fileName));
        }
    }

    /**
     * Method under test: {@link KpUserServiceImpl#downloadUploadFile(HttpServletResponse, String, String)}
     */
    @Test
    void downloadUploadFile1() {
        String path = "11.xls";
        String fileName = path;
        try (MockedConstruction<XSSFWorkbook> excelUtilMockedConstruction = mockConstruction(XSSFWorkbook.class)) {
            InputStream inputStream = mock(InputStream.class);
            when(ossComponent.downloadInputStream(path)).thenReturn(inputStream);
            //assertDoesNotThrow(()->kpUserService.downloadUploadFile(mock(HttpServletResponse.class),path,fileName));
        }
    }

    /**
     * when the ids are empty
     * Method under test: {@link KpUserServiceImpl#export(List, HttpServletResponse)}
     */
    @Test
    void export() {
        List<String> ids = new ArrayList<>();
        ClientServiceException exception = assertThrows(ClientServiceException.class, () -> kpUserService.export(ids, null));
        assertEquals(AdminMsgEnum.IDS_EMPTY,exception.getCode());
    }

    /**
     * export successfully
     * Method under test: {@link KpUserServiceImpl#export(List, HttpServletResponse)}
     */
    @Test
    void export1() {
        try (MockedConstruction<ReadExcelUtlis> excelUtilMockedConstruction = mockConstruction(ReadExcelUtlis.class)) {
            List<String> ids = new ArrayList<>();
            ids.add("id");
            IPage<UserDTO> page = new Page<>();
            List<UserDTO> records = new ArrayList<>();
            UserDTO userDTO = new UserDTO();
            IdNameDTO idNameDTO = new IdNameDTO();
            idNameDTO.setName("role name");
            idNameDTO.setId(1);
            List<IdNameDTO> idNameDTOS = new ArrayList<>();
            idNameDTOS.add(idNameDTO);
            userDTO.setRole(idNameDTOS);
            userDTO.setValidityStartTime(new Date());
            userDTO.setValidityEndTime(new Date());
            userDTO.setLastLoginTime(new Date());
            userDTO.setStatus(1);
            records.add(userDTO);
            page.setRecords(records);
            when(userService.selectList(any())).thenReturn(page);
            assertDoesNotThrow(()->kpUserService.export(ids,mock(HttpServletResponse.class)));
        }

    }

    /**
     * when the phone is duplicate
     * Method under test: {@link KpUserServiceImpl#updateMyself(UserUpdateProfileDTO)}
     */
    @Test
    void updateMyself() {
        UserUpdateProfileDTO userUpdateProfileDTO = new UserUpdateProfileDTO();
        userUpdateProfileDTO.setPhone("phone");
        when(userService.count(any())).thenReturn(1L);
        ClientServiceException exception = assertThrows(ClientServiceException.class, () -> kpUserService.updateMyself(userUpdateProfileDTO));
        assertEquals(UaaServerMsgEnum.PHONE_DUPLICATE,exception.getCode());
    }

    /**
     * when the phone is not blank and country code is blank
     * Method under test: {@link KpUserServiceImpl#updateMyself(UserUpdateProfileDTO)}
     */
    @Test
    void updateMyself1() {
        UserUpdateProfileDTO userUpdateProfileDTO = new UserUpdateProfileDTO();
        userUpdateProfileDTO.setPhone("phone");
        when(userService.count(any())).thenReturn(0L);
        ClientServiceException exception = assertThrows(ClientServiceException.class, () -> kpUserService.updateMyself(userUpdateProfileDTO));
        assertEquals(AdminMsgEnum.COUNTRY_CODE_EMPTY,exception.getCode());
    }

    /**
     * when the country code is invalid
     * Method under test: {@link KpUserServiceImpl#updateMyself(UserUpdateProfileDTO)}
     */
    @Test
    void updateMyself2() {
        UserUpdateProfileDTO userUpdateProfileDTO = new UserUpdateProfileDTO();
        userUpdateProfileDTO.setPhone("phone");
        userUpdateProfileDTO.setCountryCode("country code");
        when(userService.count(any())).thenReturn(0L);
        when(nationStrategy.get(userUpdateProfileDTO.getCountryCode())).thenReturn(null);
        ClientServiceException exception = assertThrows(ClientServiceException.class, () -> kpUserService.updateMyself(userUpdateProfileDTO));
        assertEquals(AdminMsgEnum.COUNTRY_CODE_INVALID,exception.getCode());
    }

    /**
     * when the phone is invalid
     * Method under test: {@link KpUserServiceImpl#updateMyself(UserUpdateProfileDTO)}
     */
    @Test
    void updateMyself3() {
        UserUpdateProfileDTO userUpdateProfileDTO = new UserUpdateProfileDTO();
        userUpdateProfileDTO.setPhone("phone");
        userUpdateProfileDTO.setCountryCode("country code");
        when(userService.count(any())).thenReturn(0L);
        NationStrategy nationStrategy = mock(NationStrategy.class);
        when(this.nationStrategy.get(userUpdateProfileDTO.getCountryCode())).thenReturn(nationStrategy);
        when(nationStrategy.verifyPhone(userUpdateProfileDTO.getPhone())).thenReturn(false);
        ClientServiceException exception = assertThrows(ClientServiceException.class, () -> kpUserService.updateMyself(userUpdateProfileDTO));
        assertEquals(AdminMsgEnum.PHONE_INVALID,exception.getCode());
    }

    /**
     * when the email is duplicate
     * Method under test: {@link KpUserServiceImpl#updateMyself(UserUpdateProfileDTO)}
     */
    @Test
    void updateMyself4() {
        UserUpdateProfileDTO userUpdateProfileDTO = new UserUpdateProfileDTO();
        userUpdateProfileDTO.setEmail("111");
        when(userService.count(any())).thenReturn(1L);
        ClientServiceException exception = assertThrows(ClientServiceException.class, () -> kpUserService.updateMyself(userUpdateProfileDTO));
        assertEquals(UaaServerMsgEnum.EMAIL_DUPLICATE,exception.getCode());
    }

    /**
     * when the email is invalid
     * Method under test: {@link KpUserServiceImpl#updateMyself(UserUpdateProfileDTO)}
     */
    @Test
    void updateMyself5() {
        UserUpdateProfileDTO userUpdateProfileDTO = new UserUpdateProfileDTO();
        userUpdateProfileDTO.setEmail("111");
        when(userService.count(any())).thenReturn(0L);
        ClientServiceException exception = assertThrows(ClientServiceException.class, () -> kpUserService.updateMyself(userUpdateProfileDTO));
        assertEquals(AdminMsgEnum.EMAIL_INVALID,exception.getCode());
    }

    /**
     * update successfully
     * Method under test: {@link KpUserServiceImpl#updateMyself(UserUpdateProfileDTO)}
     */
    @Test
    void updateMyself6() {
        try (MockedStatic<SessionUtil> sessionUtilMockedStatic = mockStatic(SessionUtil.class)) {
            String userId = "user id";
            UserSession userSession = new UserSession();
            userSession.setTenantId("tenant id");
            sessionUtilMockedStatic.when(SessionUtil::getUserSession).thenReturn(userSession);
            UserUpdateProfileDTO userUpdateProfileDTO = new UserUpdateProfileDTO();
            when(userService.count(any())).thenReturn(0L);
            when(userService.updateById(any())).thenReturn(true);
            doNothing().when(kpUserOnlineService).forceLogout(any());
            assertDoesNotThrow(()->kpUserService.updateMyself(userUpdateProfileDTO));
        }
    }

    /**
     * Method under test: {@link KpUserServiceImpl#getUserIdsByRoleId(Set)}
     */
    @Test
    void getUserIdsByRoleId() {
        List<UserRoleMapping> list = new ArrayList<>();
        UserRoleMapping userRoleMapping = new UserRoleMapping();
        userRoleMapping.setUserId("user id");
        list.add(userRoleMapping);
        when(userRoleMappingService.list(any())).thenReturn(list);
        Set<String> userIds = list.stream().map(UserRoleMapping::getUserId).collect(Collectors.toSet());
        assertEquals(userIds.toString(),kpUserService.getUserIdsByRoleId(mock(Set.class)).toString());
    }

    /**
     * when the username is blank
     * Method under test: {@link KpUserServiceImpl#importUploadBatchAddUser(List, String)}
     */
    @Test
    void importUploadBatchAddUser() {
        List<UserUploadViewDTO> resultList = new ArrayList<>();
        UserUploadViewDTO userUploadViewDTO = new UserUploadViewDTO();
        //userUploadViewDTO.setUserName("username");
        resultList.add(userUploadViewDTO);
        String userId = "user id";
        try (MockedStatic<SessionUtil> sessionUtilMockedStatic = mockStatic(SessionUtil.class)) {
            sessionUtilMockedStatic.when(SessionUtil::getUserId).thenReturn(userId);
            List<Role> roles = new ArrayList<>();
            Role role = new Role();
            role.setName("roleName");
            role.setId(1);
            roles.add(role);
            when(roleMapper.selectList(any())).thenReturn(roles);
            KpUserServiceImpl kpUserServiceSpy = spy(this.kpUserService);
            doNothing().when(kpUserServiceSpy).updateExcelStatusCellValue(any(),any());
            assertEquals(0,kpUserServiceSpy.importUploadBatchAddUser(resultList,"11.xlsx"));
        }

    }

    /**
     * when the username contains special character
     * Method under test: {@link KpUserServiceImpl#importUploadBatchAddUser(List, String)}
     */
    @Test
    void importUploadBatchAddUser1() {
        List<UserUploadViewDTO> resultList = new ArrayList<>();
        UserUploadViewDTO userUploadViewDTO = new UserUploadViewDTO();
        userUploadViewDTO.setUserName("=");
        resultList.add(userUploadViewDTO);
        String userId = "user id";
        try (MockedStatic<SessionUtil> sessionUtilMockedStatic = mockStatic(SessionUtil.class)) {
            sessionUtilMockedStatic.when(SessionUtil::getUserId).thenReturn(userId);
            List<Role> roles = new ArrayList<>();
            Role role = new Role();
            role.setName("roleName");
            role.setId(1);
            roles.add(role);
            when(roleMapper.selectList(any())).thenReturn(roles);
            KpUserServiceImpl kpUserServiceSpy = spy(this.kpUserService);
            doNothing().when(kpUserServiceSpy).updateExcelStatusCellValue(any(),any());
            assertEquals(0,kpUserServiceSpy.importUploadBatchAddUser(resultList,"11.xlsx"));
        }
    }

    /**
     * when the length of username is up to 50
     * Method under test: {@link KpUserServiceImpl#importUploadBatchAddUser(List, String)}
     */
    @Test
    void importUploadBatchAddUser2() {
        try (MockedStatic<SessionUtil> sessionUtilMockedStatic = mockStatic(SessionUtil.class)) {
            List<UserUploadViewDTO> resultList = new ArrayList<>();
            UserUploadViewDTO userUploadViewDTO = new UserUploadViewDTO();
            userUploadViewDTO.setUserName("123456789012345678901234567890123456789012345678901");
            resultList.add(userUploadViewDTO);
            String userId = "user id";
            sessionUtilMockedStatic.when(SessionUtil::getUserId).thenReturn(userId);
            List<Role> roles = new ArrayList<>();
            Role role = new Role();
            role.setName("roleName");
            role.setId(1);
            roles.add(role);
            when(roleMapper.selectList(any())).thenReturn(roles);
            KpUserServiceImpl kpUserServiceSpy = spy(this.kpUserService);
            doNothing().when(kpUserServiceSpy).updateExcelStatusCellValue(any(),any());
            assertEquals(0,kpUserServiceSpy.importUploadBatchAddUser(resultList,"11.xlsx"));
        }

    }

    /**
     * when the length of username is up to 50
     * Method under test: {@link KpUserServiceImpl#importUploadBatchAddUser(List, String)}
     */
    @Test
    void importUploadBatchAddUser3() {
        List<UserUploadViewDTO> resultList = new ArrayList<>();
        UserUploadViewDTO userUploadViewDTO = new UserUploadViewDTO();
        userUploadViewDTO.setUserName("12213");
        userUploadViewDTO.setFullName("12345678901234567890123456");
        resultList.add(userUploadViewDTO);
        String userId = "user id";
        try (MockedStatic<SessionUtil> sessionUtilMockedStatic = mockStatic(SessionUtil.class)) {
            sessionUtilMockedStatic.when(SessionUtil::getUserId).thenReturn(userId);
            List<Role> roles = new ArrayList<>();
            Role role = new Role();
            role.setName("roleName");
            role.setId(1);
            roles.add(role);
            when(roleMapper.selectList(any())).thenReturn(roles);
            KpUserServiceImpl kpUserServiceSpy = spy(this.kpUserService);
            doNothing().when(kpUserServiceSpy).updateExcelStatusCellValue(any(),any());
            assertEquals(0,kpUserServiceSpy.importUploadBatchAddUser(resultList,"11.xlsx"));
        }

    }

    /**
     * when the email is invalid
     * Method under test: {@link KpUserServiceImpl#importUploadBatchAddUser(List, String)}
     */
    @Test
    void importUploadBatchAddUser4() {
        List<UserUploadViewDTO> resultList = new ArrayList<>();
        UserUploadViewDTO userUploadViewDTO = new UserUploadViewDTO();
        userUploadViewDTO.setUserName("12213");
        userUploadViewDTO.setFullName("full name");
        userUploadViewDTO.setEmail("121");
        resultList.add(userUploadViewDTO);
        String userId = "user id";
        try (MockedStatic<SessionUtil> sessionUtilMockedStatic = mockStatic(SessionUtil.class)) {
            sessionUtilMockedStatic.when(SessionUtil::getUserId).thenReturn(userId);
            List<Role> roles = new ArrayList<>();
            Role role = new Role();
            role.setName("roleName");
            role.setId(1);
            roles.add(role);
            when(roleMapper.selectList(any())).thenReturn(roles);
            KpUserServiceImpl kpUserServiceSpy = spy(this.kpUserService);
            doNothing().when(kpUserServiceSpy).updateExcelStatusCellValue(any(),any());
            assertEquals(0,kpUserServiceSpy.importUploadBatchAddUser(resultList,"11.xlsx"));
        }

    }

    /**
     * when the phone is not blank and the country code is blank
     * Method under test: {@link KpUserServiceImpl#importUploadBatchAddUser(List, String)}
     */
    @Test
    void importUploadBatchAddUser5() {
        List<UserUploadViewDTO> resultList = new ArrayList<>();
        UserUploadViewDTO userUploadViewDTO = new UserUploadViewDTO();
        userUploadViewDTO.setUserName("12213");
        userUploadViewDTO.setPhone("111");
        userUploadViewDTO.setFullName("full name");
        resultList.add(userUploadViewDTO);
        String userId = "user id";
        try (MockedStatic<SessionUtil> sessionUtilMockedStatic = mockStatic(SessionUtil.class)) {
            sessionUtilMockedStatic.when(SessionUtil::getUserId).thenReturn(userId);
            List<Role> roles = new ArrayList<>();
            Role role = new Role();
            role.setName("roleName");
            role.setId(1);
            roles.add(role);
            when(roleMapper.selectList(any())).thenReturn(roles);
            KpUserServiceImpl kpUserServiceSpy = spy(this.kpUserService);
            doNothing().when(kpUserServiceSpy).updateExcelStatusCellValue(any(),any());
            assertEquals(0,kpUserServiceSpy.importUploadBatchAddUser(resultList,"11.xlsx"));
        }

    }

    /**
     * when the country code is invalid
     * Method under test: {@link KpUserServiceImpl#importUploadBatchAddUser(List, String)}
     */
    @Test
    void importUploadBatchAddUser6() {
        List<UserUploadViewDTO> resultList = new ArrayList<>();
        UserUploadViewDTO userUploadViewDTO = new UserUploadViewDTO();
        userUploadViewDTO.setUserName("12213");
        userUploadViewDTO.setPhone("111");
        userUploadViewDTO.setCountryCode("111");
        userUploadViewDTO.setFullName("full name");
        resultList.add(userUploadViewDTO);
        String userId = "user id";
        try (MockedStatic<SessionUtil> sessionUtilMockedStatic = mockStatic(SessionUtil.class)) {
            sessionUtilMockedStatic.when(SessionUtil::getUserId).thenReturn(userId);
            List<Role> roles = new ArrayList<>();
            Role role = new Role();
            role.setName("roleName");
            role.setId(1);
            roles.add(role);
            when(roleMapper.selectList(any())).thenReturn(roles);
            when(nationStrategy.get(any())).thenReturn(null);
            KpUserServiceImpl kpUserServiceSpy = spy(this.kpUserService);
            doNothing().when(kpUserServiceSpy).updateExcelStatusCellValue(any(),any());
            assertEquals(0,kpUserServiceSpy.importUploadBatchAddUser(resultList,"11.xlsx"));
        }

    }

    /**
     * when the phone is invalid
     * Method under test: {@link KpUserServiceImpl#importUploadBatchAddUser(List, String)}
     */
    @Test
    void importUploadBatchAddUser7() {
        List<UserUploadViewDTO> resultList = new ArrayList<>();
        UserUploadViewDTO userUploadViewDTO = new UserUploadViewDTO();
        userUploadViewDTO.setUserName("12213");
        userUploadViewDTO.setPhone("111");
        userUploadViewDTO.setCountryCode("111");
        userUploadViewDTO.setFullName("full name");
        resultList.add(userUploadViewDTO);
        String userId = "user id";
        try (MockedStatic<SessionUtil> sessionUtilMockedStatic = mockStatic(SessionUtil.class)) {
            sessionUtilMockedStatic.when(SessionUtil::getUserId).thenReturn(userId);
            List<Role> roles = new ArrayList<>();
            Role role = new Role();
            role.setName("roleName");
            role.setId(1);
            roles.add(role);
            when(roleMapper.selectList(any())).thenReturn(roles);
            NationStrategy nationStrategy = mock(NationStrategy.class);
            when(this.nationStrategy.get(any())).thenReturn(nationStrategy);
            when(nationStrategy.verifyPhone(userUploadViewDTO.getPhone())).thenReturn(false);
            KpUserServiceImpl kpUserServiceSpy = spy(this.kpUserService);
            doNothing().when(kpUserServiceSpy).updateExcelStatusCellValue(any(),any());
            assertEquals(0,kpUserServiceSpy.importUploadBatchAddUser(resultList,"11.xlsx"));
        }
    }

    /**
     *when the validity period is not completeness
     * Method under test: {@link KpUserServiceImpl#importUploadBatchAddUser(List, String)}
     */
    @Test
    void importUploadBatchAddUser8() {
        List<UserUploadViewDTO> resultList = new ArrayList<>();
        UserUploadViewDTO userUploadViewDTO = new UserUploadViewDTO();
        userUploadViewDTO.setUserName("12213");
        userUploadViewDTO.setFullName("full name");
        userUploadViewDTO.setValidityStartTime(LocalDateTime.now());
        resultList.add(userUploadViewDTO);
        String userId = "user id";
        try (MockedStatic<SessionUtil> sessionUtilMockedStatic = mockStatic(SessionUtil.class)) {
            sessionUtilMockedStatic.when(SessionUtil::getUserId).thenReturn(userId);
            List<Role> roles = new ArrayList<>();
            Role role = new Role();
            role.setName("roleName");
            role.setId(1);
            roles.add(role);
            when(roleMapper.selectList(any())).thenReturn(roles);
            KpUserServiceImpl kpUserServiceSpy = spy(this.kpUserService);
            doNothing().when(kpUserServiceSpy).updateExcelStatusCellValue(any(),any());
            assertEquals(0,kpUserServiceSpy.importUploadBatchAddUser(resultList,"11.xlsx"));
        }
    }

    /**
     * when the validity start time is greater than the validity end time
     * Method under test: {@link KpUserServiceImpl#importUploadBatchAddUser(List, String)}
     */
    @Test
    void importUploadBatchAddUser9() {
        List<UserUploadViewDTO> resultList = new ArrayList<>();
        UserUploadViewDTO userUploadViewDTO = new UserUploadViewDTO();
        userUploadViewDTO.setUserName("12213");
        userUploadViewDTO.setFullName("full name");
        userUploadViewDTO.setValidityEndTime(LocalDateTime.now());
        userUploadViewDTO.setValidityStartTime(LocalDateTime.now());
        resultList.add(userUploadViewDTO);
        String userId = "user id";
        try (MockedStatic<SessionUtil> sessionUtilMockedStatic = mockStatic(SessionUtil.class)) {
            sessionUtilMockedStatic.when(SessionUtil::getUserId).thenReturn(userId);
            List<Role> roles = new ArrayList<>();
            Role role = new Role();
            role.setName("roleName");
            role.setId(1);
            roles.add(role);
            when(roleMapper.selectList(any())).thenReturn(roles);
            KpUserServiceImpl kpUserServiceSpy = spy(this.kpUserService);
            doNothing().when(kpUserServiceSpy).updateExcelStatusCellValue(any(),any());
            assertEquals(0,kpUserServiceSpy.importUploadBatchAddUser(resultList,"11.xlsx"));
        }

    }

    /**
     * when the ip end is null and ip start is not null
     * Method under test: {@link KpUserServiceImpl#importUploadBatchAddUser(List, String)}
     */
    @Test
    void importUploadBatchAddUser11() {
        List<UserUploadViewDTO> resultList = new ArrayList<>();
        UserUploadViewDTO userUploadViewDTO = new UserUploadViewDTO();
        userUploadViewDTO.setUserName("12213");
        userUploadViewDTO.setIpStart("111");
        userUploadViewDTO.setFullName("full name");
        resultList.add(userUploadViewDTO);
        String userId = "user id";
        try (MockedStatic<SessionUtil> sessionUtilMockedStatic = mockStatic(SessionUtil.class)) {
            sessionUtilMockedStatic.when(SessionUtil::getUserId).thenReturn(userId);
            List<Role> roles = new ArrayList<>();
            Role role = new Role();
            role.setName("roleName");
            role.setId(1);
            roles.add(role);
            when(roleMapper.selectList(any())).thenReturn(roles);
            KpUserServiceImpl kpUserServiceSpy = spy(this.kpUserService);
            doNothing().when(kpUserServiceSpy).updateExcelStatusCellValue(any(),any());
            assertEquals(0,kpUserServiceSpy.importUploadBatchAddUser(resultList,"11.xlsx"));
        }

    }

    /**
     * when the ip start is null and ip end is not null
     * Method under test: {@link KpUserServiceImpl#importUploadBatchAddUser(List, String)}
     */
    @Test
    void importUploadBatchAddUser12() {
        List<UserUploadViewDTO> resultList = new ArrayList<>();
        UserUploadViewDTO userUploadViewDTO = new UserUploadViewDTO();
        userUploadViewDTO.setUserName("12213");
        userUploadViewDTO.setIpEnd("111");
        userUploadViewDTO.setFullName("full name");
        resultList.add(userUploadViewDTO);
        String userId = "user id";
        try (MockedStatic<SessionUtil> sessionUtilMockedStatic = mockStatic(SessionUtil.class)) {
            sessionUtilMockedStatic.when(SessionUtil::getUserId).thenReturn(userId);
            List<Role> roles = new ArrayList<>();
            Role role = new Role();
            role.setName("roleName");
            role.setId(1);
            roles.add(role);
            when(roleMapper.selectList(any())).thenReturn(roles);
            KpUserServiceImpl kpUserServiceSpy = spy(this.kpUserService);
            doNothing().when(kpUserServiceSpy).updateExcelStatusCellValue(any(),any());
            assertEquals(0,kpUserServiceSpy.importUploadBatchAddUser(resultList,"11.xlsx"));
        }

    }

    /**
     * when the ip start is null and ip end is not null
     * Method under test: {@link KpUserServiceImpl#importUploadBatchAddUser(List, String)}
     */
    @Test
    void importUploadBatchAddUser13() {
        List<UserUploadViewDTO> resultList = new ArrayList<>();
        UserUploadViewDTO userUploadViewDTO = new UserUploadViewDTO();
        userUploadViewDTO.setUserName("12213");
        userUploadViewDTO.setIpEnd("111");
        userUploadViewDTO.setFullName("full name");
        resultList.add(userUploadViewDTO);
        String userId = "user id";
        try (MockedStatic<SessionUtil> sessionUtilMockedStatic = mockStatic(SessionUtil.class)) {
            sessionUtilMockedStatic.when(SessionUtil::getUserId).thenReturn(userId);
            List<Role> roles = new ArrayList<>();
            Role role = new Role();
            role.setName("roleName");
            role.setId(1);
            roles.add(role);
            when(roleMapper.selectList(any())).thenReturn(roles);
            KpUserServiceImpl kpUserServiceSpy = spy(this.kpUserService);
            doNothing().when(kpUserServiceSpy).updateExcelStatusCellValue(any(),any());
            assertEquals(0,kpUserServiceSpy.importUploadBatchAddUser(resultList,"11.xlsx"));
        }
    }

    /**
     * when the ip is invalid
     * Method under test: {@link KpUserServiceImpl#importUploadBatchAddUser(List, String)}
     */
    @Test
    void importUploadBatchAddUser10() {
        List<UserUploadViewDTO> resultList = new ArrayList<>();
        UserUploadViewDTO userUploadViewDTO = new UserUploadViewDTO();
        userUploadViewDTO.setUserName("12213");
        userUploadViewDTO.setIpStart("111");
        userUploadViewDTO.setIpEnd("111");
        userUploadViewDTO.setFullName("full name");
        resultList.add(userUploadViewDTO);
        String userId = "user id";
        try (MockedStatic<SessionUtil> sessionUtilMockedStatic = mockStatic(SessionUtil.class)) {
            sessionUtilMockedStatic.when(SessionUtil::getUserId).thenReturn(userId);
            List<Role> roles = new ArrayList<>();
            Role role = new Role();
            role.setName("roleName");
            role.setId(1);
            roles.add(role);
            when(roleMapper.selectList(any())).thenReturn(roles);
            KpUserServiceImpl kpUserServiceSpy = spy(this.kpUserService);
            doNothing().when(kpUserServiceSpy).updateExcelStatusCellValue(any(),any());
            assertEquals(0,kpUserServiceSpy.importUploadBatchAddUser(resultList,"11.xlsx"));
        }

    }

    /**
     * when the ip start is greater than ip end
     * Method under test: {@link KpUserServiceImpl#importUploadBatchAddUser(List, String)}
     */
    @Test
    void importUploadBatchAddUser14() {
        List<UserUploadViewDTO> resultList = new ArrayList<>();
        UserUploadViewDTO userUploadViewDTO = new UserUploadViewDTO();
        userUploadViewDTO.setUserName("12213");
        userUploadViewDTO.setFullName("full name");
        userUploadViewDTO.setIpStart("192.168.1.12");
        userUploadViewDTO.setIpEnd("192.168.1.1");
        resultList.add(userUploadViewDTO);
        String userId = "user id";
        try (MockedStatic<SessionUtil> sessionUtilMockedStatic = mockStatic(SessionUtil.class)) {
            sessionUtilMockedStatic.when(SessionUtil::getUserId).thenReturn(userId);
            List<Role> roles = new ArrayList<>();
            Role role = new Role();
            role.setName("roleName");
            role.setId(1);
            roles.add(role);
            when(roleMapper.selectList(any())).thenReturn(roles);
            KpUserServiceImpl kpUserServiceSpy = spy(this.kpUserService);
            doNothing().when(kpUserServiceSpy).updateExcelStatusCellValue(any(),any());
            assertEquals(0,kpUserServiceSpy.importUploadBatchAddUser(resultList,"11.xlsx"));
        }

    }

    /**
     * when the ip start is greater than ip end
     * Method under test: {@link KpUserServiceImpl#importUploadBatchAddUser(List, String)}
     */
    @Test
    void importUploadBatchAddUser15() {
        List<UserUploadViewDTO> resultList = new ArrayList<>();
        UserUploadViewDTO userUploadViewDTO = new UserUploadViewDTO();
        userUploadViewDTO.setUserName("12213");
        userUploadViewDTO.setFullName("full name");
        userUploadViewDTO.setRole("role");
        userUploadViewDTO.setIpStart("192.168.1.12");
        userUploadViewDTO.setIpEnd("192.168.1.1");
        resultList.add(userUploadViewDTO);
        String userId = "user id";
        try (MockedStatic<SessionUtil> sessionUtilMockedStatic = mockStatic(SessionUtil.class)) {
            sessionUtilMockedStatic.when(SessionUtil::getUserId).thenReturn(userId);
            List<Role> roles = new ArrayList<>();
            Role role = new Role();
            role.setName("roleName");
            role.setId(1);
            roles.add(role);
            when(roleMapper.selectList(any())).thenReturn(roles);
            KpUserServiceImpl kpUserServiceSpy = spy(this.kpUserService);
            doNothing().when(kpUserServiceSpy).updateExcelStatusCellValue(any(),any());
            assertEquals(0,kpUserServiceSpy.importUploadBatchAddUser(resultList,"11.xlsx"));
        }

    }

    /**
     * when the ip start is greater than ip end
     * Method under test: {@link KpUserServiceImpl#importUploadBatchAddUser(List, String)}
     */
    @Test
    void importUploadBatchAddUser16() {
        List<UserUploadViewDTO> resultList = new ArrayList<>();
        UserUploadViewDTO userUploadViewDTO = new UserUploadViewDTO();
        userUploadViewDTO.setUserName("12213");
        userUploadViewDTO.setFullName("full name");
        userUploadViewDTO.setRole("roleName");
        resultList.add(userUploadViewDTO);
        String userId = "user id";
        String tenantId = "tenant id";
        try (MockedStatic<SessionUtil> sessionUtilMockedStatic = mockStatic(SessionUtil.class)) {
            sessionUtilMockedStatic.when(SessionUtil::getUserId).thenReturn(userId);
            sessionUtilMockedStatic.when(SessionUtil::getTenantId).thenReturn(tenantId);
            List<Role> roles = new ArrayList<>();
            Role role = new Role();
            role.setName("roleName");
            role.setId(1);
            roles.add(role);
            when(roleMapper.selectList(any())).thenReturn(roles);
            UserGroup userGroup = new UserGroup();
            when(userGroupMapper.selectOne(any())).thenReturn(userGroup);
            when(userService.add(any())).thenReturn(true);
            KpUserServiceImpl kpUserServiceSpy = spy(this.kpUserService);
            doNothing().when(kpUserServiceSpy).updateExcelStatusCellValue(any(),any());
            assertEquals(1,kpUserServiceSpy.importUploadBatchAddUser(resultList,"11.xlsx"));
        }

    }

    /**
     * when the full name is null
     * Method under test: {@link KpUserServiceImpl#importUploadBatchAddUser(List, String)}
     */
    @Test
    void importUploadBatchAddUser17() {
        try (MockedStatic<SessionUtil> sessionUtilMockedStatic = mockStatic(SessionUtil.class)) {
            List<UserUploadViewDTO> resultList = new ArrayList<>();
            UserUploadViewDTO userUploadViewDTO = new UserUploadViewDTO();
            userUploadViewDTO.setUserName("1234");
            userUploadViewDTO.setFullName("full name");
            userUploadViewDTO.setValidityStartTime(LocalDateTime.of(2020,1,1,1,1));
            userUploadViewDTO.setValidityEndTime(LocalDateTime.now());
            userUploadViewDTO.setUserGroupName("user group name");
            userUploadViewDTO.setRole("roleName");
            resultList.add(userUploadViewDTO);
            String userId = "user id";
            sessionUtilMockedStatic.when(SessionUtil::getUserId).thenReturn(userId);
            List<Role> roles = new ArrayList<>();
            Role role = new Role();
            role.setName("roleName");
            role.setId(1);
            roles.add(role);
            when(roleMapper.selectList(any())).thenReturn(roles);
            when(userGroupMapper.selectOne(any())).thenReturn(null);
            KpUserServiceImpl kpUserServiceSpy = spy(this.kpUserService);
            doNothing().when(kpUserServiceSpy).updateExcelStatusCellValue(any(),any());
            assertEquals(0,kpUserServiceSpy.importUploadBatchAddUser(resultList,"11.xlsx"));
        }

    }

    /**
     * Method under test: {@link KpUserServiceImpl#updateExcelStatusCellValue(String, HashMap)}
     */
    @Test
    void updateExcelStatusCellValue() throws NoSuchFieldException, FileNotFoundException {
        /*String filePath = "111.xlsx";
        HashMap<String, String> statusAndMessageMap = new HashMap<>();
        statusAndMessageMap.put("M1","Success");
        statusAndMessageMap.put("N1","");
        Field field = KpUserServiceImpl.class.getDeclaredField("ossPath");
        field.setAccessible(true);
        ReflectionUtils.setField(field,kpUserService,"D://");

        Sheet sheet = mock(Sheet.class);
        HSSFRow row = mock(HSSFRow.class);
        HSSFCell cell = mock(HSSFCell.class);
        File file = mock(File.class,withSettings().useConstructor("D://" + File.separator + filePath));
        try (MockedConstruction<FileInputStream> fileInputStreamMockedConstruction = mockConstruction(FileInputStream.class)) {
            FileInputStream fileInputStream = new FileInputStream(file);
            XSSFWorkbook workbook = mock(XSSFWorkbook.class, withSettings().useConstructor(fileInputStream));
        }
        try (MockedConstruction<XSSFWorkbook> xSSFWorkbookMockedConstruction = mockConstruction(XSSFWorkbook.class, (mock, context) -> {
            when(mock.getSheetAt(0)).thenReturn((XSSFSheet) sheet);
            when(sheet.getRow(1)).thenReturn(row);
            when(row.getCell(0)).thenReturn(cell);
        })) {
        }
        kpUserService.updateExcelStatusCellValue(filePath,statusAndMessageMap);*/
    }

    /**
     * Method under test: {@link KpUserServiceImpl#setupCellValue(Sheet, String, CellStyle)}
     */
    @Test
    void setupCellValue() {
        /*CellStyle style = mock(CellStyle.class);
        HSSFSheet sheet = mock(HSSFSheet.class);
        HSSFRow row = mock(HSSFRow.class);
        when(sheet.getRow(any())).thenReturn(row);
        when(row.getCell(any())).thenReturn(null);
        HSSFCell cell = mock(HSSFCell.class);
        when(row.createCell(any())).thenReturn(cell);
        doNothing().when(cell).setCellStyle(style);
        assertEquals(cell,kpUserService.setupCellValue(sheet,"addressKey",style));*/
    }

    /**
     * when userIds is empty
     * Method under test: {@link KpUserServiceImpl#getUserIdsByTenantId(String, Set)}
     */
    @Test
    void getUserIdsByTenantId() {
        Set<String> set = kpUserService.getUserIdsByTenantId("tenant id", null);
        assertEquals(0,set.size());
    }

    /**
     * when userIds is not empty
     * Method under test: {@link KpUserServiceImpl#getUserIdsByTenantId(String, Set)}
     */
    @Test
    void getUserIdsByTenantId1() {
        String tenantId = "tenant id";
        Set<String> ids = new HashSet<>();
        ids.add("11");
        when(userService.getUserIdsByTenantId(tenantId,ids)).thenReturn(ids);
        Set<String> set = kpUserService.getUserIdsByTenantId(tenantId, ids);
        assertEquals(ids.toString(),set.toString());
    }

    /**
     * when userIds is empty
     * Method under test: {@link KpUserServiceImpl#updateMyselfPassword(UserSelfPasswordUpdateDTO)}
     */
    @Test
    void updateMyselfPassword() {
        try (MockedStatic<SessionUtil> sessionUtilMockedStatic = mockStatic(SessionUtil.class)) {
            UserSession userSession = new UserSession();
            userSession.setTenantId("tenant id");
            sessionUtilMockedStatic.when(SessionUtil::getUserSession).thenReturn(userSession);
            UserSelfPasswordUpdateDTO userPasswordDTO = new UserSelfPasswordUpdateDTO();
        //    doNothing().when(userService).changePassword(userSession.getId(),userPasswordDTO.getNewPassword(),userPasswordDTO.getOldPassword(),null);
            assertDoesNotThrow(()->kpUserService.updateMyselfPassword(userPasswordDTO));
        }
    }

    /**
     * when the permission list is empty
     * Method under test: {@link KpUserServiceImpl#menus()}
     */
    @Test
    void menus() {
        try (MockedStatic<SessionUtil> sessionUtilMockedStatic = mockStatic(SessionUtil.class)) {
            String userId = "user id";
            sessionUtilMockedStatic.when(SessionUtil::getUserId).thenReturn(userId);
            sessionUtilMockedStatic.when(SessionUtil::getUserLevel).thenReturn(UserLevelEnum.USER);
            when(permissionService.getPermissions(userId,UserLevelEnum.USER)).thenReturn(null);
            assertTrue(CollectionUtil.isEmpty(kpUserService.menus()));
        }
    }

    /**
     * when the permission list is not empty
     * Method under test: {@link KpUserServiceImpl#menus()}
     */
    @Test
    void menus1() {
        try (MockedStatic<SessionUtil> sessionUtilMockedStatic = mockStatic(SessionUtil.class)) {
            String userId = "user id";
            sessionUtilMockedStatic.when(SessionUtil::getUserId).thenReturn(userId);
            sessionUtilMockedStatic.when(SessionUtil::getUserLevel).thenReturn(UserLevelEnum.USER);
            List<Permission> permissions = new ArrayList<>();
            Permission permission = new Permission();
            permission.setId(1);
            permission.setParentId(-1);
            permissions.add(permission);
            when(permissionService.getPermissions(userId,UserLevelEnum.USER)).thenReturn(permissions);
            assertDoesNotThrow(()->kpUserService.menus());
        }
    }
}