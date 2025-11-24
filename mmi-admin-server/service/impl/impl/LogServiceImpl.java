package sg.ncs.kp.admin.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sg.ncs.kp.admin.dto.LoginLogQueryDTO;
import sg.ncs.kp.admin.dto.OperateLogQueryDTO;
import sg.ncs.kp.admin.mapper.OperateLogMapper;
import sg.ncs.kp.admin.po.OperateLog;
import sg.ncs.kp.admin.service.LogService;
import sg.ncs.kp.uaa.client.util.SessionUtil;
import sg.ncs.kp.uaa.common.enums.UserLevelEnum;
import sg.ncs.kp.uaa.server.mapper.LoginLogMapper;
import sg.ncs.kp.uaa.server.po.LoginLog;

/**
 * @auther IVAN
 * @date 2022/9/8
 * @description
 */
@Service
public class LogServiceImpl implements LogService {

    @Autowired
    private LoginLogMapper loginLogMapper;

    @Autowired
    private OperateLogMapper operateLogMapper;

    @Override
    public Page<LoginLog> loginRecords(LoginLogQueryDTO queryDTO) {
        UserLevelEnum userLevel = SessionUtil.getUserLevel();
        if(!UserLevelEnum.TENANT_ADMIN.equals(userLevel)){
            return new Page<>(queryDTO.getPageNo(),queryDTO.getPageSize());
        }
        Page<LoginLog> page = new Page<>(queryDTO.getPageNo(), queryDTO.getPageSize());
        page = loginLogMapper.selectPage(page, Wrappers.<LoginLog>lambdaQuery()
                .eq(LoginLog :: getTenantId,SessionUtil.getTenantId())
                .like(StringUtils.isNotBlank(queryDTO.getUserName()), LoginLog::getUserName, queryDTO.getUserName())
                .like(StringUtils.isNotBlank(queryDTO.getEmail()), LoginLog::getEmail, queryDTO.getEmail())
                .like(StringUtils.isNotBlank(queryDTO.getPhone()), LoginLog::getPhone, queryDTO.getPhone())
                .like(StringUtils.isNotBlank(queryDTO.getIp()), LoginLog::getIp, queryDTO.getIp())
                .like(StringUtils.isNotBlank(queryDTO.getUserGroupName()), LoginLog::getUserGroupName, queryDTO.getUserGroupName())
                .ge(ObjectUtil.isNotNull(queryDTO.getLoginTimeBegin()), LoginLog::getLoginTime, queryDTO.getLoginTimeBegin())
                .ge(ObjectUtil.isNotNull(queryDTO.getLoginTimeEnd()), LoginLog::getLoginTime, queryDTO.getLoginTimeEnd())
                .orderByDesc(LoginLog::getLoginTime)
        );
        return page;
    }

    @Override
    public Page<OperateLog> operateRecords(OperateLogQueryDTO queryDTO) {
        UserLevelEnum userLevel = SessionUtil.getUserLevel();
        if(!UserLevelEnum.TENANT_ADMIN.equals(userLevel)){
            return new Page<>(queryDTO.getPageNo(),queryDTO.getPageSize());
        }
        Page<OperateLog> page = new Page<>(queryDTO.getPageNo(), queryDTO.getPageSize());
        page = operateLogMapper.selectPage(page, Wrappers.<OperateLog>lambdaQuery()
                .eq(OperateLog :: getTenantId,SessionUtil.getTenantId())
                .eq(StringUtils.isNotBlank(queryDTO.getUserName()), OperateLog::getUserName, queryDTO.getUserName())
                .eq(StringUtils.isNotBlank(queryDTO.getEmail()), OperateLog::getEmail, queryDTO.getEmail())
                .eq(StringUtils.isNotBlank(queryDTO.getPhone()), OperateLog::getPhone, queryDTO.getPhone())
                .eq(StringUtils.isNotBlank(queryDTO.getIp()), OperateLog::getIp, queryDTO.getIp())
                .like(StringUtils.isNotBlank(queryDTO.getOperateType()), OperateLog::getOperateType, queryDTO.getOperateType())
                .ge(ObjectUtil.isNotNull(queryDTO.getOperateTimeBegin()), OperateLog::getLogTime, queryDTO.getOperateTimeBegin())
                .ge(ObjectUtil.isNotNull(queryDTO.getOperateTimeEnd()), OperateLog::getLogTime, queryDTO.getOperateTimeEnd())
                .orderByDesc(OperateLog::getLogTime)
        );
        return page;
    }
}
