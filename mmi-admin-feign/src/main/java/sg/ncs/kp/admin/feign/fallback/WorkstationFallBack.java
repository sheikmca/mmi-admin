package sg.ncs.kp.admin.feign.fallback;

import sg.ncs.kp.admin.feign.UserFeign;
import sg.ncs.kp.admin.feign.WorkstationFeign;
import sg.ncs.kp.admin.pojo.RoleUserDTO;
import sg.ncs.kp.admin.pojo.WorkstationStatusDTO;
import sg.ncs.kp.common.core.response.Result;

import java.util.*;

/**
 * @auther 
 * @date 2022/9/2
 * @description
 */
public class WorkstationFallBack implements WorkstationFeign {

    private Throwable throwable;

    public WorkstationFallBack(Throwable throwable) {
        this.throwable = throwable;
    }
    @Override
    public List<WorkstationStatusDTO> getAllWorkstation() {
        return null;
    }

    @Override
    public void batchUpdateWorkstationStatus(List<WorkstationStatusDTO> workstationStatusDTOS) {

    }

    @Override
    public String getUserWorkstation(Integer userId) {
        return "0";
    }
}
