package sg.ncs.kp.admin.pojo;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

/**
 * @className RoleUserDTO
 * @version 1.0.0
 * @date 2023-10-25
 */
@Getter
@Setter
@ToString
public class RoleUserDTO {
    private Long roleId;
    private List<String> userIds;
}
