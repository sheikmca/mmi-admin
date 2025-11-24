package sg.ncs.kp.admin.dto;
/**
 * @className UserMonitorQueryDTO
 * @author chen xi
 * @version 1.0.0
 * @date 2023-07-25
 */

import lombok.Getter;
import lombok.Setter;
import sg.ncs.kp.uaa.common.dto.UserQueryDTO;

@Getter
@Setter
public class UserMonitorQueryDTO extends UserQueryDTO {
    private Boolean onlineStatus = null;
}
