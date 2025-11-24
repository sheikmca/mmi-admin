package sg.ncs.kp.admin.pojo;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WorkstationStatusDTO {
    private String name;

    // 1:online 2:offline 3:disconnected
    private Integer status;

    private String location;
}
