package sg.ncs.kp.admin.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @className ADLogin2FADTO
 * @author chen xi
 * @version 1.0.0
 * @date 2023-07-12
 */

@Getter
@Setter
@ToString
public class ADLogin2FADTO extends ADLoginDTO {
    private String loginAction;
    private String code2FA;
    private String direction;
}
