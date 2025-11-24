package sg.ncs.kp.admin.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import sg.ncs.kp.admin.dto.AssignWorkspaceDTO;
import sg.ncs.kp.admin.dto.WorkspaceDTO;
import sg.ncs.kp.admin.service.WorkspaceService;
import sg.ncs.kp.common.core.response.Result;
import sg.ncs.kp.common.i18n.util.MessageUtils;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

/**
 * @className WorkspaceController
 * @author chen xi
 * @version 1.0.0
 * @date 2023-07-25
 */
@RestController
@RequestMapping("/workspace")
public class WorkspaceController {

    @Resource
    private WorkspaceService workspaceService;
    @Resource
    private MessageUtils messageUtils;

    @PostMapping("/save")
    @PreAuthorize("hasAuthority('addWorkspace')")
    public Result save(@Valid @RequestBody WorkspaceDTO workspaceDTO){
        WorkspaceDTO result = workspaceService.saveOrUpdateWorkspace(workspaceDTO);
        return messageUtils.addSucceed(result);
    }

    @PostMapping("/copy/{id}")
    @PreAuthorize("hasAuthority('copyWorkspace')")
    public Result copy(@PathVariable Integer id){
        WorkspaceDTO result = workspaceService.copyWorkspace(id);
        return messageUtils.addSucceed(result);
    }

    @PostMapping("/reset/{id}")
    public Result reset(@PathVariable Integer id){
        WorkspaceDTO result = workspaceService.resetWorkspace(id);
        return messageUtils.updateSucceed(result);
    }

    @PutMapping("/update")
    @PreAuthorize("hasAuthority('updateWorkspace')")
    public Result update(@Valid @RequestBody WorkspaceDTO workspaceDTO){
        WorkspaceDTO result = workspaceService.saveOrUpdateWorkspace(workspaceDTO);
        return messageUtils.updateSucceed(result);
    }
    @PostMapping("/assign")
    @PreAuthorize("hasAuthority('assignWorkspace')")
    public Result assign(@Valid @RequestBody AssignWorkspaceDTO assignWorkspaceDTO){
        workspaceService.assignWorkspace(assignWorkspaceDTO.getId(), assignWorkspaceDTO.getRoleIds());
        return messageUtils.succeed(null);
    }

    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasAuthority('deleteWorkspace')")
    public Result delete(@PathVariable Integer id){
        workspaceService.deleteWorkspace(id);
        return messageUtils.deleteSucceed();
    }

    @GetMapping("/list")
    @PreAuthorize("hasAuthority('searchWorkspace')")
    public Result<List<WorkspaceDTO>> list(@RequestParam(value = "name",required = false) String name,
                                           @RequestParam(value = "userId",required = false) String userId,
                                           @RequestParam(value = "setType",required = false) String setType){
        return messageUtils.succeed(workspaceService.list(name, userId, setType));
    }
}
