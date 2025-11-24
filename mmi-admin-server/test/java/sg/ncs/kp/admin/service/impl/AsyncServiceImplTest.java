package sg.ncs.kp.admin.service.impl;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import sg.ncs.kp.admin.pojo.WSMsgTypEnum;
import sg.ncs.kp.admin.util.NotificationUtil;
import sg.ncs.kp.common.i18n.util.MessageUtils;

class AsyncServiceImplTest {
    private AutoCloseable autoCloseable;
    @Mock
    private NotificationUtil notificationUtil;
    
    @Mock
    private MessageUtils messageUtils;
    
    @InjectMocks
    private AsyncServiceImpl asyncService;
    
    @Test
    void sendMessageTest() {
        Mockito.when(messageUtils.getMessage(Mockito.any())).thenReturn("success");
        Assertions.assertDoesNotThrow(()-> asyncService.sendMessage(WSMsgTypEnum.EXPIRED_OFFLINE, "userId"));
    }
    
    
    @BeforeEach
    void init() {
        autoCloseable = MockitoAnnotations.openMocks(this);
    }
    @AfterEach
    void release() throws Exception {
        autoCloseable.close();
    }
}
