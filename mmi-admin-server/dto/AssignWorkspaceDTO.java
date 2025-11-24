package sg.ncs.kp.admin.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @className AssignWorkspaceDTO
 * @author chen xi
 * @version 1.0.0
 * @date 2023-08-30
 */
@Getter
@Setter
@ToString
public class AssignWorkspaceDTO {
    @NotNull
    private Integer id;
    private List<Integer> roleIds;
}
