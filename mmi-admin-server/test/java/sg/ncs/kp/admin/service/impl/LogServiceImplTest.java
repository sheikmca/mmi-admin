package sg.ncs.kp.admin.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import sg.ncs.kp.admin.dto.LoginLogQueryDTO;
import sg.ncs.kp.admin.dto.OperateLogQueryDTO;
import sg.ncs.kp.admin.mapper.OperateLogMapper;
import sg.ncs.kp.admin.po.OperateLog;
import sg.ncs.kp.uaa.client.util.SessionUtil;
import sg.ncs.kp.uaa.common.enums.UserLevelEnum;
import sg.ncs.kp.uaa.server.mapper.LoginLogMapper;
import sg.ncs.kp.uaa.server.po.LoginLog;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @auther IVAN
 * @date 2022/9/9
 * @description
 */
class LogServiceImplTest {

    @InjectMocks
    private LogServiceImpl logService;

    @Mock
    private LoginLogMapper loginLogMapper;

    @Mock
    private OperateLogMapper operateLogMapper;

   private AutoCloseable autoCloseable;

   @BeforeEach
   void setUp() {
      autoCloseable = MockitoAnnotations.openMocks(this);
   }

   @AfterEach
   void release() throws Exception {
      autoCloseable.close();
   }

   /**
    * Method under test: {@link LogServiceImpl#loginRecords(LoginLogQueryDTO)}
    */
    @Test
    void loginRecords() {
        try (MockedStatic<SessionUtil> sessionUtilMockedStatic = Mockito.mockStatic(SessionUtil.class)) {
            sessionUtilMockedStatic.when(SessionUtil::getUserLevel).thenReturn(UserLevelEnum.TENANT_ADMIN);
            Page<LoginLog> page = new Page<>(1, 10);
            Mockito.when(loginLogMapper.selectPage(Mockito.any(),Mockito.any())).thenReturn(page);
            assertEquals(page,logService.loginRecords(Mockito.mock(LoginLogQueryDTO.class)));
        }
    }

    /**
     * when the current user is not tenant admin
     * Method under test: {@link LogServiceImpl#loginRecords(LoginLogQueryDTO)}
     */
    @Test
    void loginRecords1() {
        try (MockedStatic<SessionUtil> sessionUtilMockedStatic = Mockito.mockStatic(SessionUtil.class)) {
            sessionUtilMockedStatic.when(SessionUtil::getUserLevel).thenReturn(UserLevelEnum.USER);
            Page<LoginLog> page = new Page<>(1, 10);
            LoginLogQueryDTO queryDTO = new LoginLogQueryDTO();
            assertEquals(JSONObject.toJSONString(page),JSONObject.toJSONString(logService.loginRecords(queryDTO)));
        }
    }

   /**
    * Method under test: {@link LogServiceImpl#operateRecords(OperateLogQueryDTO)}
    */
    @Test
    void operateRecords() {
        try (MockedStatic<SessionUtil> sessionUtilMockedStatic = Mockito.mockStatic(SessionUtil.class)) {
            sessionUtilMockedStatic.when(SessionUtil::getUserLevel).thenReturn(UserLevelEnum.TENANT_ADMIN);
            Page<OperateLog> page = new Page<>(1, 10);
            Mockito.when(operateLogMapper.selectPage(Mockito.any(),Mockito.any())).thenReturn(page);
            assertEquals(page,logService.operateRecords(Mockito.mock(OperateLogQueryDTO.class)));
        }
    }

    /**
     * when the current user is not tenant admin
     * Method under test: {@link LogServiceImpl#loginRecords(LoginLogQueryDTO)}
     */
    @Test
    void operateRecords2() {
        try (MockedStatic<SessionUtil> sessionUtilMockedStatic = Mockito.mockStatic(SessionUtil.class)) {
            sessionUtilMockedStatic.when(SessionUtil::getUserLevel).thenReturn(UserLevelEnum.USER);
            Page<OperateLog> page = new Page<>(1, 10);
            OperateLogQueryDTO queryDTO = new OperateLogQueryDTO();
            assertEquals(JSONObject.toJSONString(page),JSONObject.toJSONString(logService.operateRecords(queryDTO)));
        }
    }
}