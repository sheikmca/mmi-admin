package sg.ncs.kp.admin.service.impl;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import sg.ncs.kp.admin.dto.WSMessageDTO;
import sg.ncs.kp.admin.pojo.WSMsgTypEnum;
import sg.ncs.kp.admin.service.AsyncService;
import sg.ncs.kp.admin.util.NotificationUtil;
import sg.ncs.kp.common.i18n.util.MessageUtils;
import sg.ncs.kp.notification.pojo.NotificationConstants;

/**
 * @author Lai Yin BO
 * @date 2022/10/18 
 * @Description Send websocket message in async way
 */
@Slf4j
@Component
public class AsyncServiceImpl implements AsyncService {

    @Autowired
    private NotificationUtil notificationUtil;
    
    @Autowired
    private MessageUtils messageUtils;
    
    @Async("asyncExecutor")
    @Override
    public void sendMessage(WSMsgTypEnum wsMsgTypEnum,String userId) {
        try {
            WSMessageDTO wsMessageDTO = new WSMessageDTO();
            wsMessageDTO.setType(wsMsgTypEnum);
            wsMessageDTO.setMessage(messageUtils.getMessage(wsMsgTypEnum.getCode()));
            notificationUtil.sendQueueMsgToWebSocketExchange(wsMessageDTO, NotificationConstants.ADMIN_QUEUE_USER_STATUS, Arrays.asList(userId));
        } catch (Exception e) {
            log.error(e.getMessage(),e);
        }
    }
}
