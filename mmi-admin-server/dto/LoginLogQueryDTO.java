package sg.ncs.kp.admin.dto;

import lombok.Getter;
import lombok.Setter;
import sg.ncs.kp.common.core.response.PageQuery;

import java.util.Date;

/**
 * @auther IVAN
 * @date 2022/9/8
 * @description
 */
@Getter
@Setter
public class LoginLogQueryDTO extends PageQuery {

    private String userName;

    private String userGroupName;

    private String ip;

    private String phone;

    private String email;

    private Date loginTimeBegin;

    private Date loginTimeEnd;
}
