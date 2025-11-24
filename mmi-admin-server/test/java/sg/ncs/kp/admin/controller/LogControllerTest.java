package sg.ncs.kp.admin.controller;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import sg.ncs.kp.admin.dto.LoginLogQueryDTO;
import sg.ncs.kp.admin.dto.OperateLogQueryDTO;
import sg.ncs.kp.admin.po.OperateLog;
import sg.ncs.kp.admin.service.LogService;
import sg.ncs.kp.common.core.response.PageResult;
import sg.ncs.kp.common.i18n.util.MessageUtils;
import sg.ncs.kp.uaa.common.dto.UserDTO;
import sg.ncs.kp.uaa.common.dto.UserQueryDTO;
import sg.ncs.kp.uaa.server.po.LoginLog;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @auther IVAN
 * @date 2022/9/9
 * @description
 */
class LogControllerTest {

    @InjectMocks
    private LogController logController;

    @Mock
    private LogService logService;

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

    /**
     * Method under test: {@link LogController#loginList(LoginLogQueryDTO)} 
     */
    @Test
    void loginList() {
        LoginLogQueryDTO queryDTO = new LoginLogQueryDTO();
        Page<LoginLog> page = new Page<>(1, 10);
        when(logService.loginRecords(queryDTO)).thenReturn(page);
        PageResult result = mock(PageResult.class);
        when(messageUtils.pageResult((int) page.getCurrent(), (int) page.getSize(),
                page.getTotal(), BeanUtil.copyToList(page.getRecords(), UserDTO.class))).thenReturn(result);
        assertEquals(result,logController.loginList(queryDTO));
    }

    /**
     * Method under test: {@link LogController#operateList(OperateLogQueryDTO)}
     */
    @Test
    void operateList() {
        OperateLogQueryDTO queryDTO = new OperateLogQueryDTO();
        Page<OperateLog> page = new Page<>(1, 10);
        when(logService.operateRecords(queryDTO)).thenReturn(page);
        PageResult result = mock(PageResult.class);
        when(messageUtils.pageResult((int) page.getCurrent(), (int) page.getSize(),
                page.getTotal(), BeanUtil.copyToList(page.getRecords(), UserDTO.class))).thenReturn(result);
        assertEquals(result,logController.operateList(queryDTO));
    }
}