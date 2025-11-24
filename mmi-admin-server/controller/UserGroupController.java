package sg.ncs.kp.admin.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import sg.ncs.kp.admin.service.KpUserGroupService;
import sg.ncs.kp.common.core.response.PageResult;
import sg.ncs.kp.common.core.response.Result;
import sg.ncs.kp.common.i18n.util.MessageUtils;
import sg.ncs.kp.uaa.client.session.UserSession;
import sg.ncs.kp.uaa.client.util.SessionUtil;
import sg.ncs.kp.uaa.common.dto.UserDTO;
import sg.ncs.kp.uaa.common.dto.UserGroupAssignUserDTO;
import sg.ncs.kp.uaa.common.dto.UserGroupDotAssignUserDTO;
import sg.ncs.kp.uaa.common.dto.UserNeedAssignUserDTO;
import sg.ncs.kp.uaa.common.vo.UserGroupTreeVO;
import sg.ncs.kp.uaa.server.po.UserGroup;
import sg.ncs.kp.uaa.server.service.UserGroupService;

import java.util.ArrayList;
import java.util.List;

/**
 * @description: UserGroupController
 * @author: qiulinghuang
 * @create: 2022-08-18 18:07
 */
@RestController
@RequestMapping("/user-group")
@Slf4j
public class UserGroupController {

    @Autowired
    UserGroupService userGroupService;

    @Autowired
    KpUserGroupService kpUserGroupService;

    @Autowired
    MessageUtils messageUtils;

    @PostMapping("/add")
    @PreAuthorize("hasAuthority('userGroupAdd')")
    Result<UserGroup> insertUserGroup(@RequestBody UserGroup userGroup) {
        UserSession userSession = SessionUtil.getUserSession();
        userGroup.setTenantId(userSession.getTenantId());
        String userId = userSession.getId();
        userGroup.setCreatedId(userId);
        userGroup.setLastUpdatedId(userId);
        UserGroup insert = userGroupService.insert(userGroup);
        return messageUtils.addSucceed(insert);
    }


    @PostMapping("/update")
    @PreAuthorize("hasAuthority('userGroupEdit')")
    Result<Void> updateUserGroup(@RequestBody UserGroup userGroup) {
        UserSession userSession = SessionUtil.getUserSession();
        userGroup.setLastUpdatedId(userSession.getId());
        userGroupService.update(userGroup);
        return messageUtils.updateSucceed();
    }

    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasAuthority('userGroupDelete')")
    Result<Void> deleteUserGroup(@PathVariable("id") Integer id) {
        userGroupService.delete(id);
        return messageUtils.deleteSucceed();
    }


    @PostMapping("/tree")
    Result<List<UserGroupTreeVO>> getUserGroupTree(@RequestParam(value = "name",required = false) String name) {
        UserSession userSession = SessionUtil.getUserSession();
        String tenantId = userSession.getTenantId();
        UserGroupTreeVO userGroupTreeVO = userGroupService.selectAllTree(tenantId, name);
        List<UserGroupTreeVO> userGroupTreeVOList = new ArrayList<>();
        if (ObjectUtils.isNotEmpty(userGroupTreeVO)) {
            userGroupTreeVOList.add(userGroupTreeVO);
        }
        return messageUtils.succeed(userGroupTreeVOList);
    }


    @PostMapping("/assigned/user-list")
    PageResult<UserDTO> getAssignedUserList(@RequestBody UserGroupAssignUserDTO userGroupAssignUserDTO){
        UserSession userSession = SessionUtil.getUserSession();
        String tenantId = userSession.getTenantId();
        userGroupAssignUserDTO.setTenantId(tenantId);
        IPage<UserDTO> userDTOIPage = kpUserGroupService.getUserListByGroupId(userGroupAssignUserDTO);
        return messageUtils.pageResult((int) userDTOIPage.getCurrent(), (int) userDTOIPage.getSize(),
                userDTOIPage.getTotal(), userDTOIPage.getRecords());
    }


    @PostMapping("/not-assign/user-list")
    PageResult<UserDTO> getNotAssignedUserList(@RequestBody UserGroupDotAssignUserDTO userGroupDotAssignUserDTO){
        UserSession userSession = SessionUtil.getUserSession();
        String tenantId = userSession.getTenantId();
        userGroupDotAssignUserDTO.setTenantId(tenantId);
        IPage<UserDTO> userDTOIPage = userGroupService.dotAssignedUserList(userGroupDotAssignUserDTO);
        return messageUtils.pageResult((int) userDTOIPage.getCurrent(), (int) userDTOIPage.getSize(),
                userDTOIPage.getTotal(), userDTOIPage.getRecords());
    }

    @PostMapping("/assign-user")
    @PreAuthorize("hasAuthority('userGroupAssignUser')")
    Result<Void> assignUser(@RequestBody UserNeedAssignUserDTO userNeedAssignUserDTO) {
        userGroupService.assignUserToUserGroup(userNeedAssignUserDTO);
        return messageUtils.assignSucceed();
    }

    @PostMapping("/remove-user")
    @PreAuthorize("hasAuthority('userGroupRemoveUser')")
    Result<Void> removeUser(@RequestParam("userGroupId") Integer userGroupId, @RequestParam("userId") String userId) {
        userGroupService.removeUser(userGroupId, userId);
        return messageUtils.deleteSucceed();
    }


    @PostMapping("/list")
    Result<List<UserGroup>> getUserGroupList(@RequestParam(value = "name",required = false) String name) {
        UserSession userSession = SessionUtil.getUserSession();
        String tenantId = userSession.getTenantId();
        List<UserGroup> userGroupList = userGroupService.getUserGroupList(tenantId, name);
        return messageUtils.succeed(userGroupList);
    }
}
