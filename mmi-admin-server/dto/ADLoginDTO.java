package sg.ncs.kp.admin.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * @className ADUserDTO
 * @author chen xi
 * @version 1.0.0
 * @date 2023-07-10
 */

@Getter
@Setter
@ToString
public class ADLoginDTO {
    private String tenantName;
    @NotBlank
    private String param;
    @NotBlank
    private String password;
    @NotBlank
    private String adName;
}
