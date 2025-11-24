package sg.ncs.kp.admin.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sg.ncs.kp.admin.mapper.RoleWorkspaceMappingMapper;
import sg.ncs.kp.admin.po.RoleWorkspaceMapping;
import sg.ncs.kp.admin.service.RoleWorkspaceService;

import java.util.List;

/**
 * @className RoleWorkspaceServiceImpl
 * @author chen xi
 * @version 1.0.0
 * @date 2023-08-29
 */
@Service
public class RoleWorkspaceServiceImpl extends ServiceImpl<RoleWorkspaceMappingMapper, RoleWorkspaceMapping> implements RoleWorkspaceService {
    @Autowired
    private RoleWorkspaceMappingMapper roleWorkspaceMappingMapper;
    @Override
    public List<RoleWorkspaceMapping> getAllByWorkspaceId(Integer workspaceId) {
        return roleWorkspaceMappingMapper.selectList(Wrappers.<RoleWorkspaceMapping>lambdaQuery()
                .eq(RoleWorkspaceMapping::getWorkspaceId,workspaceId));
    }

    @Override
    public void deleteBatch(Integer workspaceId,List<Integer> roleIds){
        roleWorkspaceMappingMapper.delete(Wrappers.<RoleWorkspaceMapping>lambdaQuery()
                .eq(RoleWorkspaceMapping::getWorkspaceId,workspaceId)
                .in(RoleWorkspaceMapping::getRoleId,roleIds));
    }

    @Override
    public void deleteBatch(List<Integer> roleIds){
        roleWorkspaceMappingMapper.delete(Wrappers.<RoleWorkspaceMapping>lambdaQuery()
                .in(RoleWorkspaceMapping::getRoleId,roleIds));
    }

    @Override
    public void deleteBatch(Integer workspaceId){
        roleWorkspaceMappingMapper.delete(Wrappers.<RoleWorkspaceMapping>lambdaQuery()
                .eq(RoleWorkspaceMapping::getWorkspaceId,workspaceId));
    }
}
