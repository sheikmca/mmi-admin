package sg.ncs.kp.admin.feign;

import com.alibaba.fastjson.JSONObject;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import sg.ncs.kp.admin.feign.config.FeignInterceptor;

import java.util.Set;

/**
 * @date 2023-11-10
 * @description
 */
@FeignClient(name = "mmi-admin", configuration = FeignInterceptor.class)
public interface UserGroupFeign {

    @GetMapping("/inner/user-group/is-exist/{queryCriteria}/{userId}")
    JSONObject isExistByQueryCriteria(@PathVariable("queryCriteria") String queryCriteria, @PathVariable("userId") String userId);
}
