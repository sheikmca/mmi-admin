package sg.ncs.kp.admin.feign;

import com.alibaba.fastjson.JSONObject;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import sg.ncs.kp.admin.feign.config.FeignInterceptor;

import java.util.Set;

/**
 * @date 2023-11-10
 * @description
 */
@FeignClient(name = "mmi-admin", configuration = FeignInterceptor.class)
public interface RoleFeign {

    @GetMapping("/inner/role/all-role-id")
    Set<Long> getAllRoleIds();
    @GetMapping("/inner/role/is-exist/{queryCriteria}/{roleId}")
    JSONObject isExistByQueryCriteria(@PathVariable("queryCriteria") String queryCriteria, @PathVariable("roleId") String roleId);
}
