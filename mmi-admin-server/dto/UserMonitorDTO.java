package sg.ncs.kp.admin.dto;

import lombok.Getter;
import lombok.Setter;
import sg.ncs.kp.uaa.common.dto.UserDTO;

import java.util.Date;

/**
 * @className UserMonitorDTO
 * @author chen xi
 * @version 1.0.0
 * @date 2023-07-26
 */

@Getter
@Setter
public class UserMonitorDTO extends UserDTO {
    private Date lastLogoutTime;
    private Boolean onlineStatus;
    private Long lastLogin;
    private Long lastLogout;
}
