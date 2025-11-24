package sg.ncs.kp.admin.inner.controller;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import sg.ncs.kp.admin.service.RecipientGroupService;
import sg.ncs.kp.common.core.response.Result;
import sg.ncs.kp.common.i18n.util.MessageUtils;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * description: TODO
 *
 * @author Wang Shujin
 * @date 2022/9/2 14:11
 */
@RestController
@RequestMapping("/inner/recipientGroup")
public class InnerRecipientGroupController {

    @Autowired
    private RecipientGroupService recipientGroupService;

    @Autowired
    private MessageUtils messageUtils;


    @GetMapping("/owned")
    public Result<List<Integer>> ownedRecipientGroup() {
        return messageUtils.succeed(recipientGroupService.ownedRecipientGroup());
    }


    @PostMapping("/getUserIds")
    public Result<Set<String>> getUserIdsByRecipientIds(@RequestParam("ids") String ids) {
        if (StringUtils.isBlank(ids)) {
            return messageUtils.succeed(new HashSet<>());
        }
        Set<Integer> idSet = new HashSet<>();
        String[] split = ids.split(",");
        for (String str : split) {
            try {
                idSet.add(Integer.valueOf(str));
            } catch (NumberFormatException ignored) {}
        }
        return messageUtils.succeed(recipientGroupService.hasUsers(idSet));
    }
    @PostMapping("/hasUsers")
    public Result<Set<String>> groupHasUsers(@RequestBody Collection<Integer> recipientGroupIds) {
        return messageUtils.succeed(recipientGroupService.hasUsers(recipientGroupIds));
    }

}
