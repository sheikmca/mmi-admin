package sg.ncs.kp.admin.pojo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

/**
 * @auther 
 * @date 2023/1/11
 * @description
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TenantVO {

    private String id;

    private String name;

    private int status = 1;

    private Integer maxUser;

    private String description;

    private int logicallyDelete = 1;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date validityStartTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date validityEndTime;

    private String createdId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createdDate;

    private String lastUpdatedId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date lastUpdatedDate;
}
