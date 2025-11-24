package sg.ncs.kp.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;
import sg.ncs.kp.admin.dto.UserMonitorDTO;
import sg.ncs.kp.admin.dto.UserMonitorQueryDTO;
import sg.ncs.kp.admin.po.AdServer;
import sg.ncs.kp.uaa.server.po.User;

import java.util.List;

/**
 * @className AdServerMapper
 * @author chen xi
 * @version 1.0.0
 * @date 2023-07-10
 */
public interface UserMonitorMapper extends BaseMapper<User> {

    IPage<UserMonitorDTO> userMonitorList(Page<User> page, @Param("dto") UserMonitorQueryDTO dto);
}
