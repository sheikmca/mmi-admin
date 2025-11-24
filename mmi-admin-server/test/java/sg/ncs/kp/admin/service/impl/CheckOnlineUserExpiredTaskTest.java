package sg.ncs.kp.admin.service.impl;

import static org.mockito.Mockito.mockStatic;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;

import sg.ncs.kp.admin.quartz.CheckOnlineUserExpiredTask;
import sg.ncs.kp.admin.service.AsyncService;
import sg.ncs.kp.uaa.server.mapper.UserMapper;
import sg.ncs.kp.uaa.server.po.User;

public class CheckOnlineUserExpiredTaskTest {
    @Mock
    private AsyncService asyncService;

    @Mock
    private UserMapper sysUserMapper;
    
    @Mock
    private RedisTemplate<String, String> sessionRedisTemplate;
    
    @InjectMocks
    private CheckOnlineUserExpiredTask checkOnlineUserExpiredTask;
    
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
    void runTest() {
        List<User> list = new ArrayList<>();
        User user = new User();
        user.setId("id1");
        list.add(user);
        Mockito.when(sysUserMapper.selectList(Mockito.any())).thenReturn(list);
        LambdaQueryWrapper wrapper = Mockito.mock(LambdaQueryWrapper.class);
        HashOperations hashOperations = Mockito.mock(HashOperations.class);
        Mockito.when(sessionRedisTemplate.opsForHash()).thenReturn(hashOperations);
        Set<Object> userOnline = new HashSet<>();
        userOnline.add("id1");
        Mockito.when(hashOperations.keys(Mockito.any())).thenReturn(userOnline);
        Mockito.when(wrapper.select(Mockito.any(SFunction.class))).thenReturn(wrapper);
        try (MockedStatic<Wrappers> wrappersMockedStatic = mockStatic(Wrappers.class)) {
            wrappersMockedStatic.when(Wrappers::lambdaQuery).thenReturn(wrapper);
            Assertions.assertDoesNotThrow(()-> checkOnlineUserExpiredTask.run());
        }
    }

}
