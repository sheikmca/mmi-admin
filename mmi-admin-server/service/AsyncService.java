package sg.ncs.kp.admin.service;

import sg.ncs.kp.admin.pojo.WSMsgTypEnum;

/**
 * @auther IVAN
 * @date 2022/4/27
 * @description
 */
public interface AsyncService {

    void sendMessage(WSMsgTypEnum wsMsgTypEnum,String userId);
}
