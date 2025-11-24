package sg.ncs.kp.admin.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * @auther IVAN
 * @date 2022/9/8
 * @description
 */
@Getter
@Setter
public class LoginLogDTO {

    private String userId;

    private String userName;

    private String ip;

    private String phone;

    private String email;

    private String userGroupName;

    private Date loginTime;
}
