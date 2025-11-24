package sg.ncs.kp.admin.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import cn.hutool.core.util.ObjectUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.baomidou.mybatisplus.core.metadata.IPage;

import cn.hutool.core.bean.BeanUtil;
import sg.ncs.kp.admin.dto.ModifyRoleDTO;
import sg.ncs.kp.admin.dto.RoleDTO;
import sg.ncs.kp.admin.dto.WSMessageDTO;
import sg.ncs.kp.admin.po.OperateLog;
import sg.ncs.kp.admin.pojo.WSMsgTypEnum;
import sg.ncs.kp.admin.service.Control2FAService;
import sg.ncs.kp.admin.util.NotificationUtil;
import sg.ncs.kp.common.core.response.PageResult;
import sg.ncs.kp.common.core.response.Result;
import sg.ncs.kp.common.i18n.util.MessageUtils;
import sg.ncs.kp.notification.pojo.NotificationConstants;
import sg.ncs.kp.uaa.client.session.UserSession;
import sg.ncs.kp.uaa.client.util.SessionUtil;
import sg.ncs.kp.uaa.common.dto.RoleAddUpdateDTO;
import sg.ncs.kp.uaa.common.dto.RoleQueryDTO;
import sg.ncs.kp.uaa.common.vo.RoleVO;
import sg.ncs.kp.uaa.server.mapper.UserMapper;
import sg.ncs.kp.uaa.server.po.Role;
import sg.ncs.kp.uaa.server.service.RoleService;

/**
 *
 * @author Lai Yin BO
 * @date 2022/09/19
 * @Description Role management controller
 */
@RestController
@RequestMapping("/role")
public class RoleController {
    @Autowired
    private RoleService roleService;
    
    @Autowired
    MessageUtils messageUtils;
    
    @Autowired
    NotificationUtil notificationUtil;
    
    @Autowired 
    UserMapper userMapper;

    @Autowired
    Control2FAService control2FAService;
    
    /**
     *
     * @Title pageListRoles
     * @Description get Role page List
     * @param roleQueryDTO
     * @return PageResult<RoleVO>
     */
    @PostMapping("/page-list")
    public PageResult<RoleDTO> pageListRoles(@RequestBody RoleQueryDTO roleQueryDTO){
        if(Objects.isNull(roleQueryDTO.getPageNo()) || roleQueryDTO.getPageNo() < 1) {
            roleQueryDTO.setPageSize(1);
        }
        if(Objects.isNull(roleQueryDTO.getPageNo()) || roleQueryDTO.getPageSize() < 1) {
            roleQueryDTO.setPageSize(10);
        }
        UserSession userSession = SessionUtil.getUserSession();
        roleQueryDTO.setTenantId(userSession.getTenantId());
        IPage<RoleVO> roles = roleService.rolePageList(roleQueryDTO);
        return messageUtils.pageResult((int) roles.getCurrent(), (int) roles.getSize(),
                roles.getTotal(), getRoleContents(roles.getRecords()));
    }


    private List<RoleDTO> getRoleContents(List<RoleVO> roleVOS){
        List<RoleDTO> roleDTOS = new ArrayList<>();
        if(ObjectUtil.isNotEmpty(roleVOS)){
            for(RoleVO roleVO:roleVOS){
                RoleDTO roleDTO = BeanUtil.copyProperties(roleVO, RoleDTO.class);
                roleDTO.setControl2faStatus(control2FAService.getStatusByRoleId(roleDTO.getId()));
                roleDTOS.add(roleDTO);
            }
        }
        return roleDTOS;
    }
    
    /**
     *
     * @Title listRoles
     * @Description Get role List
     * @return Result<List<RoleVO>>
     */
    @PostMapping("/list")
    public Result<List<RoleDTO>> listRoles(){
        UserSession userSession = SessionUtil.getUserSession();
        List<Role> roles = roleService.getRoles(userSession.getId(),null);
        return messageUtils.succeed(getRoleContents(BeanUtil.copyToList(roles,RoleVO.class)));
    }
    
    /**
     *
     * @Title addOrUpdateRole
     * @Description Add or update role
     * @param roleAddUpdateDTO
     * @return Result<RoleVO>
     */
    @PostMapping("/add-or-update")
    public Result<RoleDTO> addOrUpdateRole(@RequestBody ModifyRoleDTO roleAddUpdateDTO){
        UserSession userSession = SessionUtil.getUserSession();
        roleAddUpdateDTO.setTenantId(userSession.getTenantId());
        roleAddUpdateDTO.setUserId(userSession.getId());
        Role role = roleService.addOrUpdateRole(roleAddUpdateDTO);
        if(Objects.nonNull(roleAddUpdateDTO.getId())) {
            List<String> users = userMapper.userIdListByRole(roleAddUpdateDTO.getId());
            if(!CollectionUtils.isEmpty(users)) {
                WSMessageDTO wsMessageDTO = new WSMessageDTO();
                wsMessageDTO.setType(WSMsgTypEnum.PERMISSION_UPDATE);
                wsMessageDTO.setMessage(messageUtils.getMessage(WSMsgTypEnum.PERMISSION_UPDATE.getCode()));
                notificationUtil.sendQueueMsgToWebSocketExchange(wsMessageDTO, NotificationConstants.ADMIN_QUEUE_USER_STATUS,users);                
            }
        }
        control2FAService.addOrUpdateControl2FA(role.getId(),roleAddUpdateDTO.getControl2faStatus());
        RoleDTO roleDTO = BeanUtil.copyProperties(role, RoleDTO.class);
        roleDTO.setControl2faStatus(roleAddUpdateDTO.getControl2faStatus());
        return messageUtils.succeed(roleDTO);
        
    }
    
    /**
     *
     * @Title deleteRole
     * @Description Delete role Id
     * @param id
     * @return Result<Void>
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('roleManagementDelete')")
    public Result<Void> deleteRole(@PathVariable Integer id){
        roleService.deleteRoleAndRelation(id);
        control2FAService.deleteControl2FA(id);
        return messageUtils.deleteSucceed();
    }
        
}
