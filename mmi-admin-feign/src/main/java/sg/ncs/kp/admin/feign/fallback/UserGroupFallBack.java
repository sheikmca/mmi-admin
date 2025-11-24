package sg.ncs.kp.admin.feign.fallback;

import com.alibaba.fastjson.JSONObject;
import sg.ncs.kp.admin.feign.RoleFeign;
import sg.ncs.kp.admin.feign.UserGroupFeign;

import java.util.HashSet;
import java.util.Set;

/**
 * @date 2023-11-10
 * @description
 */
public class UserGroupFallBack implements UserGroupFeign {
    @Override
    public JSONObject isExistByQueryCriteria(String queryCriteria, String userId) {
        return null;
    }
}
