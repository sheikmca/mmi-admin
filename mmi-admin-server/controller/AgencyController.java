package sg.ncs.kp.admin.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import sg.ncs.kp.common.core.response.PageResult;
import sg.ncs.kp.common.core.response.Result;
import sg.ncs.kp.common.i18n.util.MessageUtils;
import sg.ncs.kp.uaa.client.util.SessionUtil;
import sg.ncs.kp.uaa.common.dto.AgencyDTO;
import sg.ncs.kp.uaa.common.dto.AgencyTenantDTO;
import sg.ncs.kp.uaa.common.dto.TenantDTO;
import sg.ncs.kp.uaa.common.dto.UserModifyDTO;
import sg.ncs.kp.uaa.common.enums.LogicallyDeleteEnum;
import sg.ncs.kp.uaa.common.vo.TenantVO;
import sg.ncs.kp.uaa.server.po.TenantConfig;
import sg.ncs.kp.uaa.server.service.TenantConfigService;
import sg.ncs.kp.uaa.server.service.TenantService;

/**
 * @description: AgencyController
 * @author: qiulinghuang
 * @create: 2022-08-19 16:39
 */
@RestController
@RequestMapping("/agency")
@Slf4j
public class AgencyController {


    @Autowired
    TenantService tenantService;

    @Autowired
    MessageUtils messageUtils;

    @Autowired
    TenantConfigService tenantConfigService;


    @PostMapping("/add")
    @PreAuthorize("hasAuthority('agencyManagementCreateAgency')")
    Result<AgencyDTO> addAgency(@RequestBody AgencyDTO agencyDTO) {
        String userId = SessionUtil.getUserId();
        AgencyTenantDTO tenant = agencyDTO.getTenant();
        tenant.setCreatedId(userId);
        tenant.setLastUpdatedId(userId);
        UserModifyDTO user = agencyDTO.getUser();
        user.setCreatedId(userId);
        user.setLastUpdatedId(userId);
        AgencyDTO result = tenantService.insertAgency(agencyDTO);
        return messageUtils.addSucceed(result);
    }


    @PostMapping("/update")
    @PreAuthorize("hasAuthority('agencyManagementEditAgency')")
    Result<Void> updateAgency(@RequestBody AgencyDTO agencyDTO) {
        String userId = SessionUtil.getUserId();
        AgencyTenantDTO tenant = agencyDTO.getTenant();
        tenant.setLastUpdatedId(userId);
        UserModifyDTO user = agencyDTO.getUser();
        String id = user.getId();
        if(StringUtils.isBlank(id)){
            user.setCreatedId(userId);
        }
        user.setLastUpdatedId(userId);
        tenantService.updateAgencyDetail(agencyDTO);
        return messageUtils.updateSucceed();
    }


    @GetMapping("/detail")
    @PreAuthorize("hasAuthority('agencyManagementEditAgency')")
    Result<AgencyDTO> getAgencyDTODetail(@RequestParam("tenantId") String tenantId, @RequestParam("userId") String userId){
        AgencyDTO agencyDetail = tenantService.getAgencyDetail(tenantId, userId);
        return messageUtils.succeed(agencyDetail);
    }


    @PostMapping("/update-status/{id}")
    @PreAuthorize("hasAuthority('agencyManagementEditAgency')")
    Result<Void> updateAgencyStatus(@PathVariable("id")String id, @RequestParam("status") Integer status) {
        tenantService.updateTenantStatus(id, status);
        return messageUtils.updateSucceed();
    }


    @PostMapping("/list")
    PageResult<TenantVO> getAgencyList(@RequestBody TenantDTO tenantDTO) {
        IPage<TenantVO> tenantVOIPage = tenantService.selectAgencyList(tenantDTO);
        return messageUtils.pageResult((int) tenantVOIPage.getCurrent(), (int) tenantVOIPage.getSize(),
                tenantVOIPage.getTotal(), tenantVOIPage.getRecords());
    }


    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasAuthority('agencyManagementDeleteAgency')")
    Result<Void> deleteAgencyList(@PathVariable("id") String id) {
        tenantService.LogicallyDelete(id, LogicallyDeleteEnum.INACTIVE);
        return messageUtils.deleteSucceed();
    }

    @PostMapping("/config/add-or-update")
    Result<TenantConfig> addOrUpdate(@RequestBody TenantConfig tenantConfig) {
        return messageUtils.succeed(tenantConfigService.addOrUpdate(tenantConfig));
    }


}
