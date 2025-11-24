package sg.ncs.kp.admin.dto;

import lombok.Getter;
import lombok.Setter;
import sg.ncs.kp.admin.pojo.WSMsgTypEnum;


/**
 * @author Wang Shujin
 * @date 2022/9/16 18:29
 */
@Getter
@Setter
public class WSMessageDTO {

    private WSMsgTypEnum type;
    private String message;

}
