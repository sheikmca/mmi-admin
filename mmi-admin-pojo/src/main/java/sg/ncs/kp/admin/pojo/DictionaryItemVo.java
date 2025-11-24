package sg.ncs.kp.admin.pojo;

import java.util.Date;

import lombok.Getter;
import lombok.Setter;
/**
 * 
 * @date 2022/07/26 
 * @Description Dictionary Item Vo
 */
@Getter
@Setter
public class DictionaryItemVo {
    private Integer id;
    private Integer dictId;
    private String name;
    private String value;
    private String description;
    private Integer sortOrder;
    private Integer version;
    private String tenantId;
    private String createdId;
    private Date createdDate;
    private String lastUpdatedId;
    private Date lastUpdatedDate;
}
