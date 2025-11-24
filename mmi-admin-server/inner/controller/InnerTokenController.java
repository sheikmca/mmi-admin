package sg.ncs.kp.admin.inner.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import sg.ncs.kp.common.core.response.Result;
import sg.ncs.kp.common.i18n.util.MessageUtils;
import sg.ncs.kp.uaa.server.service.UserService;

/**
 * @auther IVAN
 * @date 2023/1/4
 * @description
 */
@RestController
@RequestMapping("/inner/token")
public class InnerTokenController {

    @Autowired
    private MessageUtils messageUtils;


    @Autowired
    private UserService userService;

    @GetMapping("/checkPassword")
    public Result<Boolean> checkPassWord(@RequestParam("username") String username, @RequestParam("password") String password) {
        return messageUtils.succeed(userService.checkPassWordIsTenantAdmin(username, password));
    }
}

