package sg.ncs.kp.admin.po;

import java.util.Date;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * <p>
 * 
 * </p>
 *
 * @author IVAN
 * @since 2022-09-08
 */
@Getter
@Setter
@ToString
@TableName("kp_operate_log")
public class OperateLog {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id",type = IdType.AUTO)
    private Integer id;
    private String userId;
    private String tenantId;
    private String userName;
    private String module;
    private String requestData;
    private String responseData;
    private Date logTime;
    private String operateType;
    private String ip;
    private String phone;
    private String email;
    private String userGroupName;
}
