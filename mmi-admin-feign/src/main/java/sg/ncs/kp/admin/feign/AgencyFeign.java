package sg.ncs.kp.admin.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import sg.ncs.kp.admin.feign.config.FeignInterceptor;
import sg.ncs.kp.admin.pojo.TenantConfig;
import sg.ncs.kp.common.core.response.Result;

/**
 * @date 2022/10/19 13:41
 */
@FeignClient(name = "mmi-admin", configuration = FeignInterceptor.class)
public interface AgencyFeign {

    @GetMapping("/inner/agency/config")
    Result<TenantConfig> getConfig();

}
