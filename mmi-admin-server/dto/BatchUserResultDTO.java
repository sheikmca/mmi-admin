package sg.ncs.kp.admin.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

/**
 * @className BatchUserResultDTO
 * @author chen xi
 * @version 1.0.0
 * @date 2023-08-24
 */
@Getter
@Setter
@ToString
public class BatchUserResultDTO {
    private Integer total;

    private Integer success;

    private Integer fail;

    private List<AddUserMessageDTO> messages;

}
