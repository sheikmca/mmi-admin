package sg.ncs.kp.admin.util;

import java.util.Collection;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import sg.ncs.kp.notification.pojo.NotificationConstants;
import sg.ncs.kp.notification.pojo.NotificationMessage;

@Component
public class NotificationUtil {
    @Autowired
    private RabbitTemplate rabbitTemplate;
    
    @Value("${spring.rabbitmq.userName}")
    private String userName; 

    public void sendTopicMsgToWebSocketExchange(Object payload,String topic) {
        NotificationMessage notificationMessage = new NotificationMessage(topic, payload);
        rabbitTemplate.convertAndSend(userName+NotificationConstants.NOTIFICATION_EXCHANGE, NotificationConstants.NOTIFICATION_ROUTINGKEY, notificationMessage);
    }
    
    public void sendQueueMsgToWebSocketExchange(Object payload,String queue, Collection<String> userIds) {
        NotificationMessage notificationMessage = new NotificationMessage(queue, userIds,payload);
        rabbitTemplate.convertAndSend(userName+NotificationConstants.NOTIFICATION_EXCHANGE, NotificationConstants.NOTIFICATION_ROUTINGKEY, notificationMessage);
    }
}
