package sg.ncs.kp.admin.dto;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

/**
 * @auther IVAN
 * @date 2022/9/15
 * @description
 */
@Getter
@Setter
public class UserSelfPasswordUpdateDTO {

    @NotBlank
    private String oldPassword;

    @NotBlank
    private String newPassword;
}
