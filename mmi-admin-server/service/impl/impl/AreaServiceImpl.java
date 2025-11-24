package sg.ncs.kp.admin.service.impl;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import sg.ncs.kp.admin.dto.AreaDTO;
import sg.ncs.kp.admin.enums.AdminMsgEnum;
import sg.ncs.kp.admin.mapper.AreaMapper;
import sg.ncs.kp.admin.mapper.RoleAreaMappingMapper;
import sg.ncs.kp.admin.po.Area;
import sg.ncs.kp.admin.po.RoleAreaMapping;
import sg.ncs.kp.admin.service.AreaService;
import sg.ncs.kp.common.exception.pojo.ServiceException;
import sg.ncs.kp.uaa.client.session.UserSession;
import sg.ncs.kp.uaa.client.util.SessionUtil;
import sg.ncs.kp.uaa.server.mapper.UserRoleMappingMapper;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collection;

/**
 * @author Duan Ran
 * @date 2022/8/23
 */
@Service
public class AreaServiceImpl implements AreaService {

    @Resource
    private AreaMapper areaMapper;
    @Resource
    private RoleAreaMappingMapper roleAreaMappingMapper;
    @Resource
    private UserRoleMappingMapper userRoleMappingMapper;

    @Override
    public void saveArea(AreaDTO areaDTO) {
        //若前端没有传递父地址，那么默认就是根目录
        if (areaDTO.getParentId() == null) {
            areaDTO.setParentId(0);
        } else if (areaDTO.getParentId() > 0) {
            QueryWrapper<Area> areaQueryWrapper = new QueryWrapper<>();
            areaQueryWrapper.eq("id", areaDTO.getParentId());
            Long count = areaMapper.selectCount(areaQueryWrapper);
            if (count == 0) {
                throw new ServiceException(AdminMsgEnum.PARENT_AREA_NOT_EXIST);
            }
        }
        UserSession userSession = SessionUtil.getUserSession();
        //名称重复
        Long count = repeatNameCheck(userSession.getTenantId(), areaDTO.getName(), areaDTO.getId());
        if (count > 0) {
            throw new ServiceException(AdminMsgEnum.AREA_NAME_REPEAT);
        }

        if (areaDTO.getId() == null) {
            //新增
            Area area = new Area();
            BeanUtils.copyProperties(areaDTO, area);
            area.setSort(999);
            area.setCreatedId(userSession.getId());
            area.setCreatedDate(DateUtil.date());
            area.setLastUpdatedId(area.getCreatedId());
            area.setLastUpdatedDate(area.getCreatedDate());

            areaMapper.insert(area);

            if (CollectionUtils.isNotEmpty(userSession.getRoleId())) {
                for (Integer roleId : userSession.getRoleId()) {
                    RoleAreaMapping roleAreaMapping = new RoleAreaMapping();
                    roleAreaMapping.setRoleId(roleId);
                    roleAreaMapping.setAreaId(area.getId());
                    roleAreaMappingMapper.insert(roleAreaMapping);
                }
            }

        } else {
            //update
            Area area = areaMapper.selectById(areaDTO.getId());
            if (area == null) {
                throw new ServiceException(AdminMsgEnum.AREA_NOT_EXIST);
            }
            area.setParentId(areaDTO.getParentId());
            area.setName(areaDTO.getName());
            area.setDescription(areaDTO.getDescription());
            area.setLastUpdatedDate(DateUtil.date());
            area.setLastUpdatedId(userSession.getId());
            areaMapper.updateById(area);
        }
    }

    @Override
    public void delete(Integer id) {
        Area area=areaMapper.selectById(id);
        if (null==area){
            throw new ServiceException(AdminMsgEnum.AREA_NOT_EXIST);
        }
        areaMapper.deleteBatchIds(getSubArea(id,true));
    }


    private Collection<Integer> getSubArea(Integer areaId,boolean includeMe){

        return new ArrayList<>(0);
    }

    /**
     * 检查租户下名称重复数量
     *
     * @param tenantId 租户id
     * @param name     名称
     * @param ignoreId 需要忽略的area id
     * @return
     */
    private Long repeatNameCheck(String tenantId, String name, Integer... ignoreId) {
        if (StringUtils.isBlank(name)) {
            throw new ServiceException(AdminMsgEnum.AREA_NAME_EXIST);
        }
        QueryWrapper<Area> areaQueryWrapper = new QueryWrapper<>();

        if (null != tenantId) {
            areaQueryWrapper.eq("tenant_id", tenantId);
        }

        areaQueryWrapper.eq("name", name);
        areaQueryWrapper.notIn("id", ignoreId);

        return areaMapper.selectCount(areaQueryWrapper);
    }
}
