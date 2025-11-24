package sg.ncs.kp.admin.feign.fallback;

import sg.ncs.kp.admin.feign.AgencyFeign;
import sg.ncs.kp.admin.pojo.TenantConfig;
import sg.ncs.kp.common.core.response.Result;

/**
 * @date 2022/10/19 13:55
 */
public class AgencyFeignFallBack implements AgencyFeign {

    @Override
    public Result<TenantConfig> getConfig() {
        return new Result<>();
    }

}
