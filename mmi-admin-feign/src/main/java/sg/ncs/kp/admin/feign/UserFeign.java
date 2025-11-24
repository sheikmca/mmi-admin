package sg.ncs.kp.admin.feign;

import com.alibaba.fastjson.JSONObject;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import sg.ncs.kp.admin.feign.config.FeignInterceptor;
import sg.ncs.kp.admin.pojo.RoleUserDTO;
import sg.ncs.kp.common.core.response.Result;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @auther 
 * @date 2022/9/1
 * @description
 */
@FeignClient(name = "mmi-admin", configuration = FeignInterceptor.class)
public interface UserFeign {

    @PostMapping("/inner/user/ids")
    Result<Set<String>> getUserIdsByRoleId(@RequestBody Set<Long> roleIds);

    @PostMapping("/inner/user/idsByGroupId")
    Result<Set<String>> getUserIdsByGroupId(@RequestBody Set<Integer> groupIds);

    @PostMapping("/inner/user/getUserIds")
    Result<Set<String>> getUserIdsByTenantId(@RequestParam("tenantId") String tenantId, @RequestParam("ids") String ids);


    @GetMapping("/inner/user/getUserNameMap")
    Result<Map<String, String>> getUserNameMap(@RequestParam String idArray);

    @GetMapping("/inner/user/agencyAdminId")
    Result<Set<String>> getAgencyAdminId(@RequestParam("tenantId") String tenantId);

    @GetMapping("/inner/user/{userId}/role")
    Result<String> getRoleIdByUserId(@PathVariable("userId") String userId);

    @GetMapping("/inner/user/{permissionId}/role-user")
    List<RoleUserDTO> getRoleUsersByPermissionId(@PathVariable("permissionId") Integer permissionId);

    @GetMapping("/inner/user/all-user-id")
    Set<String> getAllUserIds();

    @GetMapping("/inner/user/is-same-hub/{targetUserId}/{currentUserId}")
    Boolean isSameHub(@PathVariable("targetUserId") String targetUserId, @PathVariable("currentUserId") String currentUserId);

    @GetMapping("/inner/user/get-full-path/{userId}")
    Result<String> getFullPathByUserId(@PathVariable("userId") String userId);

    @GetMapping("/inner/user/{id}/user/id")
    Result<Integer> getUserId(@PathVariable("id") String id);

    @GetMapping("/inner/user/id/{userId}")
    Result<String> getId(@PathVariable("userId") Integer userId);
    @GetMapping("/inner/user/is-exist/{queryCriteria}/{roleId}")
    JSONObject isExistByQueryCriteria(@PathVariable("queryCriteria") String queryCriteria, @PathVariable("roleId") String roleId);
}
