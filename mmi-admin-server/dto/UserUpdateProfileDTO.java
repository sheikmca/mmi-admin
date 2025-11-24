package sg.ncs.kp.admin.dto;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

/**
 * @auther IVAN
 * @date 2022/8/26
 * @description
 */
@Getter
@Setter
public class UserUpdateProfileDTO {

    @NotBlank(message = "is null")

    private String id;

    @NotBlank(message = "is empty")

    private String fullName;


    private String phone;


    private String email;

    private String countryCode;
}
