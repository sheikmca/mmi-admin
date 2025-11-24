package sg.ncs.kp.admin.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import sg.ncs.kp.admin.enums.DirectionEnum;
import sg.ncs.kp.uaa.common.dto.LoginDTO;

/**
 * @className Login2FADTO
 * @author chen xi
 * @version 1.0.0
 * @date 2023-07-11
 */

@Getter
@Setter
@ToString
public class Login2FADTO extends LoginDTO {
    private String loginAction;
    private String code2FA;
    private String direction;
}
