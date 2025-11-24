package sg.ncs.kp.admin.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.NotBlank;

/**
 * @className ResetPasswordDTO
 * @author chen xi
 * @version 1.0.0
 * @date 2023-09-25
 */
@Getter
@Setter
@ToString
public class ResetPasswordDTO {
    @NotBlank
    private String id;
    private String password;
}
