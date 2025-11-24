package sg.ncs.kp.admin.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.stereotype.Service;

import sg.ncs.kp.admin.pojo.AdminConstants;
import sg.ncs.kp.admin.pojo.WSMsgTypEnum;
import sg.ncs.kp.admin.service.AsyncService;
import sg.ncs.kp.admin.service.KpUserOnlineService;
import sg.ncs.kp.uaa.server.service.LoginService;

@Service
public class KpUserOnlineServiceImpl implements KpUserOnlineService{

    @Autowired
    private AsyncService asycService;
    
    @Autowired
    private RedisTemplate<String, String> sessionRedisTemplate;

    @Autowired
    private LoginService loginService;

    @Autowired
    private DefaultTokenServices defaultTokenServices;

    @Override
    public void userOnline(String userId, String accessToken) {
        sessionRedisTemplate.opsForHash().put(AdminConstants.USER_ONLINE_REDIS_KEY, userId, accessToken);
    }

    @Override
    public void userOffline(String userId,WSMsgTypEnum type) {
        sessionRedisTemplate.opsForHash().delete(AdminConstants.USER_ONLINE_REDIS_KEY, userId);
        loginService.updateLogoutTime(userId);
        asycService.sendMessage(type, userId);
    }

    @Override
    public void forceLogout(String userId){
        Object token = sessionRedisTemplate.opsForHash().get(AdminConstants.USER_ONLINE_REDIS_KEY, userId);
        if(token != null) {
            defaultTokenServices.revokeToken(token.toString());
            this.userOffline(userId,WSMsgTypEnum.FORCED_OFFLINE);
        }
    }
    
}
