package sg.ncs.kp.admin.service.impl;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;

import sg.ncs.kp.admin.quartz.KpUserOnlineScheduled;
import sg.ncs.kp.admin.service.KpUserOnlineService;

class KpUserOnlineScheduledTest {
    @Mock
    private RedisTemplate<String,String> sessionRedisTemplate;
    @Mock
    private DefaultTokenServices tokenStore;
    @Mock
    private KpUserOnlineService kpUserOnlineService;
    
    @InjectMocks
    private KpUserOnlineScheduled kpUserOnlineScheduled;
    
    private AutoCloseable autoCloseable;
    
    @Test
    @SuppressWarnings("rawtypes")
    void checkOnlineTest() {
        HashOperations hashOperations = Mockito.mock(HashOperations.class);
        Mockito.when(sessionRedisTemplate.opsForHash()).thenReturn(hashOperations);
        Map<Object,Object> onlineUserMap = new HashMap<>();
        onlineUserMap.put("userId", "token1");
        Mockito.when(hashOperations.entries(Mockito.anyString())).thenReturn(onlineUserMap);
        OAuth2AccessToken oAuth2Authentication = Mockito.mock(OAuth2AccessToken.class);
        Mockito.when(tokenStore.readAccessToken(Mockito.anyString())).thenReturn(oAuth2Authentication);
        Mockito.when(oAuth2Authentication.isExpired()).thenReturn(true);
        Assertions.assertDoesNotThrow(()->kpUserOnlineScheduled.checkOnline());
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
