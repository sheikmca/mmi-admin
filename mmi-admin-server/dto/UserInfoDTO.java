package sg.ncs.kp.admin.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import sg.ncs.kp.uaa.common.dto.UserModifyDTO;

/**
 * @className UserInfoDTO
 * @author chen xi
 * @version 1.0.0
 * @date 2023-08-24
 */
@Getter
@Setter
@ToString
public class UserInfoDTO extends UserModifyDTO {
    private Integer index;
}
