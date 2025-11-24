package sg.ncs.kp.admin.feign.fallback;

import com.alibaba.fastjson.JSONObject;
import sg.ncs.kp.admin.feign.RoleFeign;

import java.util.*;

/**
 * @date 2023-11-10
 * @description
 */
public class RoleFallBack implements RoleFeign {

    @Override
    public Set<Long> getAllRoleIds() {
        return new HashSet<>();
    }

    @Override
    public JSONObject isExistByQueryCriteria(String queryCriteria, String roleId) {
        return null;
    }
}
