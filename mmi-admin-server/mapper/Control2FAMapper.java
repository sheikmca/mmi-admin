package sg.ncs.kp.admin.mapper;
/**
 * @className Control2FAMapper
 * @author chen xi
 * @version 1.0.0
 * @date 2023-07-11
 */

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import sg.ncs.kp.admin.po.Control2FA;

import java.util.List;

public interface Control2FAMapper extends BaseMapper<Control2FA> {

    List<Control2FA> getControl2fas(@Param("userId") String userId, @Param("status") Integer status);
}
