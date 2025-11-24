package sg.ncs.kp.admin.service.impl;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.security.oauth2.provider.OAuth2Authentication;

import sg.ncs.kp.admin.notify.KpTokenUpdateNotify;
import sg.ncs.kp.admin.service.AsyncService;
import sg.ncs.kp.uaa.client.enums.OfflineEnum;
import sg.ncs.kp.uaa.client.session.UserSession;

public class KpTokenUpdateNotifyTest {
    @Mock
    private AsyncService asyncService;
    @InjectMocks
    private KpTokenUpdateNotify kpTokenUpdateNotify;
    private AutoCloseable autoCloseable;
    
    @BeforeEach
    void init() {
        autoCloseable = MockitoAnnotations.openMocks(this);
    }
    
    @AfterEach
    void release() throws Exception {
        autoCloseable.close();
    }
    
    @Test
    void offLineNotificationTest() {
        OAuth2Authentication authentication = Mockito.mock(OAuth2Authentication.class);
        UserSession userSession = new UserSession();
        userSession.setId("id1");
        Mockito.when(authentication.getPrincipal()).thenReturn(userSession);
        Assertions.assertDoesNotThrow(()-> kpTokenUpdateNotify.offLineNotification(authentication, OfflineEnum.FORCED));
    }
}
