package sg.ncs.kp.admin.feign.fallback;

import org.springframework.cloud.openfeign.FallbackFactory;
import sg.ncs.kp.admin.feign.UserFeign;
import sg.ncs.kp.admin.feign.WorkstationFeign;

/**
 * @auther 
 * @date 2022/9/2
 * @description
 */
public class WorkstationFallbackFactory implements FallbackFactory<WorkstationFeign> {

    @Override
    public WorkstationFeign create(Throwable cause) {
        return new WorkstationFallBack(cause);
    }
}
