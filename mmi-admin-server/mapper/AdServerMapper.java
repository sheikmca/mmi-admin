package sg.ncs.kp.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import sg.ncs.kp.admin.po.AdServer;

import java.util.List;

/**
 * @className AdServerMapper
 * @author chen xi
 * @version 1.0.0
 * @date 2023-07-10
 */
public interface AdServerMapper extends BaseMapper<AdServer> {

    AdServer getByAdName(String adName);

    List<String> getAllAdNames();
}
