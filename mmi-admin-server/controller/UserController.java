package sg.ncs.kp.admin.controller;

import cn.hutool.core.bean.BeanUtil;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import sg.ncs.kp.admin.dto.*;
import sg.ncs.kp.admin.po.UploadRecord;
import sg.ncs.kp.admin.pojo.WSMsgTypEnum;
import sg.ncs.kp.admin.service.AsyncService;
import sg.ncs.kp.admin.service.KpUserService;
import sg.ncs.kp.admin.util.NotificationUtil;
import sg.ncs.kp.common.core.response.PageResult;
import sg.ncs.kp.common.core.response.Result;
import sg.ncs.kp.common.i18n.util.MessageUtils;
import sg.ncs.kp.common.oss.component.OssComponent;
import sg.ncs.kp.common.uti.poi.ExcelDropDownList;
import sg.ncs.kp.notification.pojo.NotificationConstants;
import sg.ncs.kp.uaa.client.util.SessionUtil;
import sg.ncs.kp.uaa.common.dto.PasswordPolicyDTO;
import sg.ncs.kp.uaa.common.dto.RoleUserBasicDTO;
import sg.ncs.kp.uaa.common.dto.UserDTO;
import sg.ncs.kp.uaa.common.dto.UserDetailDTO;
import sg.ncs.kp.uaa.common.dto.UserModifyDTO;
import sg.ncs.kp.uaa.common.dto.UserPasswordDTO;
import sg.ncs.kp.uaa.common.dto.UserQueryDTO;
import sg.ncs.kp.uaa.common.dto.UserUploadRecordQueryDTO;
import sg.ncs.kp.uaa.common.vo.UserGroupTreeVO;
import sg.ncs.kp.uaa.server.mapper.RoleMapper;
import sg.ncs.kp.uaa.server.po.PasswordPolicy;
import sg.ncs.kp.uaa.server.po.Role;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @auther IVAN
 * @date 2022/8/19
 * @description
 */
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private KpUserService kpUserService;

    @Autowired
    private MessageUtils messageUtils;

    @Autowired
    private OssComponent ossComponent;

    @Autowired
    private AsyncService asyncService;

    @Autowired
    private RoleMapper roleMapper;

    @Autowired
    NotificationUtil notificationUtil;

    private static final String FILE_NAME = "user_batch_add.xlsx";

    @PostMapping("/list")
    //@PreAuthorize("hasAuthority('roleManagementUserList')")
    public PageResult<UserDTO> list(@RequestBody UserQueryDTO userQueryDTO) {
        IPage<UserDTO> page = kpUserService.selectList(userQueryDTO);
        return messageUtils.pageResult((int) page.getCurrent(), (int) page.getSize(),
                page.getTotal(), BeanUtil.copyToList(page.getRecords(), UserDTO.class));
    }

    @PostMapping("/add")
    @PreAuthorize("hasAuthority('userManagementAddUser')")
    public Result add(@Valid @RequestBody UserModifyDTO userModifyDTO) {
        kpUserService.add(userModifyDTO);
        return messageUtils.addSucceed(null);
    }

    @PutMapping("/update")
    @PreAuthorize("hasAuthority('userManagementEditUser')")
    public Result update(@Valid @RequestBody UserModifyDTO userModifyDTO) {
        boolean roleChange = false;
        if (StringUtils.isNotBlank(userModifyDTO.getId())) {
            List<RoleUserBasicDTO> roleBasic = roleMapper.getRolesByUserIds(Arrays.asList(userModifyDTO.getId()));
            Set<Integer> oldRoleIds = new HashSet<>();
            roleBasic.forEach(role -> oldRoleIds.add(role.getId()));
            roleChange = checkRolesChange(oldRoleIds, userModifyDTO.getRoleIds());
        }
        kpUserService.update(userModifyDTO);
        if (roleChange) {
            WSMessageDTO wsMessageDTO = new WSMessageDTO();
            wsMessageDTO.setType(WSMsgTypEnum.PERMISSION_UPDATE);
            wsMessageDTO.setMessage(messageUtils.getMessage(WSMsgTypEnum.PERMISSION_UPDATE.getCode()));
            notificationUtil.sendQueueMsgToWebSocketExchange(wsMessageDTO, NotificationConstants.ADMIN_QUEUE_USER_STATUS, Arrays.asList(userModifyDTO.getId()));
        }
        return messageUtils.updateSucceed(null);
    }

    @PostMapping("/assign-role")
    @PreAuthorize("hasAuthority('userManagementAssignRole')")
    public Result assignRole(@Valid @RequestBody AssignRoleDTO assignRole) {
        List<String> userIds = kpUserService.assignRole(assignRole);
        if(ObjectUtil.isNotEmpty(userIds)){
            WSMessageDTO wsMessageDTO = new WSMessageDTO();
            wsMessageDTO.setType(WSMsgTypEnum.PERMISSION_UPDATE);
            wsMessageDTO.setMessage(messageUtils.getMessage(WSMsgTypEnum.PERMISSION_UPDATE.getCode()));
            notificationUtil.sendQueueMsgToWebSocketExchange(wsMessageDTO, NotificationConstants.ADMIN_QUEUE_USER_STATUS,userIds);
        }
        return messageUtils.updateSucceed(null);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('userManagementDeleteUser')")
    public Result delete(@PathVariable("id") String id) {
        kpUserService.delete(id);
        return messageUtils.deleteSucceed(null);
    }

    @PostMapping("/delete")
    @PreAuthorize("hasAuthority('userManagementDeleteUser')")
    public Result delete(@RequestBody List<String> ids) {
        kpUserService.delete(ids);
        return messageUtils.deleteSucceed(null);
    }

    @PutMapping("/status/{id}/{status}")
    @PreAuthorize("hasAuthority('userManagementEditUser')")
    public Result updateStatus(@PathVariable("id") String id, @PathVariable("status") Integer status) {
        kpUserService.updateStatus(id, status);
        if (Objects.equals(0, status)) {
            asyncService.sendMessage(WSMsgTypEnum.ACCOUNT_DISABLE, id);
        }
        return messageUtils.updateSucceed(null);
    }

    @GetMapping("/group")
    public Result<UserGroupTreeVO> group(String groupName) {
        UserGroupTreeVO userGroupTreeVO = kpUserService.group(groupName);
        return messageUtils.succeed(userGroupTreeVO);
    }

    @PostMapping("/change-group")
    @PreAuthorize("hasAuthority('userManagementChangeGroup')")
    public Result changeGroup(@Valid @RequestBody ChangeGroupDTO changeGroupDTO) {
        kpUserService.changeGroup(changeGroupDTO);
        return messageUtils.updateSucceed(null);
    }

    @PutMapping("/password")
    public Result password(@Valid @RequestBody UserPasswordDTO userPasswordDTO) {
        kpUserService.password(userPasswordDTO);
        return messageUtils.updateSucceed(null);
    }

    @PostMapping("/update-myself-password")
    public Result updateMyselfPassword(@Valid @RequestBody UserSelfPasswordUpdateDTO userPasswordDTO) {
        kpUserService.updateMyselfPassword(userPasswordDTO);
        return messageUtils.updateSucceed(null);
    }

    @GetMapping("/password/policy")
    public Result<List<PasswordPolicy>> policy() {
        List<PasswordPolicy> passwordPolicies = kpUserService.policy();
        return messageUtils.succeed(passwordPolicies);
    }

    @PostMapping("/password/policy")
    @PreAuthorize("hasAuthority('savePasswordPolicy')")
    public Result policy(@Valid @RequestBody List<PasswordPolicyDTO> list) {
        kpUserService.policy(list);
        return messageUtils.updateSucceed(null);
    }

    @PostMapping("/reset-password")
    @PreAuthorize("hasAuthority('userManagementResetPassword')")
    public Result resetPassword(@RequestBody ResetPasswordDTO resetPasswordDTO) {
        kpUserService.resetPassword(resetPasswordDTO);
        return messageUtils.updateSucceed(null);
    }

    @GetMapping("/get-myself")
    public Result<UserDetailDTO> myself() {
        UserDetailDTO userDetailDTO = kpUserService.get(SessionUtil.getUserId());
        userDetailDTO.setRoleId(SessionUtil.getRoles());
        return messageUtils.succeed(userDetailDTO);
    }

    @PutMapping("/update-myself")
    public Result updateMyself(@Valid @RequestBody UserUpdateProfileDTO user) {
        kpUserService.updateMyself(user);
        return messageUtils.updateSucceed();
    }


    @PostMapping("/upload-batch-add-user-file")
    @PreAuthorize("hasAuthority('userManagementBatchAddUser')")
    public Result<UserUploadPathDTO> upload(@RequestParam("file") MultipartFile file) {
        String path = kpUserService.upload(file);
        UserUploadPathDTO userUploadPathDTO = new UserUploadPathDTO();
        userUploadPathDTO.setPath(path);
        return messageUtils.succeed(userUploadPathDTO);
    }

    @PostMapping("/view-upload-batch-add-user-file")
    @PreAuthorize("hasAuthority('userManagementBatchAddUser')")
    public PageResult<UserUploadViewDTO> view(@Valid @RequestBody UserViewQueryDTO userViewQueryDTO) {
        Page<UserUploadViewDTO> page = kpUserService.view(userViewQueryDTO);
        return messageUtils.pageResult((int) page.getCurrent(), (int) page.getSize(),
                page.getTotal(), BeanUtil.copyToList(page.getRecords(), UserUploadViewDTO.class));
    }

    @PostMapping("/import-upload-batch-add-user")
    @PreAuthorize("hasAuthority('userManagementBatchAddUser')")
    public Result<UploadResultDTO> importFile(@Valid @RequestBody UserViewQueryDTO userViewQueryDTO) {
        UploadResultDTO uploadResultDTO = kpUserService.importFile(userViewQueryDTO.getPath());
        return messageUtils.succeed(uploadResultDTO);
    }

    @PostMapping("/batch-add")
    @PreAuthorize("hasAuthority('userManagementAddUser')")
    public Result<BatchUserResultDTO> add(@Valid @RequestBody BatchUserDTO batchUserDTO) {
        BatchUserResultDTO result = kpUserService.batchAdd(batchUserDTO);
        return messageUtils.addSucceed(result);
    }

    @GetMapping("/download-user-batch-add-template")
    @PreAuthorize("hasAuthority('userManagementBatchAddUser')")
    public void template(HttpServletResponse response) throws IOException {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("template/user_batch_add.xlsx");
        XSSFWorkbook workbook = new XSSFWorkbook(inputStream);
        List<Role> roles = roleMapper.selectList(Wrappers.<Role>lambdaQuery()
                .eq(Role::getTenantId, SessionUtil.getTenantId())
                .eq(Role::getStatus, 1)
        );
        if(!CollectionUtils.isEmpty(roles)){
            List<String> roleNames = roles.stream().map(Role::getName).collect(Collectors.toList());
            ExcelDropDownList.setExcelDropDownList(workbook.getSheetAt(0), 1, 1, 8, 8, roleNames, workbook);
        }
        response.setHeader("Content-disposition", "attachment;filename=" + FILE_NAME + ";" + "filename*=utf-8''" + FILE_NAME);
        response.addHeader("Access-Control-Expose-Headers", "Content-Disposition");
        response.setContentType("application/vnd.ms-excel;charset=UTF-8");
        workbook.write(response.getOutputStream());
        workbook.close();
    }

    @PostMapping("/get-history")
    @PreAuthorize("hasAuthority('userManagementBatchAddUser')")
    public PageResult<UploadRecord> uploadRecord(@RequestBody UserUploadRecordQueryDTO userUploadRecordQueryDTO) {
        Page<UploadRecord> page = kpUserService.uploadRecord(userUploadRecordQueryDTO);
        return messageUtils.pageResult((int) page.getCurrent(), (int) page.getSize(),
                page.getTotal(), BeanUtil.copyToList(page.getRecords(), UploadRecord.class));
    }

    @PostMapping("/download-upload-user-file")
    @PreAuthorize("hasAuthority('userManagementBatchAddUser')")
    public void downloadUploadUserFile(HttpServletResponse response, @Valid @RequestBody UserViewQueryDTO userViewQueryDTO) {
        kpUserService.downloadUploadFile(response, userViewQueryDTO.getPath(), FILE_NAME);
    }

    @PostMapping("/export")
    @PreAuthorize("hasAuthority('userManagementExportUser')")
    public void export(@RequestBody List<String> ids, HttpServletResponse response) {
        kpUserService.export(ids, response);
    }

    @PutMapping("/notification")
    public Result notification(@RequestParam("id") String id, @RequestParam("status") Integer status) {
        kpUserService.notification(id, status);
        return messageUtils.updateSucceed();
    }

    @GetMapping("/menus")
    public Result<List<MenuDTO>> menus() {
        List<MenuDTO> permissionDTOS = kpUserService.menus();
        return messageUtils.succeed(permissionDTOS);
    }

    @GetMapping("/all-ids-by-group/{groupId}")
    public Result<Set<String>> getAllIdsByGroupId(@PathVariable("groupId") Integer groupId) {
        Set<Integer> groupIds = new HashSet<>();
        groupIds.add(groupId);
        return messageUtils.succeed(kpUserService.getUserIdsByGroupId(groupIds));
    }

    private boolean checkRolesChange(Set<Integer> oldRoleIds, Set<Integer> roleIds) {
        if (CollectionUtils.isEmpty(roleIds)) {
            if (CollectionUtils.isEmpty(oldRoleIds)) {
                return false;
            }
        } else {
            if (roleIds.containsAll(oldRoleIds) && oldRoleIds.containsAll(roleIds)) {
                return false;
            }
        }
        return true;
    }

}
