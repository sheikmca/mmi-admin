package sg.ncs.kp.admin.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @className ChannelGroupDTO
 * @author chen xi
 * @version 1.0.0
 * @date 2023-08-25
 */
@Getter
@Setter
@ToString
public class ChangeGroupDTO {
    @NotNull
    private List<String> userIds;
    @NotNull
    private Integer userGroupId;
}
