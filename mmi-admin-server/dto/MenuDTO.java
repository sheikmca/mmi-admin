package sg.ncs.kp.admin.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * @auther IVAN
 * @date 2022/9/30
 * @description
 */
@Getter
@Setter
public class MenuDTO {

    private String authorityKey;

    private String uri;

    private List<MenuDTO> subs;
}
