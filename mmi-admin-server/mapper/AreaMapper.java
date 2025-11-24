package sg.ncs.kp.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import sg.ncs.kp.admin.po.Area;

import java.util.List;

/**
 * @author Duan Ran
 * @date 2022/8/23
 */
public interface AreaMapper extends BaseMapper<Area> {
    List<Integer> getSubId(Integer parentId);
}
