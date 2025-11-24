package sg.ncs.kp.admin.po;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

/**
 * @author Duan Ran
 * @date 2022/8/23
 */
@TableName("kp_role_area_mapping")
@Getter
@Setter
public class RoleAreaMapping {
    @TableField("role_id")
    private Integer roleId;
    @TableField("area_id")
    private Integer areaId;

}
