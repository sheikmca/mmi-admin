package sg.ncs.kp.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import sg.ncs.kp.admin.po.Workspace;

import java.util.List;

/**
 * @className WorkspaceMapper
 * @author chen xi
 * @version 1.0.0
 * @date 2023-07-10
 */
public interface WorkspaceMapper extends BaseMapper<Workspace> {
    Integer selectCountByRoleId(@Param("name")String name, @Param("roleId")Integer roleId,@Param("tenantId")String tenantId, @Param("bindingId")Integer bindingId, @Param("workspaceId")Integer workspaceId);
    List<Workspace> list(@Param("name")String name, @Param("roleId")Integer roleId, @Param("setType")Integer setType,@Param("tenantId")String tenantId);
}
