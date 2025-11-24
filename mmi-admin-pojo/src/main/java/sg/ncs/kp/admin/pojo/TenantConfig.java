package sg.ncs.kp.admin.pojo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

/**
 * @date 2022/10/19 10:23
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TenantConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;
    private Integer maxConcurrentInstance;
    private Integer maxVaCount;
    private Integer maxConcurrentChannel;
    private Integer maxCameraCount;
    private Integer cloudStorage;
    private Integer enableConfig;

}
