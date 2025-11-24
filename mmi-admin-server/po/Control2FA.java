package sg.ncs.kp.admin.po;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;
import sg.ncs.kp.common.mybaits.base.po.AutoIncrementPo;
import sg.ncs.kp.uaa.common.dto.BaseDTO;

/**
 * @className Control2FA
 * @author chen xi
 * @version 1.0.0
 * @date 2023-07-11
 */

@TableName("kp_control_2fa")
@Getter
@Setter
public class Control2FA extends AutoIncrementPo {
    @TableField("role_id")
    private Integer roleId;
    @TableField("status")
    private Integer status;
}
