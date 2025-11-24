package sg.ncs.kp.admin.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import sg.ncs.kp.admin.feign.config.FeignInterceptor;
import sg.ncs.kp.admin.pojo.WorkstationStatusDTO;

import java.util.List;

/**
 * @auther 
 * @date 2022/9/1
 * @description
 */
@FeignClient(name = "mmi-admin", configuration = FeignInterceptor.class)
public interface WorkstationFeign {

    @GetMapping("/inner/workstation/get-all")
    List<WorkstationStatusDTO> getAllWorkstation();

    @PutMapping("/inner/workstation/batch-update-status")
    void batchUpdateWorkstationStatus(@RequestBody List<WorkstationStatusDTO> workstationStatusDTOS);

    @GetMapping("/inner/workstation/machineId/user/{userId}")
    String getUserWorkstation(@PathVariable Integer userId);
}
