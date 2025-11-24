package sg.ncs.kp.admin.dto;

import com.alibaba.fastjson.JSONObject;
import lombok.Getter;
import lombok.Setter;

/**
 * @auther IVAN
 * @date 2022/8/25
 * @description
 */
@Getter
@Setter
public class UploadResultDTO {

    private Integer total;

    private Integer success;

    private Integer fail;

    private JSONObject failedMessage;
}
