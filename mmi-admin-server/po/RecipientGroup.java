package sg.ncs.kp.admin.po;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import sg.ncs.kp.common.mybaits.base.po.AutoIncrementPo;

/**
 * @author Wang Shujin
 * @date 2022/8/24 14:45
 */
@Getter
@Setter
@ToString
@TableName("kp_recipient_group")
public class RecipientGroup extends AutoIncrementPo {

    public static final String NAME_COLUMN = "name";
    public static final String TENANT_ID_COLUMN = "tenant_id";

    @TableField(NAME_COLUMN)
    private String name;

    @TableField(TENANT_ID_COLUMN)
    private String tenantId;

}

