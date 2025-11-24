package sg.ncs.kp.admin.quartz;

import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;

import lombok.extern.slf4j.Slf4j;
import sg.ncs.kp.admin.pojo.AdminConstants;
import sg.ncs.kp.admin.pojo.WSMsgTypEnum;
import sg.ncs.kp.admin.service.AsyncService;
import sg.ncs.kp.uaa.server.mapper.UserMapper;
import sg.ncs.kp.uaa.server.po.User;

/**
 * 
 * @author Lai Yin BO
 * @date 2022/10/18 
 * @Description Check Online user is expired or not.
 */
@Slf4j
@Component
public class CheckOnlineUserExpiredTask {

    @Autowired
    private AsyncService asyncService;

    @Autowired
    private UserMapper sysUserMapper;
    
    @Autowired
    private RedisTemplate<String, String> sessionRedisTemplate;

    @Scheduled(cron = "0 0/5 * * * ?")
    public void run(){
        log.info("start the task that check online user whether is expired");
        List<User> list = sysUserMapper.selectList(Wrappers.<User>lambdaQuery()
                .select(User::getId)
                .le(User::getValidityEndTime,new Date()));
        Set<Object> users= sessionRedisTemplate.opsForHash().keys(AdminConstants.USER_ONLINE_REDIS_KEY);
        Set<String> set = list.stream().map(item -> item.getId()).filter(users::contains).collect(Collectors.toSet());
        for (String userId : set) {
            asyncService.sendMessage(WSMsgTypEnum.ACCOUNT_DISABLE,userId);
        }
    }

}
