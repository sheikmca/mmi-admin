package sg.ncs.kp.admin.controller;
/**
 * @className UserMonitorController
 * @author chen xi
 * @version 1.0.0
 * @date 2023-07-25
 */

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import sg.ncs.kp.admin.dto.UserMonitorCountDTO;
import sg.ncs.kp.admin.dto.UserMonitorDTO;
import sg.ncs.kp.admin.dto.UserMonitorQueryDTO;
import sg.ncs.kp.admin.pojo.WSMsgTypEnum;
import sg.ncs.kp.admin.service.KpUserOnlineService;
import sg.ncs.kp.admin.service.UserMonitorService;
import sg.ncs.kp.common.core.response.PageResult;
import sg.ncs.kp.common.core.response.Result;
import sg.ncs.kp.common.i18n.util.MessageUtils;
import sg.ncs.kp.uaa.client.util.SessionUtil;
import sg.ncs.kp.uaa.common.dto.HourOnlineCountDTO;
import sg.ncs.kp.uaa.server.service.LoginService;

import javax.validation.Valid;
import java.util.Date;
import java.util.List;

/**
 * @author Chen Xi
 * @version 1.0
 * @date 2021-07-14
 */
@RestController
@RequestMapping("/user-monitor")
public class UserMonitorController {
    @Autowired
    private UserMonitorService userMonitorService;
    @Autowired
    private MessageUtils messageUtils;

    @PostMapping("/list")
    public PageResult<UserMonitorDTO> list(@RequestBody @Valid UserMonitorQueryDTO userQueryDTO){
        IPage<UserMonitorDTO> page = userMonitorService.selectUserMonitorList(userQueryDTO);
        return messageUtils.pageResult((int) page.getCurrent(), (int) page.getSize(),
                page.getTotal(), BeanUtil.copyToList(page.getRecords(), UserMonitorDTO.class));
    }

    @GetMapping("/count")
    public Result<UserMonitorCountDTO> count(){
        return messageUtils.succeed(userMonitorService.getUserCount());
    }

    @GetMapping("/count-by-time/{time}")
    public Result<List<HourOnlineCountDTO>> countByTime(@PathVariable("time") String time){
        return messageUtils.succeed(userMonitorService.countByTime(DateUtil.parseDate(time)));
    }
}
