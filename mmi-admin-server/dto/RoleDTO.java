package sg.ncs.kp.admin.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import sg.ncs.kp.uaa.common.vo.RoleVO;

/**
 * @className RoleDTO
 * @author chen xi
 * @version 1.0.0
 * @date 2023-07-11
 */

@Getter
@Setter
@ToString
public class RoleDTO extends RoleVO {
    private Boolean control2faStatus = false;
}
