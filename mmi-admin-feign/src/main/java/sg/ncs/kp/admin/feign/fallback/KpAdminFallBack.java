package sg.ncs.kp.admin.feign.fallback;

import sg.ncs.kp.admin.feign.KpAdminFeign;
import sg.ncs.kp.admin.pojo.TenantVO;
import sg.ncs.kp.common.core.response.Result;

/**
 * @date 2023-11-08
 * @description
 */
public class KpAdminFallBack implements KpAdminFeign {

    @Override
    public Result<Boolean> checkPassWord(String username, String password) {
        return null;
    }

    @Override
    public Result<TenantVO> getTenantDetail(String id) {
        return null;
    }

    @Override
    public Boolean isUseSameRegion(String userId1, String userId2) {
        return false;
    }


}
