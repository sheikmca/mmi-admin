package sg.ncs.kp.admin.controller;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import sg.ncs.kp.admin.dto.LoginLogQueryDTO;
import sg.ncs.kp.admin.dto.OperateLogQueryDTO;
import sg.ncs.kp.admin.po.OperateLog;
import sg.ncs.kp.admin.service.LogService;
import sg.ncs.kp.common.core.response.PageResult;
import sg.ncs.kp.common.i18n.util.MessageUtils;
import sg.ncs.kp.uaa.server.po.LoginLog;

/**
 * @auther IVAN
 * @date 2022/9/8
 * @description
 */
@RestController
@RequestMapping("/log")
public class LogController {

    @Autowired
    private LogService logService;

    @Autowired
    private MessageUtils messageUtils;

    @PostMapping("/login-log-list")
    public PageResult<LoginLog> loginList(@RequestBody LoginLogQueryDTO queryDTO) {
        Page<LoginLog> page= logService.loginRecords(queryDTO);
        return messageUtils.pageResult((int) page.getCurrent(), (int) page.getSize(),
                page.getTotal(), BeanUtil.copyToList(page.getRecords(), LoginLog.class));
    }

    @PostMapping("/operate-log-list")
    public PageResult<OperateLog> operateList(@RequestBody OperateLogQueryDTO queryDTO) {
        Page<OperateLog> page=logService.operateRecords(queryDTO);
        return messageUtils.pageResult((int) page.getCurrent(), (int) page.getSize(),
                page.getTotal(), BeanUtil.copyToList(page.getRecords(), OperateLog.class));
    }
}
