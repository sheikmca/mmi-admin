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
import sg.ncs.kp.common.i18n.util.MessageUtils;
import sg.ncs.kp.uaa.client.session.UserSession;
import sg.ncs.kp.uaa.client.util.SessionUtil;
import sg.ncs.kp.uaa.common.dto.AgencyDTO;
import sg.ncs.kp.uaa.common.dto.AgencyTenantDTO;
import sg.ncs.kp.uaa.common.dto.UserModifyDTO;
import sg.ncs.kp.uaa.common.vo.TenantVO;
import sg.ncs.kp.uaa.server.service.TenantService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;


class AgencyControllerTest {

    @InjectMocks
    AgencyController agencyController;

    @Mock
    TenantService tenantService;

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
    void addAgency() {
        try (MockedStatic<SessionUtil> sessionUtilMockedStatic = mockStatic(SessionUtil.class)) {
            UserSession userSession = new UserSession();
            userSession.setTenantId("tenant id");
            userSession.setId("test");
            sessionUtilMockedStatic.when(SessionUtil::getUserSession).thenReturn(userSession);
            AgencyDTO agencyDTO = new AgencyDTO();
            agencyDTO.setTenant(new AgencyTenantDTO());
            agencyDTO.setUser(new UserModifyDTO());
            Assertions.assertDoesNotThrow(() -> agencyController.addAgency(agencyDTO));
        }
    }

    @Test
    void updateAgency() {
        try (MockedStatic<SessionUtil> sessionUtilMockedStatic = mockStatic(SessionUtil.class)) {
            sessionUtilMockedStatic.when(SessionUtil::getUserId).thenReturn("test");
            AgencyDTO agencyDTO = new AgencyDTO();
            agencyDTO.setTenant(new AgencyTenantDTO());
            agencyDTO.setUser(new UserModifyDTO());
            Assertions.assertDoesNotThrow(() -> agencyController.updateAgency(agencyDTO));
        }
    }

    @Test
    void getAgencyDTODetail() {
        Assertions.assertDoesNotThrow(() -> agencyController.getAgencyDTODetail("test","test"));
    }

    @Test
    void updateAgencyStatus() {
        Assertions.assertDoesNotThrow(() -> agencyController.updateAgencyStatus("test",1));
    }

    @Test
    void getAgencyList() {
        Page<TenantVO> page = new Page<>(1, 10);
        when(tenantService.selectAgencyList(any())).thenReturn(page);
        Assertions.assertDoesNotThrow(() -> agencyController.getAgencyList(any()));
    }

    @Test
    void deleteAgencyList() {
        Assertions.assertDoesNotThrow(() -> agencyController.deleteAgencyList("3"));
    }
}