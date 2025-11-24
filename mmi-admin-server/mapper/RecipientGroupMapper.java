package sg.ncs.kp.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import sg.ncs.kp.admin.po.RecipientGroup;
import sg.ncs.kp.uaa.common.dto.UserDTO;
import sg.ncs.kp.uaa.common.dto.UserGroupAssignUserDTO;
import sg.ncs.kp.uaa.server.po.User;

/**
 * @author Wang Shujin
 * @date 2022/8/24 14:45
 */
@Mapper
public interface RecipientGroupMapper extends BaseMapper<RecipientGroup> {

    IPage<UserDTO> assignedUserList(Page<User> page, @Param("id") Integer id, @Param("dto") UserGroupAssignUserDTO dto);
    IPage<UserDTO> notAssignedUserList(Page<User> page, @Param("id") Integer id, @Param("dto") UserGroupAssignUserDTO dto);

}
