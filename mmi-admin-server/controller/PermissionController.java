package sg.ncs.kp.admin.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import sg.ncs.kp.common.core.response.Result;
import sg.ncs.kp.common.i18n.util.MessageUtils;
import sg.ncs.kp.uaa.client.util.SessionUtil;
import sg.ncs.kp.uaa.common.dto.PermissionDTO;
import sg.ncs.kp.uaa.server.po.Permission;
import sg.ncs.kp.uaa.server.service.PermissionService;

import java.util.List;

/**
 * @auther IVAN
 * @date 2022/8/30
 * @description
 */
@RestController
@RequestMapping("/permission")
public class PermissionController {

    @Autowired
    private PermissionService permissionService;

    @Autowired
    private MessageUtils messageUtils;


    @GetMapping
    public Result<List<PermissionDTO>> permission(){
        List<PermissionDTO> tree = permissionService.tree(SessionUtil.getUserId(), SessionUtil.getUserLevel());
        return messageUtils.succeed(tree);
    }

    @GetMapping("/list")
    public Result<List<Permission>> permissionList(@RequestParam("roleId") Integer roleId){
        List<Permission> permissions = permissionService.getPermissionsByRole(roleId);
        return messageUtils.succeed(permissions);
    }
}
