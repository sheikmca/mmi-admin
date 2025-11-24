package sg.ncs.kp.admin.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * description: Assign User to Group
 * @author Wang Shujin
 * @date 2022/8/24 15:25
 */
@Getter
@Setter
public class AssignUserDTO {

    private Integer groupId;

    private List<String> userIds;

}
