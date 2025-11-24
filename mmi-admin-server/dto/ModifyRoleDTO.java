package sg.ncs.kp.admin.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import sg.ncs.kp.uaa.common.dto.RoleAddUpdateDTO;

/**
 * @className ModifyRoleDTO
 * @author chen xi
 * @version 1.0.0
 * @date 2023-07-11
 */

@Getter
@Setter
@ToString
public class ModifyRoleDTO extends RoleAddUpdateDTO {
    private Boolean control2faStatus;
}
