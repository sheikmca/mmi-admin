package sg.ncs.kp.admin.po;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

/**
 * @author Chen Xi
 * @date 2023-08-29
 */
@TableName("kp_role_workspace_mapping")
@Getter
@Setter
public class RoleWorkspaceMapping {
    @TableField("role_id")
    private Integer roleId;
    @TableField("workspace_id")
    private Integer workspaceId;

}
