package sg.ncs.kp.admin.controller;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sg.ncs.kp.common.core.response.PageResult;
import sg.ncs.kp.common.core.response.Result;
import sg.ncs.kp.common.i18n.util.MessageUtils;
import sg.ncs.kp.uaa.client.apikey.ApikeyProperties;
import sg.ncs.kp.uaa.common.dto.ApiKeyQueryDTO;
import sg.ncs.kp.uaa.common.dto.ApikeyDTO;
import sg.ncs.kp.uaa.common.dto.UserDTO;
import sg.ncs.kp.uaa.common.vo.ApiKeyQueryVO;
import sg.ncs.kp.uaa.common.vo.ApikeyVO;
import sg.ncs.kp.uaa.server.service.ApikeyService;

/**
 * description:
 * @author Wang Shujin
 * @date 2022/11/22 16:15
 */
@RestController
@RequestMapping("/apikey")
@ConditionalOnProperty(prefix = "kp.uaa.resourceserver.apikey", name = "enabled", havingValue = "true")
public class ApikeyController {

    @Autowired
    private ApikeyService apikeyService;

    @Autowired
    MessageUtils messageUtils;

    @PostMapping("/add-or-update")
    public Result<ApikeyVO> addOrUpdate(@RequestBody ApikeyDTO dto) {
        return messageUtils.succeed(apikeyService.addOrUpdate(dto));
    }

    @GetMapping
    public PageResult<ApiKeyQueryVO> list(ApiKeyQueryDTO dto) {
        IPage<ApiKeyQueryVO> page = apikeyService.list(dto);
        return messageUtils.pageResult((int) page.getCurrent(), (int) page.getSize(), page.getTotal(), page.getRecords());
    }

    @DeleteMapping("/delete-by-user/{userName}")
    public Result<Void> delete(@PathVariable("userName") String userName) {
        apikeyService.delete(userName);
        return messageUtils.deleteSucceed();
    }


}
