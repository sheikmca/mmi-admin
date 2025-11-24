package sg.ncs.kp.admin.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @className AssignRoleDTO
 * @author chen xi
 * @version 1.0.0
 * @date 2023-08-24
 */
@Getter
@Setter
@ToString
public class AssignRoleDTO {
    @NotNull
    private List<String> userIds;
    @NotNull
    private Integer roleId;
}
