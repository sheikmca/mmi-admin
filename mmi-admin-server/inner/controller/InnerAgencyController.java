package sg.ncs.kp.admin.inner.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sg.ncs.kp.common.core.response.Result;
import sg.ncs.kp.common.i18n.util.MessageUtils;
import sg.ncs.kp.uaa.server.po.TenantConfig;
import sg.ncs.kp.uaa.server.service.TenantConfigService;
import sg.ncs.kp.uaa.server.service.TenantService;

/**
 * @author Wang Shujin
 * @date 2022/10/19 11:29
 */
@RestController
@RequestMapping("/inner/agency")
public class InnerAgencyController {

    @Autowired
    MessageUtils messageUtils;

    @Autowired
    TenantConfigService tenantConfigService;

    @GetMapping("/config")
    Result<TenantConfig> getConfig() {
        return messageUtils.succeed(tenantConfigService.getConfig());
    }

}
