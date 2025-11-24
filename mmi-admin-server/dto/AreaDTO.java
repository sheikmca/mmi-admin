package sg.ncs.kp.admin.dto;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * @author Duan Ran
 * @date 2022/8/23
 */
@Getter
@Setter
public class AreaDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer id;
    private String name;
    private Integer parentId;
    private String description;
}
