package sg.ncs.kp.admin.quartz;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import sg.ncs.kp.admin.pojo.AdminConstants;
import sg.ncs.kp.admin.pojo.WSMsgTypEnum;
import sg.ncs.kp.admin.service.KpUserOnlineService;

/**
 * 
 * @author Lai Yin BO
 * @date 2022/10/18 
 * @Description Remove session expired user
 */
@Component
public class KpUserOnlineScheduled {

    @Autowired
    private RedisTemplate<String,String> sessionRedisTemplate;

    @Autowired
    private DefaultTokenServices tokenStore;
    @Autowired
    private KpUserOnlineService kpUserOnlineService;
    /**
     * Perform the check every 5 minutes
     * Check whether the current system online user has expired in Redis
     * If it expires, you are considered logged out
     */
    @Scheduled(cron = "0 */1 * * * ?")
    public void checkOnline(){
        Map<Object,Object> onlineUserMap = sessionRedisTemplate.opsForHash().entries(AdminConstants.USER_ONLINE_REDIS_KEY);
        if (CollectionUtils.isEmpty(onlineUserMap)){
            return;
        }
        for (Entry<Object, Object> userOnlineEntry:onlineUserMap.entrySet()){
            OAuth2AccessToken oAuth2Authentication = tokenStore.readAccessToken((String)userOnlineEntry.getValue());
            if(Objects.isNull(oAuth2Authentication) || oAuth2Authentication.isExpired()) {
                kpUserOnlineService.userOffline((String)userOnlineEntry.getKey(),WSMsgTypEnum.EXPIRED_OFFLINE);
            }
        }
    }
}
