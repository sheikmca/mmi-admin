package sg.ncs.kp.admin.inner.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sg.ncs.kp.common.core.response.Result;
import sg.ncs.kp.common.i18n.util.MessageUtils;
import sg.ncs.kp.uaa.server.po.Tenant;
import sg.ncs.kp.uaa.server.service.TenantService;

/**
 * @auther IVAN
 * @date 2023/1/4
 * @description
 */
@RestController
@RequestMapping("/inner/tenant")
@Slf4j
public class InnerTenantController {

    @Autowired
    TenantService tenantService;
    @Autowired
    MessageUtils messageUtils;

    @GetMapping("/{id}")
    public Result<Tenant> getTenantDetail(@PathVariable("id") String id) {
        Tenant tenant = tenantService.getTenantById(id);
        return messageUtils.succeed(tenant);
    }


}

