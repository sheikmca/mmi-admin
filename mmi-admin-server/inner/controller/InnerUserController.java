package sg.ncs.kp.admin.inner.controller;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import sg.ncs.kp.admin.service.KpUserService;
import sg.ncs.kp.common.core.response.Result;
import sg.ncs.kp.common.i18n.util.MessageUtils;
import sg.ncs.kp.uaa.common.enums.UserLevelEnum;
import sg.ncs.kp.uaa.server.mapper.UserMapper;
import sg.ncs.kp.uaa.server.po.User;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @auther IVAN
 * @date 2022/8/19
 * @description
 */
@RestController
@RequestMapping("/inner/user")
public class InnerUserController {

    @Autowired
    private KpUserService kpUserService;

    @Autowired
    private MessageUtils messageUtils;

    @Autowired
    private UserMapper userMapper;

    @PostMapping("/getUserIds")
    Result<Set<String>> getUserIdsByTenantId(@RequestParam("tenantId") String tenantId, @RequestParam("ids")String ids) {
        if (StringUtils.isBlank(ids)) {
            return messageUtils.succeed(new HashSet<>());
        }
        Set<String> idSet = new HashSet<>();
        String[] split = ids.split(",");
        for (String str : split) {
            idSet.add(str);
        }
        return messageUtils.succeed(kpUserService.getUserIdsByTenantId(tenantId, idSet));
    }
    @PostMapping("/ids")
    public Result<Set<String>> getUserIdsByRoleId(@RequestBody Set<Integer> roleIds){
        Set<String> ids = kpUserService.getUserIdsByRoleId(roleIds);
        return messageUtils.succeed(ids);
    }

    @PostMapping({"/idsByGroupId"})
    Result<Set<String>> getUserIdsByGroupId(@RequestBody Set<Integer> userGroupIds){
        Set<String> ids = kpUserService.getUserIdsByGroupId(userGroupIds);
        return messageUtils.succeed(ids);
    }

    @GetMapping("/getUserNameMap")
    Result<Map<String, String>> getUserNameMap(@RequestParam String idArray) {
        if (StrUtil.isBlank(idArray)){
            return messageUtils.succeed(Collections.emptyMap());
        }
        List<String> idList= Arrays.asList(idArray.split(","));
        Map<String, String> map= kpUserService.getUserNameMap(idList);
        return messageUtils.succeed(map);
    }
    
    @GetMapping("/agencyAdminId")
    public Result<Set<String>> getAgencyAdminId(@RequestParam("tenantId") String tenantId){
        Set<String> ids= new HashSet<>();
        List<User> users = userMapper.selectList(Wrappers.<User>lambdaQuery()
                .select(User::getId)
                .eq(User::getTenantId, tenantId)
                .eq(User::getLevel, UserLevelEnum.TENANT_ADMIN.getValue()));
       for(User user:users) {
           ids.add(user.getId());
       }
        return messageUtils.succeed(ids);
    }
}
