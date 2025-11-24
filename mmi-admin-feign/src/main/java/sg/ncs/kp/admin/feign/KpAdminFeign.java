package sg.ncs.kp.admin.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import sg.ncs.kp.admin.feign.config.FeignInterceptor;
import sg.ncs.kp.admin.pojo.TenantVO;
import sg.ncs.kp.common.core.response.Result;

/**
 * @auther 
 * @date 2023/1/11
 * @description
 */
@FeignClient(name = "mmi-admin", configuration = FeignInterceptor.class)
public interface KpAdminFeign {

    @GetMapping(value = "/inner/token/checkPassword",consumes = "application/json;charset=UTF-8")
    Result<Boolean> checkPassWord(@RequestParam("username") String username,@RequestParam("password") String password);

    @GetMapping(value = "/inner/tenant/{id}",consumes = "application/json;charset=UTF-8")
    Result<TenantVO> getTenantDetail(@PathVariable("id") String id);

    @GetMapping("/inner/token/{userId1}/{userId2}/login-region")
    Boolean isUseSameRegion(@PathVariable("userId1") String userId1,@PathVariable("userId2") String userId2);
}
