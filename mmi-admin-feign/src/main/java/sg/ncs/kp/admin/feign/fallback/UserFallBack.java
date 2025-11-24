package sg.ncs.kp.admin.feign.fallback;

import com.alibaba.fastjson.JSONObject;
import sg.ncs.kp.admin.feign.UserFeign;
import sg.ncs.kp.admin.pojo.RoleUserDTO;
import sg.ncs.kp.common.core.response.Result;

import java.util.*;

/**
 * @auther 
 * @date 2022/9/2
 * @description
 */
public class UserFallBack implements UserFeign {
    private String serverName = "admin";

    private Throwable throwable;

    public UserFallBack(Throwable throwable) {
        this.throwable = throwable;
    }

    @Override
    public Result<Set<String>> getUserIdsByRoleId(Set<Long> roleIds) {
        return null;
    }

    @Override
    public Result<Set<String>> getUserIdsByGroupId(Set<Integer> groupIds) {
        return new Result<>(new HashSet<>(), false, null, "error", 500);
    }

    @Override
    public Result<Set<String>> getUserIdsByTenantId(String tenantId, String ids) {
        return new Result<>(new HashSet<>(), false, null, "error", 500);
    }

    @Override
    public Result<Map<String, String>> getUserNameMap(String idArray) {
        return null;
    }

    @Override
    public Result<Set<String>> getAgencyAdminId(String tenantId) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Result<String> getRoleIdByUserId(String userId) {
        return null;
    }

    @Override
    public List<RoleUserDTO> getRoleUsersByPermissionId(Integer permissionId) {
        return new ArrayList<>();
    }

    @Override
    public Set<String> getAllUserIds() {
        return new HashSet<>();
    }

    @Override
    public Boolean isSameHub(String targetUserId, String currentUserId) {
        return false;
    }

    @Override
    public Result<String> getFullPathByUserId(String userId) {
        return new Result<>();
    }

    @Override
    public Result<Integer> getUserId(String id) {
        return new Result<>();
    }

    @Override
    public Result<String> getId(Integer userId) {
        return new Result<>();
    }

    @Override
    public JSONObject isExistByQueryCriteria(String queryCriteria, String roleId) {
        return null;
    }
}
