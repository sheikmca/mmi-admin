package sg.ncs.kp.admin.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

/**
 * @className CheckADUserResponseDTO
 * @author chen xi
 * @version 1.0.0
 * @date 2023-07-10
 */

@Getter
@Setter
@ToString
public class CheckADUserResponseDTO {
    private List<ADUserDTO> userList;
    private Integer num;
}
