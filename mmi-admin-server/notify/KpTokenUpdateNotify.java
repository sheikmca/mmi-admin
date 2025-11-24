package sg.ncs.kp.admin.notify;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;
import sg.ncs.kp.admin.pojo.WSMsgTypEnum;
import sg.ncs.kp.admin.service.AsyncService;
import sg.ncs.kp.uaa.client.enums.OfflineEnum;
import sg.ncs.kp.uaa.client.notify.TokenUpdateNotify;
import sg.ncs.kp.uaa.client.session.UserSession;

/**
 * @author Wang Shujin
 * @date 2022/9/16 18:21
 */
@Slf4j
@Service
public class KpTokenUpdateNotify implements TokenUpdateNotify {
    
    @Autowired
    private AsyncService asyncService;

    @Override
    public void offLineNotification(OAuth2Authentication authentication, OfflineEnum type) {
        if (OfflineEnum.FORCED.equals(type) && authentication.getPrincipal() instanceof UserSession) {
            
            String id = ((UserSession) authentication.getPrincipal()).getId();
            asyncService.sendMessage(WSMsgTypEnum.FORCED_OFFLINE, id);
            log.info("User:{} forced offline", id);
        }
    }
}
