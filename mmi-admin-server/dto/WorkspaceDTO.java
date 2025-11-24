package sg.ncs.kp.admin.dto;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * @author Chen Xi
 * @date 2023-07-25
 */
@Getter
@Setter
public class WorkspaceDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer id;
    @NotBlank
    private String name;
    private String frontSize;
    private String frontStyle;
    private String setType;
    private Integer bindingId;
    private String theme;
    private Integer trackNum;
    private String roiColor;
    private JSONObject eoSetting;
    private JSONObject nfoaSetting;
    private JSONObject dashboardSetting;
    @JsonFormat(
            pattern = "dd/MM/yyyy HH:mm:ss",
            timezone = "GMT+8"
    )
    private Date lastUpdatedDate;
    @JsonFormat(
            pattern = "dd/MM/yyyy HH:mm:ss",
            timezone = "GMT+8"
    )
    private Date createdDate;
    private List<Integer> roleIds;
}
