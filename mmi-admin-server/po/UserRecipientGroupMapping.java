package sg.ncs.kp.admin.po;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author Duan Ran
 * @date 2022/8/23
 */
@TableName("kp_user_recipient_group_mapping")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserRecipientGroupMapping {
    @TableField("user_id")
    private String userId;
    @TableField("recipient_group_id")
    private Integer recipientGroupId;

}
