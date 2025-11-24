package sg.ncs.kp.admin.feign.fallback;

import org.springframework.cloud.openfeign.FallbackFactory;
import sg.ncs.kp.admin.feign.UserFeign;

/**
 * @auther 
 * @date 2022/9/2
 * @description
 */
public class UserFeignFallbackFactory implements FallbackFactory<UserFeign> {

    @Override
    public UserFeign create(Throwable cause) {
        return new UserFallBack(cause);
    }
}
