package sg.ncs.kp.admin.service.impl;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import sg.ncs.kp.admin.dto.AreaDTO;
import sg.ncs.kp.admin.dto.WorkspaceDTO;
import sg.ncs.kp.admin.enums.AdminMsgEnum;
import sg.ncs.kp.admin.enums.FrontSizeTypeEnum;
import sg.ncs.kp.admin.enums.SetTypeEnum;
import sg.ncs.kp.admin.enums.ThemeTypeEnum;
import sg.ncs.kp.admin.mapper.RoleWorkspaceMappingMapper;
import sg.ncs.kp.admin.mapper.WorkspaceMapper;
import sg.ncs.kp.admin.po.*;
import sg.ncs.kp.admin.service.RoleWorkspaceService;
import sg.ncs.kp.admin.service.WorkspaceService;
import sg.ncs.kp.common.exception.pojo.ServiceException;
import sg.ncs.kp.uaa.client.session.UserSession;
import sg.ncs.kp.uaa.client.util.SessionUtil;
import sg.ncs.kp.uaa.common.enums.StatusEnum;
import sg.ncs.kp.uaa.server.po.Role;
import sg.ncs.kp.uaa.server.po.User;
import sg.ncs.kp.uaa.server.service.RoleService;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
/**
 * @className WorkspaceServiceImpl
 * @author chen xi
 * @version 1.0.0
 * @date 2023-07-25
 */

@Slf4j
@Service
public class WorkspaceServiceImpl implements WorkspaceService {

    @Autowired
    private WorkspaceMapper workspaceMapper;
    @Autowired
    private RoleWorkspaceService roleWorkspaceService;
    @Autowired
    private RoleService roleService;
    @Value("${admin.systemRoleId}")
    private Integer systemRoleId;

    @Override
    public WorkspaceDTO saveOrUpdateWorkspace(WorkspaceDTO workspaceDTO) {
        UserSession userSession = SessionUtil.getUserSession();
        WorkspaceDTO result = new WorkspaceDTO();
        Integer roleId = null;
        Integer workspaceId = null;
        List<Integer> bindingRoleIds = workspaceDTO.getRoleIds();
        if(ObjectUtil.isEmpty(bindingRoleIds)){
            bindingRoleIds = new ArrayList<>();
        }
        if(ObjectUtil.isNotEmpty(SessionUtil.getRoles())){
            List<Integer> roleIds = new ArrayList<Integer>(SessionUtil.getRoles());
            roleId = roleIds.get(0);
        }
        //名称重复
        Integer count = repeatNameCheck(roleId, userSession.getTenantId(), workspaceDTO.getName(), workspaceDTO.getBindingId(),workspaceDTO.getId());
        if (count > 0) {
            throw new ServiceException(AdminMsgEnum.WORKSPACE_NAME_REPEAT);
        }
        Workspace workspace = null;
        if(ObjectUtil.isNotEmpty(workspaceDTO.getId())) {
            workspace = workspaceMapper.selectById(workspaceDTO.getId());
        }
        workspace = setWorkspace(workspaceDTO, workspace, userSession.getTenantId(), userSession.getId());
        if (ObjectUtil.isEmpty(workspace.getId())) {
            workspaceMapper.insert(workspace);
            workspaceId = workspace.getId();
            bindingRoleIds.add(roleId);
            result = getWorkspaceDTO(workspace);
        } else {
            workspaceMapper.updateById(workspace);
            workspaceId = workspace.getId();
            result = getWorkspaceDTO(workspace);
        }
        //bind role
        if(ObjectUtil.isNotEmpty(bindingRoleIds) && bindingRoleIds.size() > 0){
            assignWorkspace(workspaceId,bindingRoleIds);
        }
        bindingRoleIds.remove(roleId);
        result.setRoleIds(bindingRoleIds);
        return result;
    }

    @Override
    public WorkspaceDTO copyWorkspace(Integer id) {
        WorkspaceDTO result = new WorkspaceDTO();
        Integer workspaceId = null;
        UserSession userSession = SessionUtil.getUserSession();
        Integer roleId = null;
        if(ObjectUtil.isNotEmpty(SessionUtil.getRoles())){
            List<Integer> roleIds = new ArrayList<Integer>(SessionUtil.getRoles());
            roleId = roleIds.get(0);
        }
        List<Integer> bindingRoleIds = new ArrayList<>();
        Workspace workspace = workspaceMapper.selectById(id);
        if(ObjectUtil.isNotEmpty(workspace)) {
            workspace.setId(null);
            workspace.setSetType(SetTypeEnum.NORMAL.getKey());
            workspace.setCreatedId(userSession.getId());
            workspace.setLastUpdatedId(userSession.getId());
            workspace.setCreatedDate(DateUtil.date());
            workspace.setLastUpdatedDate(DateUtil.date());
            workspace.setName(getCopyName(id, workspace.getName()));
            workspace.setBindingId(id);
            workspaceMapper.insert(workspace);
            workspaceId = workspace.getId();
            bindingRoleIds.add(roleId);
            result = getWorkspaceDTO(workspace);
            //bind role
            if (ObjectUtil.isNotEmpty(bindingRoleIds) && bindingRoleIds.size() > 0) {
                assignWorkspace(workspaceId, bindingRoleIds);
            }
            bindingRoleIds.remove(roleId);
            result.setRoleIds(bindingRoleIds);
        }
        return result;
    }

    private String getCopyName(Integer bindingId, String bindName){
        Long count = workspaceMapper.selectCount(Wrappers
                .<Workspace>lambdaQuery()
                .eq(Workspace::getBindingId, bindingId)
                .eq(Workspace::getTenantId, SessionUtil.getTenantId()));
        if(count == 0){
            return bindName+" copy"+(count+1);
        }else{
            Long index = count+1;
            String name = bindName+" copy"+index;
            while(workspaceMapper.selectCount(Wrappers
                    .<Workspace>lambdaQuery()
                    .eq(Workspace::getName, name)
                    .eq(Workspace::getTenantId, SessionUtil.getTenantId())) != 0){
                index++;
                name = bindName+" copy"+index;
            }
            return name;
        }
    }

    @Override
    public WorkspaceDTO resetWorkspace(Integer id) {
        WorkspaceDTO result = new WorkspaceDTO();
        UserSession userSession = SessionUtil.getUserSession();
        Workspace workspace = workspaceMapper.selectById(id);
        if(ObjectUtil.isNotEmpty(workspace)) {
            if(workspace.getSetType().equals(1)){
                throw new ServiceException(AdminMsgEnum.WORKSPACE_RESET_TYPE_ERROR);
            }
            Workspace defaultWorkspace = workspaceMapper.selectById(workspace.getBindingId());
            if(ObjectUtil.isEmpty(defaultWorkspace)){
                throw new ServiceException(AdminMsgEnum.WORKSPACE_DEFAULT_ALREADY_DEL);
            }
            workspace.setFrontSize(defaultWorkspace.getFrontSize());
            workspace.setDashboardSetting(defaultWorkspace.getDashboardSetting());
            workspace.setFrontStyle(defaultWorkspace.getFrontStyle());
            workspace.setEoSetting(defaultWorkspace.getEoSetting());
            workspace.setRoiColor(defaultWorkspace.getRoiColor());
            workspace.setTrackNum(defaultWorkspace.getTrackNum());
            workspace.setTheme(defaultWorkspace.getTheme());
            workspace.setNfoaSetting(defaultWorkspace.getNfoaSetting());
            workspace.setLastUpdatedId(userSession.getId());
            workspace.setLastUpdatedDate(DateUtil.date());
            workspaceMapper.updateById(workspace);
            result = getWorkspaceDTO(workspace);
        }
        return result;
    }

    @Override
    public List<WorkspaceDTO> list(String name, String userId, String setType){
        Integer roleId = null;
        if(ObjectUtil.isNotEmpty(userId)){
            List<Role> roles = roleService.getRoles(userId, StatusEnum.ACTIVE);
            if(ObjectUtil.isNotEmpty(roles)){
                Role role = roles.get(0);
                roleId = role.getId();
            }
        }else {
            if (ObjectUtil.isNotEmpty(SessionUtil.getRoles())) {
                List<Integer> roleIds = new ArrayList<Integer>(SessionUtil.getRoles());
                roleId = roleIds.get(0);
            } else {
                roleId = systemRoleId;
            }
        }
        Integer setTypeInt = null;
        if(ObjectUtil.isNotEmpty(setType)) {
            setTypeInt = SetTypeEnum.getKeyByValue(setType);
        }
        List<Workspace> workspaces = workspaceMapper.list(name,roleId,setTypeInt,SessionUtil.getTenantId());
        List<WorkspaceDTO> results = new ArrayList<>();
        if(ObjectUtil.isNotEmpty(workspaces)){
            for(Workspace workspace:workspaces){
                WorkspaceDTO workspaceDTO = this.getWorkspaceDTO(workspace);
                workspaceDTO.setRoleIds(this.getAllRoleIdsByWorkspaceId(workspaceDTO.getId()));
                results.add(workspaceDTO);
            }
        }
        return results;
    }

    @Override
    public void assignWorkspace(Integer id, List<Integer> roleIds){
        List<Integer> oldRoleIds = getAllRoleIdsByWorkspaceId(id);
        List<Integer> removeRoles = new ArrayList<>();
        List<Integer> addRoles = new ArrayList<>();
        addRoles.addAll(roleIds);
        removeRoles.addAll(oldRoleIds);
        addRoles.removeAll(oldRoleIds);
        removeRoles.removeAll(roleIds);
        batchAddMappings(addRoles,id);
        if(ObjectUtil.isNotEmpty(removeRoles)) {
            batchRemoveMappings(removeRoles, id);
        }
    }

    @Override
    public void deleteWorkspace(Integer id){
        workspaceMapper.deleteById(id);
        roleWorkspaceService.deleteBatch(id);
    }

    private List<Integer> getAllRoleIdsByWorkspaceId(Integer id){
        List<RoleWorkspaceMapping> mappings =  roleWorkspaceService.getAllByWorkspaceId(id);
        List<Integer> roleIds = new ArrayList<>();
        if(ObjectUtil.isNotEmpty(mappings)) {
            roleIds = mappings.stream().map(item -> item.getRoleId()).collect(Collectors.toList());
        }
        return roleIds;
    }

    private void batchAddMappings(List<Integer> addRoles, Integer id){
        List<RoleWorkspaceMapping> addMappings = new ArrayList<>();
        // Each person can only have one workspace. Need to cover
        List<Integer> delMapping = new ArrayList<>();
        if(ObjectUtil.isNotEmpty(addRoles)){
            for(Integer roleId:addRoles) {
                RoleWorkspaceMapping mapping = new RoleWorkspaceMapping();
                mapping.setWorkspaceId(id);
                mapping.setRoleId(roleId);
                addMappings.add(mapping);
                if(!roleId.equals(systemRoleId)){
                    delMapping.add(roleId);
                }
            }
            if(ObjectUtil.isNotEmpty(delMapping)) {
                roleWorkspaceService.deleteBatch(delMapping);
            }
            roleWorkspaceService.saveBatch(addMappings);
        }
    }

    private void batchRemoveMappings(List<Integer> removeRoles, Integer id){
        if(ObjectUtil.isNotEmpty(removeRoles)){
            roleWorkspaceService.deleteBatch(id,removeRoles);
        }
    }

    private Workspace selectByNameAndUserId(String name, String userId, String tenantId){
        return workspaceMapper.selectOne(Wrappers.<Workspace>lambdaQuery()
                .eq(Workspace::getName,name)
                .eq(Workspace::getCreatedId,userId)
                .eq(Workspace::getTenantId,tenantId));
    }

    private Workspace setWorkspace(WorkspaceDTO workspaceDTO, Workspace workspace, String tenantId, String userId){
        if (ObjectUtil.isEmpty(workspace)) {
            workspace = new Workspace();
            workspace.setTenantId(tenantId);
            workspace.setCreatedId(userId);
            workspace.setCreatedDate(DateUtil.date());
        }
        workspace.setName(workspaceDTO.getName());
        Integer frontSize = null;
        if(ObjectUtil.isNotEmpty(workspaceDTO.getFrontSize())){
            frontSize = FrontSizeTypeEnum.getKeyByValue(workspaceDTO.getFrontSize());
        }
        workspace.setFrontSize(frontSize);
        Integer theme = null;
        if(ObjectUtil.isNotEmpty(workspaceDTO.getTheme())) {
            theme = ThemeTypeEnum.getKeyByValue(workspaceDTO.getTheme());
        }
        workspace.setTheme(theme);
        Integer setType = null;
        if(ObjectUtil.isNotEmpty(workspaceDTO.getSetType())) {
            setType = SetTypeEnum.getKeyByValue(workspaceDTO.getSetType());
        }
        workspace.setSetType(setType);
        workspace.setTrackNum(workspaceDTO.getTrackNum());
        workspace.setRoiColor(workspaceDTO.getRoiColor());
        workspace.setBindingId(workspaceDTO.getBindingId());
        workspace.setFrontStyle(workspaceDTO.getFrontStyle());
        String eoSetting = null;
        if(ObjectUtil.isNotEmpty(workspaceDTO.getEoSetting())) {
            eoSetting = workspaceDTO.getEoSetting().toJSONString();
        }
        workspace.setEoSetting(eoSetting);
        String nfoaSetting = null;
        if(ObjectUtil.isNotEmpty(workspaceDTO.getNfoaSetting())) {
            nfoaSetting = workspaceDTO.getNfoaSetting().toJSONString();
        }
        workspace.setNfoaSetting(nfoaSetting);
        String dashboardSetting = null;
        if(ObjectUtil.isNotEmpty(workspaceDTO.getDashboardSetting())) {
            dashboardSetting = workspaceDTO.getDashboardSetting().toJSONString();
        }
        workspace.setDashboardSetting(dashboardSetting);
        workspace.setLastUpdatedId(userId);
        workspace.setLastUpdatedDate(DateUtil.date());
        return workspace;
    }

    private WorkspaceDTO getWorkspaceDTO(Workspace workspace) {
        if(ObjectUtil.isNotEmpty(workspace)) {
            WorkspaceDTO workspaceDTO = new WorkspaceDTO();
            workspaceDTO.setId(workspace.getId());
            workspaceDTO.setName(workspace.getName());
            String frontSize = null;
            if(ObjectUtil.isNotEmpty(workspace.getFrontSize())){
                frontSize = FrontSizeTypeEnum.getValueByKey(workspace.getFrontSize());
            }
            workspaceDTO.setFrontSize(frontSize);
            workspaceDTO.setFrontStyle(workspace.getFrontStyle());
            workspaceDTO.setBindingId(workspaceDTO.getBindingId());
            String setType = null;
            if(ObjectUtil.isNotEmpty(workspace.getSetType())){
                setType = SetTypeEnum.getValueByKey(workspace.getSetType());
            }
            workspaceDTO.setSetType(setType);
            String theme = null;
            if(ObjectUtil.isNotEmpty(workspace.getTheme())){
                theme = ThemeTypeEnum.getValueByKey(workspace.getTheme());
            }
            workspaceDTO.setTheme(theme);
            workspaceDTO.setTrackNum(workspace.getTrackNum());
            workspaceDTO.setRoiColor(workspace.getRoiColor());
            JSONObject eoSetting = new JSONObject();
            if(ObjectUtil.isNotEmpty(workspace.getEoSetting())){
                eoSetting = JSON.parseObject(workspace.getEoSetting());
            }
            workspaceDTO.setEoSetting(eoSetting);
            JSONObject nfoaSetting = new JSONObject();
            if(ObjectUtil.isNotEmpty(workspace.getNfoaSetting())){
                nfoaSetting = JSON.parseObject(workspace.getNfoaSetting());
            }
            workspaceDTO.setNfoaSetting(nfoaSetting);
            JSONObject dashboardSetting = new JSONObject();
            if(ObjectUtil.isNotEmpty(workspace.getDashboardSetting())){
                dashboardSetting = JSON.parseObject(workspace.getDashboardSetting());
            }
            workspaceDTO.setDashboardSetting(dashboardSetting);
            workspaceDTO.setLastUpdatedDate(workspace.getLastUpdatedDate());
            workspaceDTO.setCreatedDate(workspace.getCreatedDate());
            return workspaceDTO;
        }
        return null;
    }

    /**
     * check repeat name in role
     *
     * @param roleId role id
     * @param name     name
     * @param bindingId normal binding default id
     * @param workspaceId need ignore workspace id
     * @return
     */
    private Integer repeatNameCheck(Integer roleId, String tenantId,String name, Integer bindingId, Integer workspaceId) {
        if (StringUtils.isBlank(name)) {
            throw new ServiceException(AdminMsgEnum.WORKSPACE_NOT_EXIST);
        }
        if (ObjectUtil.isEmpty(roleId)) {
            roleId = systemRoleId;
        }
        return workspaceMapper.selectCountByRoleId(name,roleId,tenantId,bindingId,workspaceId);
    }
}
