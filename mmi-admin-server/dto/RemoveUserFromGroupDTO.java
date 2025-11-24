package sg.ncs.kp.admin.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * @author Wang Shujin
 * @date 2022/8/24 15:43
 */
@Getter
@Setter
public class RemoveUserFromGroupDTO {

    private Integer groupId;

    private List<String> userIds;

}
