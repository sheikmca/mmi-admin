package sg.ncs.kp.admin.dto;

import com.alibaba.fastjson.JSONArray;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @className ADUserDTO
 * @author chen xi
 * @version 1.0.0
 * @date 2023-07-10
 */

@Getter
@Setter
@ToString
public class ADUserDTO {
    private String userName;
    private JSONArray userInfo;
}
