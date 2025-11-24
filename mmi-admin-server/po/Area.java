package sg.ncs.kp.admin.po;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;
import sg.ncs.kp.uaa.common.dto.BaseDTO;

/**
 * @author Duan Ran
 * @date 2022/8/23
 */
@TableName("kp_area")
@Getter
@Setter
public class Area extends BaseDTO {
    private static final long serialVersionUID = 1L;

    @TableId("id")
    private Integer id;

    @TableField("tenant_id")
    private Integer tenantId;

    @TableField("name")
    private String name;

    @TableField("parent_id")
    private Integer parentId;

    @TableField("description")
    private String description;

    @TableField("sort")
    private Integer sort;
}
