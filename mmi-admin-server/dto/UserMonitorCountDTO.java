package sg.ncs.kp.admin.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @className UserMonitorCountDTO
 * @author chen xi
 * @version 1.0.0
 * @date 2023-07-26
 */
@Getter
@Setter
@ToString
public class UserMonitorCountDTO {
    private Long totalUser;
    private Long onlineUser;
    private Long offlineUser;
}
