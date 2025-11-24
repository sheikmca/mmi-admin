package sg.ncs.kp.admin.quartz;

import cn.hutool.core.bean.BeanUtil;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import sg.ncs.kp.admin.mapper.OperateLogMapper;
import sg.ncs.kp.admin.po.OperateLog;
import sg.ncs.kp.uaa.client.common.UaaConstant;
import sg.ncs.kp.uaa.client.dto.OperateLogDTO;
import sg.ncs.kp.uaa.common.dto.UserDTO;
import sg.ncs.kp.uaa.server.po.UserGroup;
import sg.ncs.kp.uaa.server.service.UserGroupService;
import sg.ncs.kp.uaa.server.service.UserService;

import java.util.Date;

/**
 * @auther IVAN
 * @date 2022/9/8
 * @description
 */
@Component
public class OperateLogConsumer {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    @Autowired
    private UserService userService;
    @Autowired
    private OperateLogMapper operateLogMapper;
    @Autowired
    private UserGroupService userGroupService;

    @Scheduled(cron = "*/5 * * * * ?")
    public void run() {
        Long size = redisTemplate.opsForList().size(UaaConstant.DEFAULT_LOG_QUEUE_KEY);
        if (0L == size) {
            return;
        }

        int eachTime = 0;
        String object = null;
        while (eachTime < 3) {
            object = redisTemplate.opsForList().rightPop(UaaConstant.DEFAULT_LOG_QUEUE_KEY);
            if (null == object) {
                eachTime++;
                try {
                    Thread.sleep(1_000);
                } catch (InterruptedException e) {
                }
                continue;
            }
            OperateLogDTO ospLog = JSONObject.parseObject(object, OperateLogDTO.class);
            save(ospLog);
        }

    }


    private void save(OperateLogDTO operateLogDTO) {
        OperateLog operateLog = BeanUtil.copyProperties(operateLogDTO, OperateLog.class);
        UserDTO userDTO = userService.get(operateLog.getUserId());
        if (null != userDTO) {
            operateLog.setLogTime(new Date(operateLogDTO.getLogTime()));
            operateLog.setUserName(userDTO.getUserName());
            operateLog.setPhone(userDTO.getPhone());
            operateLog.setEmail(userDTO.getEmail());
            UserGroup group = userGroupService.getByUserId(userDTO.getId());
            if (group != null) {
                operateLog.setUserGroupName(group.getName());
            }
            operateLogMapper.insert(operateLog);
        }
    }
}
