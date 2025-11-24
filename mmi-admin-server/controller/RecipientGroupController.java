package sg.ncs.kp.admin.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import sg.ncs.kp.admin.dto.AssignUserDTO;
import sg.ncs.kp.admin.dto.RemoveUserFromGroupDTO;
import sg.ncs.kp.admin.po.RecipientGroup;
import sg.ncs.kp.admin.service.RecipientGroupService;
import sg.ncs.kp.common.core.response.PageResult;
import sg.ncs.kp.common.core.response.Result;
import sg.ncs.kp.common.i18n.util.MessageUtils;
import sg.ncs.kp.uaa.client.session.UserSession;
import sg.ncs.kp.uaa.client.util.SessionUtil;
import sg.ncs.kp.uaa.common.dto.UserDTO;
import sg.ncs.kp.uaa.common.dto.UserGroupAssignUserDTO;

import java.util.List;

/**
 * description: Recipient Group Controller
 * @author Wang Shujin
 * @date 2022/8/24 15:03
 */
@RestController
@RequestMapping("/recipient-group")
@Slf4j
public class RecipientGroupController {

    @Autowired
    private RecipientGroupService recipientGroupService;

    @Autowired
    private MessageUtils messageUtils;

    private static final String ACTIVE = "1";

    @PostMapping("/add")
    @PreAuthorize("hasAuthority('recipientGroupAddGroup')")
    public Result<RecipientGroup> insertRecipientGroup(@RequestBody RecipientGroup recipientGroup) {
        UserSession userSession = SessionUtil.getUserSession();
        recipientGroup.setTenantId(userSession.getTenantId());
        String userId = userSession.getId();
        recipientGroup.setCreatedId(userId);
        recipientGroup.setLastUpdatedId(userId);
        RecipientGroup insert = recipientGroupService.insert(recipientGroup);
        return messageUtils.addSucceed(insert);
    }


    @PostMapping("/update")
    @PreAuthorize("hasAuthority('recipientGroupEditGroup')")
    public Result<Void> updateRecipientGroup(@RequestBody RecipientGroup recipientGroup) {
        UserSession userSession = SessionUtil.getUserSession();
        recipientGroup.setLastUpdatedId(userSession.getId());
        recipientGroupService.update(recipientGroup);
        return messageUtils.updateSucceed();
    }

    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasAuthority('recipientGroupDeleteGroup')")
    public Result<Void> deleteRecipientGroup(@PathVariable("id") Integer id) {
        recipientGroupService.delete(id);
        return messageUtils.deleteSucceed();
    }


    @GetMapping("/list")
    public Result<List<RecipientGroup>> getRecipientGroup(@RequestParam(value = "name",required = false) String name) {
        UserSession userSession = SessionUtil.getUserSession();
        String tenantId = userSession.getTenantId();
        List<RecipientGroup> recipientGroups = recipientGroupService.selectAll(tenantId, name);
        return messageUtils.succeed(recipientGroups);

    }


    @PostMapping("/{id}/assigned/user-list")
    public PageResult<UserDTO> getAssignedUserList(@PathVariable("id") Integer id, @RequestBody UserGroupAssignUserDTO dto){
        UserSession userSession = SessionUtil.getUserSession();
        String tenantId = userSession.getTenantId();
        dto.setTenantId(tenantId);
        dto.setStatus(ACTIVE);
        IPage<UserDTO> userDTOIPage = recipientGroupService.assignedUserList(id, dto);
        return messageUtils.pageResult((int) userDTOIPage.getCurrent(), (int) userDTOIPage.getSize(),
                userDTOIPage.getTotal(), userDTOIPage.getRecords());
    }


    @PostMapping("/{id}/not-assign/user-list")
    PageResult<UserDTO> getNotAssignedUserList(@PathVariable("id") Integer id, @RequestBody UserGroupAssignUserDTO dto){
        UserSession userSession = SessionUtil.getUserSession();
        String tenantId = userSession.getTenantId();
        dto.setTenantId(tenantId);
        dto.setStatus(ACTIVE);
        IPage<UserDTO> userDTOIPage = recipientGroupService.groupNotAssignedUserList(id, dto);
        return messageUtils.pageResult((int) userDTOIPage.getCurrent(), (int) userDTOIPage.getSize(),
                userDTOIPage.getTotal(), userDTOIPage.getRecords());
    }

    @PostMapping("/{id}/assign-user")
    @PreAuthorize("hasAuthority('recipientGroupAddUser')")
    public Result<Void> assignUser(@PathVariable("id") Integer id, @RequestBody AssignUserDTO dto) {
        dto.setGroupId(id);
        recipientGroupService.assignUserToRecipientGroup(dto);
        return messageUtils.assignSucceed();
    }

    @PostMapping("/{id}/remove-user")
    @PreAuthorize("hasAuthority('recipientGroupDeleteUser')")
    public Result<Void> removeUser(@PathVariable("id") Integer id, @RequestBody RemoveUserFromGroupDTO dto) {
        dto.setGroupId(id);
        recipientGroupService.removeUser(dto);
        return messageUtils.deleteSucceed();
    }

}
