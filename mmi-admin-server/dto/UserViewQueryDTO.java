package sg.ncs.kp.admin.dto;

import lombok.Getter;
import lombok.Setter;
import sg.ncs.kp.common.core.response.PageQuery;

import javax.validation.constraints.NotNull;

/**
 * @auther IVAN
 * @date 2022/8/25
 * @description
 */
@Getter
@Setter
public class UserViewQueryDTO extends PageQuery {

    @NotNull
    private String path;
}
