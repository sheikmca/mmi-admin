package sg.ncs.kp.admin.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @className AddUserMessageDTO
 * @author chen xi
 * @version 1.0.0
 * @date 2023-08-24
 */
@Getter
@Setter
@ToString
public class AddUserMessageDTO {
    private Integer index;
    private String status;
    private String message;
}
