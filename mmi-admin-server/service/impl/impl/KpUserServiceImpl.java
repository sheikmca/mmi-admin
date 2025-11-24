package sg.ncs.kp.admin.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.file.FileNameUtil;
import cn.hutool.core.lang.Validator;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.poi.excel.ExcelReader;
import cn.hutool.poi.excel.ExcelUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.Feature;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.web.multipart.MultipartFile;
import sg.ncs.kp.admin.dto.*;
import sg.ncs.kp.admin.enums.*;
import sg.ncs.kp.admin.mapper.AdServerMapper;
import sg.ncs.kp.admin.po.AdServer;
import sg.ncs.kp.admin.po.Control2FA;
import sg.ncs.kp.admin.po.UploadRecord;
import sg.ncs.kp.admin.pojo.AdminConstants;
import sg.ncs.kp.admin.service.Control2FAService;
import sg.ncs.kp.admin.service.KpUserOnlineService;
import sg.ncs.kp.admin.service.KpUserService;
import sg.ncs.kp.admin.service.UploadRecordService;
import sg.ncs.kp.admin.strategy.NationStrategy;
import sg.ncs.kp.common.exception.pojo.ClientServiceException;
import sg.ncs.kp.common.exception.pojo.ServiceException;
import sg.ncs.kp.common.i18n.pojo.MessageEnum;
import sg.ncs.kp.common.i18n.util.MessageUtils;
import sg.ncs.kp.common.oss.component.OssComponent;
import sg.ncs.kp.common.uti.poi.ReadExcelUtlis;
import sg.ncs.kp.uaa.client.util.SessionUtil;
import sg.ncs.kp.uaa.common.constant.CommonConstant;
import sg.ncs.kp.uaa.common.dto.IdNameDTO;
import sg.ncs.kp.uaa.common.dto.PasswordPolicyDTO;
import sg.ncs.kp.uaa.common.dto.PermissionDTO;
import sg.ncs.kp.uaa.common.dto.UserDTO;
import sg.ncs.kp.uaa.common.dto.UserDetailDTO;
import sg.ncs.kp.uaa.common.dto.UserModifyDTO;
import sg.ncs.kp.uaa.common.dto.UserPasswordDTO;
import sg.ncs.kp.uaa.common.dto.UserQueryDTO;
import sg.ncs.kp.uaa.common.dto.UserUploadRecordQueryDTO;
import sg.ncs.kp.uaa.common.enums.StatusEnum;
import sg.ncs.kp.uaa.common.enums.UserLevelEnum;
import sg.ncs.kp.uaa.common.enums.UserTypeEnum;
import sg.ncs.kp.uaa.common.vo.UserGroupTreeVO;
import sg.ncs.kp.uaa.server.common.UaaServerMsgEnum;
import sg.ncs.kp.uaa.server.mapper.LoginLogMapper;
import sg.ncs.kp.uaa.server.mapper.RoleMapper;
import sg.ncs.kp.uaa.server.mapper.UserGroupMapper;
import sg.ncs.kp.uaa.server.mapper.UserMapper;
import sg.ncs.kp.uaa.server.po.*;
import sg.ncs.kp.uaa.server.service.*;
import sg.ncs.kp.uaa.server.utils.EntryptUtil;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @auther IVAN
 * @date 2022/8/20
 * @description
 */
@Slf4j
@Service
public class KpUserServiceImpl implements KpUserService {

    @Autowired
    private UserService userService;

    @Autowired
    private UserGroupService userGroupService;

    @Autowired
    private PasswordPolicyService passwordPolicyService;

    @Autowired
    private Map<String, NationStrategy> nationStrategy;

    @Autowired
    private PermissionService permissionService;

    @Autowired
    private UploadRecordService uploadRecordService;

    @Autowired
    private OssComponent ossComponent;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private MessageUtils messageUtils;

    @Autowired
    private RoleService roleService;

    @Autowired
    private UserGroupMapper userGroupMapper;

    @Autowired
    private LoginLogMapper loginLogMapper;

    @Autowired
    private UserRoleMappingService userRoleMappingService;

    @Autowired
    private RoleMapper roleMapper;

    @Autowired
    private AdServerMapper adServerMapper;

    @Autowired
    private Control2FAService control2FAService;

    @Autowired
    private KpUserOnlineService kpUserOnlineService;

    @Autowired
    private UserRelUserGroupMappingMapperService userRelUserGroupMappingMapperService;

    @Autowired
    private UserMapper userMapper;

    @Value("${oss.path}")
    private String ossPath;

    @Value("${admin.adDefaultRoleId}")
    private Integer roleId;

    @Value("${admin.batchUserMaxNum}")
    private Integer batchUserMaxNum;

    private static final String M = "M";
    private static final String N = "N";
    private static final String SUCCESS = "Success";
    private static final String FAILED = "Failed";
    private static final String ENABLE = "Enabled";
    private static final String DISABLE = "Disabled";


    @Override
    public IPage<UserDTO> selectList(UserQueryDTO userQueryDTO) {
        UserLevelEnum userLevel = SessionUtil.getUserLevel();
        if (UserLevelEnum.SUPER_ADMIN.equals(userLevel)) {
            userQueryDTO.setUserLevel(UserLevelEnum.TENANT_ADMIN.getValue());
        } else {
            userQueryDTO.setTenantId(SessionUtil.getTenantId());
            userQueryDTO.setUserLevel(UserLevelEnum.USER.getValue());
        }
        IPage<UserDTO> page = userService.selectList(userQueryDTO);
        List<UserDTO> records = page.getRecords();
        Map<String, Date> map = new HashMap<>();
        if (CollectionUtil.isNotEmpty(records)) {
            List<String> ids = records.stream().map(UserDTO::getId).collect(Collectors.toList());
            List<LoginLog> loginLogs = loginLogMapper.getUserLastLogin(ids);
            if (CollectionUtil.isNotEmpty(loginLogs)) {
                for (LoginLog loginLog : loginLogs) {
                    map.put(loginLog.getUserId(), loginLog.getLoginTime());
                }
            }
            for (UserDTO record : records) {
                record.setLastLoginTime(map.get(record.getId()));
            }
        }
        return page;
    }

    @Override
    public void add(UserModifyDTO userModifyDTO) {
        UserLevelEnum userLevel = SessionUtil.getUserLevel();
        if (UserLevelEnum.SUPER_ADMIN.equals(userLevel)) {
            throw new ClientServiceException(AdminMsgEnum.SUPER_ADMIN_CANNOT_OPERATE);
        }
        commonCheck(userModifyDTO);
        userModifyDTO.setCreatedId(SessionUtil.getUserId());
        userModifyDTO.setLastUpdatedId(SessionUtil.getUserId());
        userModifyDTO.setTenantId(SessionUtil.getUserSession().getTenantId());
        userModifyDTO.setLevel(UserLevelEnum.USER.getValue());
        userModifyDTO.setType(UserTypeEnum.NORMAL.getType());
        userService.add(userModifyDTO);
    }

    @Override
    public void addLDAPUser(String name, String mail, String userName, String adName, String mobile, String direction) {
        // Prepare Entity to save the new LADP user
        UserModifyDTO userModifyDTO = new UserModifyDTO();
        userModifyDTO.setName(name);
        userModifyDTO.setUserName(userName);
        userModifyDTO.setEmail(mail);
        if(StringUtils.isNotBlank(mobile)){
            String countryCode=mobile.substring(0,2);
            userModifyDTO.setCountryCode(countryCode);
            userModifyDTO.setPhone(mobile.substring(2));
        }
        commonCheck(userModifyDTO);
        AdServer adServer = adServerMapper.getByAdName(adName);
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("tenant_id", adServer.getTenantId());
        queryWrapper.eq("level", UserLevelEnum.TENANT_ADMIN.getValue());
        List<User> users = userMapper.selectList(Wrappers.<User>lambdaQuery()
                .eq(User::getTenantId, adServer.getTenantId())
                .eq(User::getLevel, UserLevelEnum.TENANT_ADMIN.getValue()));
        User user = new User();
        if(ObjectUtil.isNotEmpty(users)){
            user = users.get(0);
        }
        userModifyDTO.setCreatedId(user.getId());
        userModifyDTO.setLastUpdatedId(user.getId());
        userModifyDTO.setTenantId(adServer.getTenantId());
        userModifyDTO.setLevel(UserLevelEnum.USER.getValue());
        userModifyDTO.setType(UserTypeEnum.AD.getType());
        Set<Integer> roleIds = new HashSet<>();
        roleIds.add(roleId);
        userModifyDTO.setRoleIds(roleIds);
        if(ObjectUtil.isNotEmpty(direction)) {
            userModifyDTO.setUserGroupId(DirectionEnum.getIndexByKey(direction));
        }
        userService.add(userModifyDTO);
    }

    @Override
    public void update(UserModifyDTO userModifyDTO) {
        commonCheck(userModifyDTO);
        userModifyDTO.setLastUpdatedId(SessionUtil.getUserId());
        userModifyDTO.setTenantId(SessionUtil.getUserSession().getTenantId());
        userModifyDTO.setType(UserTypeEnum.NORMAL.getType());
        userService.update(userModifyDTO);
    }

    @Override
    public List<String> assignRole(AssignRoleDTO assignRole) {
        List<String> roleChangeUserId = new ArrayList<>();
        List<UserRoleMapping> roleMappings = new ArrayList<>();
        Integer roleId = assignRole.getRoleId();
        List<Role> roles = roleMapper.selectList(Wrappers.<Role>lambdaQuery().eq(Role::getTenantId, SessionUtil.getTenantId()));
        List<Integer> roleIds = roles.stream().map(Role::getId).collect(Collectors.toList());
        if(ObjectUtil.isEmpty(roleIds) || !roleIds.contains(assignRole.getRoleId())){
            throw new ClientServiceException(AdminMsgEnum.USER_ROLE_INVALID);
        }
        List<UserRoleMapping> userRoleMappings = userRoleMappingService.getByUserIds(assignRole.getUserIds());
        Map<String,Integer> userRoles = new HashMap<>();
        userRoles = userRoleMappings.stream().collect(Collectors.toMap(UserRoleMapping::getUserId, UserRoleMapping::getRoleId));
        userRoleMappingService.deleteBatchIds(assignRole.getUserIds());
        Iterator iterator = assignRole.getUserIds().iterator();
        while(iterator.hasNext()) {
            String userId = (String) iterator.next();
            UserRoleMapping userRoleMapping = new UserRoleMapping();
            userRoleMapping.setUserId(userId);
            userRoleMapping.setRoleId(roleId);
            roleMappings.add(userRoleMapping);
            if(ObjectUtil.isEmpty(userRoles.get(userId))|| !userRoles.get(userId).equals(roleId)){
                roleChangeUserId.add(userId);
            }
        }
        userRoleMappingService.saveBatch(roleMappings);
        return roleChangeUserId;
    }

    @Override
    public void updateLDAPUser(String name, String mail, String userName, String adName, String mobile) {
        // Prepare Entity to save the new LADP user
        User oldUser = userService.getUser(userName);
        UserModifyDTO userModifyDTO=BeanUtil.copyProperties(oldUser, UserModifyDTO.class);
        userModifyDTO.setName(name);
        userModifyDTO.setUserName(userName);
        userModifyDTO.setEmail(mail);
        if(StringUtils.isNotBlank(mobile)){
            String countryCode=mobile.substring(0,2);
            userModifyDTO.setCountryCode(countryCode);
            userModifyDTO.setPhone(mobile.substring(2));
        }
        commonCheck(userModifyDTO);
        AdServer adServer = adServerMapper.getByAdName(adName);
        List<User> users = userMapper.selectList(Wrappers.<User>lambdaQuery()
                .eq(User::getTenantId, adServer.getTenantId())
                .eq(User::getLevel, UserLevelEnum.TENANT_ADMIN.getValue()));
        User user = new User();
        if(ObjectUtil.isNotEmpty(users)){
            user = users.get(0);
        }
        userModifyDTO.setLastUpdatedId(user.getId());
        userModifyDTO.setTenantId(adServer.getTenantId());
        userModifyDTO.setLastUpdatedDate(DateUtil.date());
        userModifyDTO.setCreatedDate(DateUtil.date(userModifyDTO.getCreatedDate()));
        userModifyDTO.setType(UserTypeEnum.AD.getType());
        if(ObjectUtil.isNotEmpty(userModifyDTO.getPassword())) {
            userModifyDTO.setPassword(EntryptUtil.encryptSHA256Password(userModifyDTO.getPassword()));
        }
        User newUser = new User();
        BeanUtils.copyProperties(userModifyDTO, newUser);
        userMapper.updateUserById(newUser);
    }

    @Override
    public void delete(String id) {
        UserLevelEnum userLevel = SessionUtil.getUserLevel();
        if (!UserLevelEnum.USER.equals(userLevel)) {
            verifyUserChild(id);
        }
        if(SessionUtil.getUserId().equals(id)){
            throw new ClientServiceException(AdminMsgEnum.USER_NOT_OPERATE_SELF);
        }
        userService.delete(List.of(id));
        kpUserOnlineService.forceLogout(id);
    }

    @Override
    public void delete(List<String> ids) {
        UserLevelEnum userLevel = SessionUtil.getUserLevel();
        if (!UserLevelEnum.USER.equals(userLevel)) {
            for (String id : ids) {
                verifyUserChild(id);
            }
        }
        if(ids.contains(SessionUtil.getUserId())){
            throw new ClientServiceException(AdminMsgEnum.USER_NOT_OPERATE_SELF);
        }
        userService.delete(ids);

        // clear token
        for(String id: ids){
            kpUserOnlineService.forceLogout(id);
        }
    }

    private void verifyUserChild(String id) {
        long count = userService.count(Wrappers.<User>lambdaQuery().eq(User::getCreatedId, id));
        if (count > 0) {
            User user = userService.getById(id);
            throw new ClientServiceException(AdminMsgEnum.USER_HAVE_CHILD_USER, user.getUserName());
        }
    }

    @Override
    public void updateStatus(String id, Integer status) {
        if(SessionUtil.getUserId().equals(id)){
            throw new ClientServiceException(AdminMsgEnum.USER_NOT_OPERATE_SELF);
        }
        userService.updateStatus(id, StatusEnum.value(status));
    }

    @Override
    public UserGroupTreeVO group(String groupName) {
        return userGroupService.selectAllTree(SessionUtil.getUserSession().getTenantId(), groupName);
    }

    @Override
    public void changeGroup(ChangeGroupDTO changeGroupDTO) {
        for(String id:changeGroupDTO.getUserIds()) {
            userService.changeGroup(id, changeGroupDTO.getUserGroupId());
        }
    }

    @Override
    public void password(UserPasswordDTO userPasswordDTO) {
        try {
            userService.changePassword(userPasswordDTO.getUserId(), userPasswordDTO.getNewPassword(), userPasswordDTO.getOldPassword(), userPasswordDTO.getUserName());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        // force logout
        kpUserOnlineService.forceLogout(userPasswordDTO.getUserId());
    }

    @Override
    public List<PasswordPolicy> policy() {
        return passwordPolicyService.get(SessionUtil.getUserSession().getTenantId());
    }

    @Override
    public void policy(List<PasswordPolicyDTO> list) {
        List<PasswordPolicy> passwordPolicies = new ArrayList<>();
        list.stream().forEach(passwordPolicyDTO -> {
            PasswordPolicy passwordPolicy = new PasswordPolicy();
            BeanUtils.copyProperties(passwordPolicyDTO, passwordPolicy);
            passwordPolicy.setLastUpdatedId(SessionUtil.getUserId());
            passwordPolicy.setEnable(passwordPolicyDTO.getEnable());
            passwordPolicies.add(passwordPolicy);
        });
        passwordPolicyService.update(passwordPolicies);
    }

    @Override
    public void resetPassword(ResetPasswordDTO resetPassword) {
        try {
            userService.resetPassword(resetPassword.getId(), resetPassword.getPassword(), SessionUtil.getUserSession().getTenantId());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        // force logout
        kpUserOnlineService.forceLogout(resetPassword.getId());
    }

    @Override
    public UserDetailDTO get(String userId) {
        UserDTO userDTO = userService.get(userId);
        UserDetailDTO userDetailDTO = new UserDetailDTO();
        BeanUtils.copyProperties(userDTO, userDetailDTO);
        List<Permission> permissions = permissionService.getPermissions(userId, UserLevelEnum.byValue(userDTO.getLevel()));
        List<String> list = permissions.stream().map(Permission::getAuthorityKey).collect(Collectors.toList());
        userDetailDTO.setPermissions(list);
        userDetailDTO.setUserLevel(UserLevelEnum.byValue(userDetailDTO.getLevel()));
        return userDetailDTO;
    }

    @Override
    public String upload(MultipartFile file) {
        if (file.isEmpty()) {
            throw new ClientServiceException(AdminMsgEnum.FILE_IS_EMPTY);
        }
        String fileName = file.getOriginalFilename();
        String extName = FileNameUtil.extName(fileName);
        if (!("xlsx".equalsIgnoreCase(extName) || "xls".equalsIgnoreCase(extName))) {
            throw new ClientServiceException(AdminMsgEnum.UN_SUPPORT_FILE_FORMAT, "xlsx,xls");
        }
        try {
            ExcelReader reader = ExcelUtil.getReader(file.getInputStream(), 0);
            List<List<Object>> list = reader.read(1);
            if (CollectionUtil.isEmpty(list)) {
                throw new ClientServiceException(AdminMsgEnum.EXCEL_NO_DATA);
            }
            if (list.size() > 10000) {
                throw new ClientServiceException(AdminMsgEnum.EXCEL_ROW_LIMIT, "10000");
            }
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            throw new ClientServiceException(AdminMsgEnum.FILE_LOAD_FAIL);
        }

        //save in oss
        try {
            String path = ossComponent.upload(file.getBytes(), fileName, null, SessionUtil.getUserId());
            redisTemplate.opsForValue().set(CommonConstant.BATCH_ADD_USER_UPLOAD_KEY + path, fileName, 1, TimeUnit.HOURS);
            return path;
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

    @Override
    public Page<UploadRecord> uploadRecord(UserUploadRecordQueryDTO userUploadRecordQueryDTO) {
        Page<UploadRecord> page = new Page<>(userUploadRecordQueryDTO.getPageNo(), userUploadRecordQueryDTO.getPageSize());
        Page<UploadRecord> recordPage = uploadRecordService.page(page, Wrappers.<UploadRecord>lambdaQuery()
                .eq(UploadRecord::getCreatedId, SessionUtil.getUserId())
                .ge(userUploadRecordQueryDTO.getStartDate() != null, UploadRecord::getCreatedDate, userUploadRecordQueryDTO.getStartDate())
                .ge(userUploadRecordQueryDTO.getEndDate() != null, UploadRecord::getCreatedDate, userUploadRecordQueryDTO.getEndDate())
                .orderByDesc(UploadRecord::getCreatedDate)
        );
        return recordPage;
    }

    @Override
    public Page<UserUploadViewDTO> view(UserViewQueryDTO userViewQueryDTO) {
        String path = userViewQueryDTO.getPath();
        Integer pageNo = userViewQueryDTO.getPageNo();
        Integer pageSize = userViewQueryDTO.getPageSize();
        Object value = redisTemplate.opsForValue().get(path);
        if (value == null) {
            try (InputStream inputStream = ossComponent.downloadInputStream(path)) {
                ExcelReader reader = ExcelUtil.getReader(inputStream, 0);
                List<List<Object>> list = reader.read(1);
                value = JSONObject.toJSONString(list);
                redisTemplate.opsForValue().set(path, value, 1, TimeUnit.HOURS);
            } catch (Exception e) {
                log.error("get upload channel file failed");
                log.error(e.getMessage(), e);
                throw new ClientServiceException(AdminMsgEnum.USER_FILE_ERROR);
            }
        }
        List<List<Object>> list = JSON.parseObject(value.toString(), List.class, Feature.OrderedField);
        List<UserUploadViewDTO> resultList;
        try (InputStream inputStream = ossComponent.downloadInputStream(path)) {
            ReadExcelUtlis<UserUploadViewDTO> readExcelUtils = new ReadExcelUtlis<>(UserUploadViewDTO.class);
            resultList = readExcelUtils.importExcel(null, inputStream, pageNo, pageSize, path.split("\\.")[1]);
            Page<UserUploadViewDTO> page = new Page<>(pageNo, pageSize, Long.parseLong(String.valueOf(list.size())));
            page.setRecords(resultList);
            return page;
        } catch (Exception e) {
            log.error("get upload user file failed");
            log.error(e.getMessage(), e);
            throw new ServiceException(AdminMsgEnum.USER_FILE_ERROR);
        }
    }
    @Override
    public BatchUserResultDTO batchAdd(BatchUserDTO batchUserDTO){
        List<UserInfoDTO> userInfos = batchUserDTO.getUserInfos();
        if(userInfos.size() > batchUserMaxNum){
            throw new ClientServiceException(AdminMsgEnum.USER_FILE_ERROR,batchUserMaxNum+"");
        }
        BatchUserResultDTO result = new BatchUserResultDTO();
        result.setTotal(userInfos.size());
        List<AddUserMessageDTO> messages = new ArrayList<>();
        int successNum = this.batchAddUser(userInfos,messages);
        int failedNum = userInfos.size()-successNum;
        result.setFail(failedNum);
        result.setSuccess(successNum);
        result.setMessages(messages);
        return result;
    }

    private AddUserMessageDTO setMessage(Integer index, AddUserStatusEnum statusEnum, String messageStr){
        AddUserMessageDTO message = new AddUserMessageDTO();
        message.setIndex(index);
        message.setStatus(statusEnum.getValue());
        message.setMessage(messageStr);
        return message;
    }

    private Integer batchAddUser(List<UserInfoDTO> userInfos, List<AddUserMessageDTO> messages){
        Integer successNum = 0;
        List<Role> roles = roleMapper.selectList(Wrappers.<Role>lambdaQuery().eq(Role::getTenantId, SessionUtil.getTenantId()));
       List<Integer> roleIds = new ArrayList<>();
        if (CollectionUtil.isNotEmpty(roles)) {
            roleIds = roles.stream().map(Role::getId).collect(Collectors.toList());
        }
        for (UserInfoDTO userInfo : userInfos) {
            if (StrUtil.isBlank(userInfo.getUserName())) {
                messages.add(setMessage(userInfo.getIndex(),AddUserStatusEnum.FAIL,messageUtils.getMessage(AdminMsgEnum.USERNAME_EMPTY.val())));
                continue;
            }
            String name = userInfo.getUserName();
            Pattern p = Pattern.compile(CommonConstant.SPECIAL_CHAR);
            Matcher m = p.matcher(name);
            if (m.find()) {
                messages.add(setMessage(userInfo.getIndex(),AddUserStatusEnum.FAIL,messageUtils.getMessage(UaaServerMsgEnum.USERNAME_ILLEGAL.val())));
                continue;
            }

            if (userInfo.getUserName().length() > 50) {
                messages.add(setMessage(userInfo.getIndex(),AddUserStatusEnum.FAIL,messageUtils.getMessage(AdminMsgEnum.FIELD_LENGTH_ILLEGAL.val(), "User Name", 50)));
                continue;
            }

            if (StringUtils.isBlank(userInfo.getName())) {
                messages.add(setMessage(userInfo.getIndex(),AddUserStatusEnum.FAIL,messageUtils.getMessage(AdminMsgEnum.FULL_NAME_EMPTY.val())));
                continue;
            }

            if (userInfo.getName().length() > 25) {
                messages.add(setMessage(userInfo.getIndex(),AddUserStatusEnum.FAIL,messageUtils.getMessage(AdminMsgEnum.FIELD_LENGTH_ILLEGAL.val(), "Name", 25)));
                continue;
            }

            //verify email
            if (StringUtils.isNotBlank(userInfo.getEmail())) {
                if (!Validator.isEmail(userInfo.getEmail())) {
                    messages.add(setMessage(userInfo.getIndex(),AddUserStatusEnum.FAIL,messageUtils.getMessage(AdminMsgEnum.EMAIL_INVALID.val())));
                    continue;
                }
            }

            //verify phone
            if (StringUtils.isNotBlank(userInfo.getPhone())) {
                if (StringUtils.isNotBlank(userInfo.getCountryCode())) {
                    NationStrategy nationStrategy = this.nationStrategy.get(userInfo.getCountryCode());
                    if (nationStrategy == null) {
                        messages.add(setMessage(userInfo.getIndex(),AddUserStatusEnum.FAIL,messageUtils.getMessage(AdminMsgEnum.COUNTRY_CODE_INVALID.val())));
                        continue;
                    }
                    if (!nationStrategy.verifyPhone(userInfo.getPhone())) {
                        messages.add(setMessage(userInfo.getIndex(),AddUserStatusEnum.FAIL,messageUtils.getMessage(AdminMsgEnum.PHONE_INVALID.val())));
                        continue;
                    }
                } else {
                    messages.add(setMessage(userInfo.getIndex(),AddUserStatusEnum.FAIL,messageUtils.getMessage(AdminMsgEnum.COUNTRY_CODE_EMPTY.val())));
                    continue;
                }
            }

            boolean startIsNull = userInfo.getValidityStartTime() != null;
            boolean endIsNull = userInfo.getValidityEndTime() != null;
            if ((startIsNull ^ endIsNull)) {
                messages.add(setMessage(userInfo.getIndex(),AddUserStatusEnum.FAIL,messageUtils.getMessage(AdminMsgEnum.VALIDITY_TIME_LACK.val())));
                continue;
            }
            if (startIsNull && endIsNull) {
                if (userInfo.getValidityStartTime().after(userInfo.getValidityEndTime()) || userInfo.getValidityStartTime().equals(userInfo.getValidityEndTime())) {
                    messages.add(setMessage(userInfo.getIndex(),AddUserStatusEnum.FAIL,messageUtils.getMessage(AdminMsgEnum.VALIDITY_TIME_ERROR.val())));
                    continue;
                }
            }

            boolean ipStartBool = StrUtil.isNotBlank(userInfo.getIpStart());
            boolean ipEndBool = StrUtil.isNotBlank(userInfo.getIpEnd());

            if (ipStartBool && ipEndBool) {
                if (Validator.isIpv4(userInfo.getIpStart()) && Validator.isIpv4(userInfo.getIpEnd())) {
                    long ipStartNum = getIpNum(userInfo.getIpStart());
                    long ipEndNum = getIpNum(userInfo.getIpEnd());
                    if (ipStartNum > ipEndNum) {
                        messages.add(setMessage(userInfo.getIndex(),AddUserStatusEnum.FAIL,messageUtils.getMessage(AdminMsgEnum.IP_START_GT_IP_END.val())));
                        continue;
                    }
                } else {
                    messages.add(setMessage(userInfo.getIndex(),AddUserStatusEnum.FAIL,messageUtils.getMessage(AdminMsgEnum.IP_INVALID.val())));
                    continue;
                }
            } else {
                if (ipStartBool ^ ipEndBool) {
                    if (ipStartBool) {
                        messages.add(setMessage(userInfo.getIndex(),AddUserStatusEnum.FAIL,messageUtils.getMessage(AdminMsgEnum.IP_END_LACK.val())));
                    } else {
                        messages.add(setMessage(userInfo.getIndex(),AddUserStatusEnum.FAIL,messageUtils.getMessage(AdminMsgEnum.IP_START_LACK.val())));
                    }
                    continue;
                }
            }

            //verify role
            boolean flag = true;
            Set<Integer> roleIdSet = new HashSet<>(8);
            if (ObjectUtil.isNotEmpty(userInfo.getRoleIds())) {
                Integer roleId = new ArrayList<>(userInfo.getRoleIds()).get(0);
                if (!roleIds.contains(roleId)) {
                    messages.add(setMessage(userInfo.getIndex(),AddUserStatusEnum.FAIL,messageUtils.getMessage(AdminMsgEnum.USER_ROLE_INVALID.val())));
                    continue;
                }
                roleIdSet.add(roleId);
            } else {
                messages.add(setMessage(userInfo.getIndex(),AddUserStatusEnum.FAIL,messageUtils.getMessage(AdminMsgEnum.ROLE_IS_NULL.val())));
                continue;
            }
            UserModifyDTO userModifyDTO = BeanUtil.copyProperties(userInfo, UserInfoDTO.class);
            if (ObjectUtil.isNotEmpty(userModifyDTO.getUserGroupId())) {
                UserGroup userGroup = userGroupMapper.selectOne(Wrappers.<UserGroup>lambdaQuery()
                        .eq(UserGroup::getId, userModifyDTO.getUserGroupId())
                        .eq(UserGroup::getTenantId, SessionUtil.getTenantId())
                );
                if (userGroup == null) {
                    messages.add(setMessage(userInfo.getIndex(),AddUserStatusEnum.FAIL,messageUtils.getMessage(AdminMsgEnum.USER_GROUP_INVALID.val())));
                    continue;
                }
                userModifyDTO.setUserGroupId(userGroup.getId());
            }
            if (roleIdSet.size() > 0) {
                userModifyDTO.setRoleIds(roleIdSet);
            }
            userModifyDTO.setTenantId(SessionUtil.getTenantId());
            try {
                if (userService.add(userModifyDTO)) {
                    messages.add(setMessage(userInfo.getIndex(),AddUserStatusEnum.SUCCESS,""));
                    successNum++;
                }
            } catch (ClientServiceException e) {
                messages.add(setMessage(userInfo.getIndex(),AddUserStatusEnum.FAIL,messageUtils.getMessage(e.getCode().val())));
            }
        }
        return successNum;
    }

    @Override
    public UploadResultDTO importFile(String path) {
        Object fileName = redisTemplate.opsForValue().get(CommonConstant.BATCH_ADD_USER_UPLOAD_KEY + path);
        if (fileName == null) {
            throw new ClientServiceException(MessageEnum.OSS_UPLOAD_FILE_MISS);
        }
        byte[] bytes = ossComponent.downloadByte(path);

        //create_upload_record
        UploadRecord uploadRecord = new UploadRecord();
        uploadRecord.setUploadKey(OspBatchUploadRecordKeyEnum.USER_BATCH_UPLOAD.getKey());
        uploadRecord.setFileName(fileName.toString());
        uploadRecord.setFileSize(bytes.length);
        uploadRecord.setPath(path);
        String userId = SessionUtil.getUserId();
        uploadRecord.setCreatedId(userId);
        uploadRecord.setLastUpdatedId(userId);


        List<UserUploadViewDTO> resultList;
        try (InputStream inputStream = ossComponent.downloadInputStream(path)) {
            ReadExcelUtlis<UserUploadViewDTO> readExcelUtils = new ReadExcelUtlis<>(UserUploadViewDTO.class);
            resultList = readExcelUtils.importExcel(null, inputStream, null, null, path.split("\\.")[1]);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            log.error("get upload user file failed");
            throw new ClientServiceException(AdminMsgEnum.USER_FILE_ERROR);
        }

        UploadResultDTO uploadResultDTO = new UploadResultDTO();
        uploadResultDTO.setTotal(resultList.size());
        try {
            Integer count = importUploadBatchAddUser(resultList, path);
            uploadResultDTO.setSuccess(count);
            uploadResultDTO.setFail(resultList.size() - count);
            if (count == 0) {
                uploadRecord.setStatus(UploadStatusEnum.FAIL.getKey());
            } else if (count > 0 && count < resultList.size()) {
                uploadRecord.setStatus(UploadStatusEnum.ABNORMAL.getKey());
            } else if (count == resultList.size()) {
                uploadRecord.setStatus(UploadStatusEnum.SUCCESS.getKey());
            }
            uploadRecordService.save(uploadRecord);
        } catch (ClientServiceException serviceException) {
            uploadRecord.setStatus(UploadStatusEnum.FAIL.getKey());
            uploadRecord.setRemark(messageUtils.getMessage(serviceException.getCode().val()));
            uploadRecordService.save(uploadRecord);
            throw new ClientServiceException(AdminMsgEnum.USER_FILE_ERROR);
        }
        return uploadResultDTO;
    }

    @Override
    public void downloadUploadFile(HttpServletResponse response, String path, String fileName) {
        try (InputStream inputStream = ossComponent.downloadInputStream(path)) {
            String filePrefix = fileName.substring(0, fileName.indexOf(".") + 1);
            String extension = path.split("\\.")[1];
            Workbook workbook = null;
            if ("xls".equals(extension)) {
                workbook = new HSSFWorkbook(inputStream);
            } else {
                workbook = new XSSFWorkbook(inputStream);
            }
            // 设置输出的格式
            response.setHeader("Content-disposition", "attachment;filename=" + filePrefix + extension + ";" + "filename*=utf-8''" + filePrefix + extension);
            response.addHeader("Access-Control-Expose-Headers", "Content-Disposition");
            response.setContentType("application/vnd.ms-excel;charset=UTF-8");
            workbook.write(response.getOutputStream());
            workbook.close();
        } catch (IOException e) {
            log.error("download file error {}", path);
            throw new ServiceException(AdminMsgEnum.USER_DOWNLOAD_FILE_FAIL);
        }
    }

    @Override
    public void export(List<String> ids, HttpServletResponse response) {
        if (CollectionUtil.isEmpty(ids)) {
            throw new ClientServiceException(AdminMsgEnum.IDS_EMPTY);
        }
        UserQueryDTO userQueryDTO = new UserQueryDTO();
        userQueryDTO.setIds(ids);
        userQueryDTO.setPageNo(1);
        userQueryDTO.setPageSize(ids.size());
        IPage<UserDTO> page = userService.selectList(userQueryDTO);
        if (CollectionUtil.isNotEmpty(page.getRecords())) {
            List<UserExportDTO> resultList = new ArrayList<>();
            Map<String, Date> map = new HashMap<>();
            List<LoginLog> loginLogs = loginLogMapper.getUserLastLogin(ids);
            if (CollectionUtil.isNotEmpty(loginLogs)) {
                for (LoginLog loginLog : loginLogs) {
                    map.put(loginLog.getUserId(), loginLog.getLoginTime());
                }
            }
            for (UserDTO user : page.getRecords()) {
                user.setLastLoginTime(map.get(user.getId()));
                UserExportDTO userExportDTO = new UserExportDTO();
                userExportDTO.setUserName(user.getUserName());
                userExportDTO.setStatus(StatusEnum.ACTIVE.getStatus() == user.getStatus() ? ENABLE : DISABLE);
                userExportDTO.setEmail(user.getEmail());
                userExportDTO.setPhone(user.getPhone());
                userExportDTO.setUserGroupName(user.getUserGroupName());
                List<IdNameDTO> role = user.getRole();
                if (CollectionUtil.isNotEmpty(role)) {
                    Object[] roleArray = role.stream().map(IdNameDTO::getName).toArray();
                    userExportDTO.setRoleName(Arrays.toString(roleArray));
                }
                if (null != user.getValidityStartTime()) {
                    userExportDTO.setValidityStartTime(DateUtil.format(user.getValidityStartTime(), DatePattern.NORM_DATETIME_PATTERN));
                }
                if (null != user.getValidityEndTime()) {
                    userExportDTO.setValidityEndTime(DateUtil.format(user.getValidityEndTime(), DatePattern.NORM_DATETIME_PATTERN));
                }
                if (null != user.getLastLoginTime()) {
                    userExportDTO.setLastLogin(DateUtil.format(user.getLastLoginTime(), DatePattern.NORM_DATETIME_PATTERN));
                }
                resultList.add(userExportDTO);
            }
            String fileName = "exportUser.xlsx";

            ReadExcelUtlis<UserExportDTO> readExcelUtils = new ReadExcelUtlis<>(UserExportDTO.class);
            try {
                readExcelUtils.exportExcel(resultList, "exportUser", 1, response, fileName);
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }
        }
    }

    @Override
    public void updateMyself(UserUpdateProfileDTO user) {
        if (StringUtils.isNotBlank(user.getPhone())) {
            Long phone = userService.count(Wrappers.<User>lambdaQuery()
                    .eq(User::getPhone, user.getPhone())
                    .ne(User::getId, user.getId())
            );
            if (phone > 0) {
                throw new ClientServiceException(UaaServerMsgEnum.PHONE_DUPLICATE);
            }
            if (StringUtils.isBlank(user.getCountryCode())) {
                throw new ClientServiceException(AdminMsgEnum.COUNTRY_CODE_EMPTY);
            }
            NationStrategy nationStrategy = this.nationStrategy.get(user.getCountryCode());
            if (nationStrategy == null) {
                throw new ClientServiceException(AdminMsgEnum.COUNTRY_CODE_INVALID);
            }
            if (!nationStrategy.verifyPhone(user.getPhone())) {
                throw new ClientServiceException(AdminMsgEnum.PHONE_INVALID);
            }
        }
        if (StringUtils.isNotBlank(user.getEmail())) {
            Long email = userService.count(Wrappers.<User>lambdaQuery()
                    .eq(User::getEmail, user.getEmail())
                    .ne(User::getId, user.getId())
            );
            if (email > 0) {
                throw new ClientServiceException(UaaServerMsgEnum.EMAIL_DUPLICATE);
            }
            if (StringUtils.isNotBlank(user.getEmail()) && !Validator.isEmail(user.getEmail())) {
                throw new ClientServiceException(AdminMsgEnum.EMAIL_INVALID);
            }
        }
        User userPO = new User();
        userPO.setId(user.getId());
        userPO.setName(user.getFullName());
        userPO.setPhone(user.getPhone());
        userPO.setEmail(user.getEmail());
        userPO.setCountryCode(user.getCountryCode());
        userPO.setLastUpdatedId(SessionUtil.getUserId());
        userService.updateUserById(userPO);
    }

    @Override
    public Set<String> getUserIdsByRoleId(Set<Integer> roleIds) {
        List<UserRoleMapping> list = userRoleMappingService.list(Wrappers.<UserRoleMapping>lambdaQuery()
                .in(UserRoleMapping::getRoleId, roleIds)
        );
        Set<String> userIds = list.stream().map(UserRoleMapping::getUserId).collect(Collectors.toSet());
        return userIds;
    }

    @Override
    public Set<String> getUserIdsByGroupId(Set<Integer> groupIds) {
        List<UserRelUserGroupMapping> list = userRelUserGroupMappingMapperService.list(Wrappers.<UserRelUserGroupMapping>lambdaQuery()
                .in(UserRelUserGroupMapping::getUserGroupId, groupIds));
        Set<String> userIds = list.stream().map(UserRelUserGroupMapping::getUserId).collect(Collectors.toSet());
        return userIds;
    }

    private void commonCheck(UserModifyDTO userModifyDTO) {

        //verify field length
        if (StringUtils.isNotBlank(userModifyDTO.getUserName()) && userModifyDTO.getUserName().length() > 50) {
            throw new ClientServiceException(AdminMsgEnum.FIELD_LENGTH_ILLEGAL, "User Name", "50");
        }
        if (StringUtils.isNotBlank(userModifyDTO.getName()) && userModifyDTO.getName().length() > 25) {
            throw new ClientServiceException(AdminMsgEnum.FIELD_LENGTH_ILLEGAL, "Full Name", "25");
        }
        //verify phone
        if (StringUtils.isNotBlank(userModifyDTO.getPhone())) {
            if (StringUtils.isBlank(userModifyDTO.getCountryCode())) {
                throw new ClientServiceException(AdminMsgEnum.COUNTRY_CODE_EMPTY);
            } else {
                NationStrategy nationStrategy = this.nationStrategy.get(userModifyDTO.getCountryCode());
                if (nationStrategy == null) {
                    throw new ClientServiceException(AdminMsgEnum.COUNTRY_CODE_INVALID);
                }
                if (!nationStrategy.verifyPhone(userModifyDTO.getPhone())) {
                    throw new ClientServiceException(AdminMsgEnum.PHONE_INVALID);
                }
            }
        }

        //verify email
        if (StringUtils.isNotBlank(userModifyDTO.getEmail()) && !Validator.isEmail(userModifyDTO.getEmail())) {
            throw new ClientServiceException(AdminMsgEnum.EMAIL_INVALID);
        }

        //verify validity period
        Date startTime = userModifyDTO.getValidityStartTime();
        Date endTime = userModifyDTO.getValidityEndTime();
        boolean startTimeIsNull = startTime != null;
        boolean endTimeIsNull = endTime != null;
        if (startTimeIsNull ^ endTimeIsNull) {
            throw new ClientServiceException(AdminMsgEnum.VALIDITY_TIME_LACK);
        }
        if (startTimeIsNull && startTime.getTime() >= endTime.getTime()) {
            throw new ClientServiceException(AdminMsgEnum.VALIDITY_TIME_ERROR);
        }

        //verify ip
        String ipStart = userModifyDTO.getIpStart();
        String ipEnd = userModifyDTO.getIpEnd();
        boolean ipStartIsNull = StringUtils.isNotBlank(ipStart);
        boolean ipEndIsNull = StringUtils.isNotBlank(ipEnd);
        if (ipStartIsNull ^ ipEndIsNull) {
            if (ipStartIsNull) {
                throw new ClientServiceException(AdminMsgEnum.IP_END_LACK);
            }
            throw new ClientServiceException(AdminMsgEnum.IP_START_LACK);
        }
        if (ipStartIsNull) {
            if (Validator.isIpv4(ipStart) && Validator.isIpv4(ipEnd)) {
                long ipStartNum = getIpNum(ipStart);
                long ipEndNum = getIpNum(ipEnd);
                if (ipStartNum > ipEndNum) {
                    throw new ClientServiceException(AdminMsgEnum.IP_START_GT_IP_END);
                }
            } else {
                throw new ClientServiceException(AdminMsgEnum.IP_INVALID);
            }
        }
    }

    private long getIpNum(String ipAddress) {
        String[] ip = ipAddress.split("\\.");
        long a = Integer.parseInt(ip[0]);
        long b = Integer.parseInt(ip[1]);
        long c = Integer.parseInt(ip[2]);
        long d = Integer.parseInt(ip[3]);
        long ipNum = a * 256 * 256 * 256 + b * 256 * 256 + c * 256 + d;
        return ipNum;
    }

    public Integer importUploadBatchAddUser(List<UserUploadViewDTO> resultList, String path) {

        //userName Duplicate detection
        //Map<String, List<UserUploadViewDTO>> usernameMap = resultList.stream().collect(Collectors.groupingBy(UserUploadViewDTO::getUserName));
        Integer row = 1;

        HashMap<String, String> map = new HashMap<>();
        List<UserModifyDTO> userList = new ArrayList<>(resultList.size());
        List<Role> roles = roleMapper.selectList(Wrappers.<Role>lambdaQuery().eq(Role::getTenantId, SessionUtil.getTenantId()));
        Map<String, Integer> roleMap = new HashMap<>();
        if (CollectionUtil.isNotEmpty(roles)) {
            roleMap = roles.stream().collect(Collectors.toMap(Role::getName, Role::getId));
        }
        for (UserUploadViewDTO userUploadViewDTO : resultList) {
            row++;
            if (StrUtil.isBlank(userUploadViewDTO.getUserName())) {
                map.put(M + row, FAILED);
                map.put(N + row, messageUtils.getMessage(AdminMsgEnum.USERNAME_EMPTY.val()));
                continue;
            }
            String name = userUploadViewDTO.getUserName();
            Pattern p = Pattern.compile(CommonConstant.SPECIAL_CHAR);
            Matcher m = p.matcher(name);
            if (m.find()) {
                map.put(M + row, FAILED);
                map.put(N + row, messageUtils.getMessage(UaaServerMsgEnum.USERNAME_ILLEGAL.val()));
                continue;
            }

/*            if(usernameMap.get(userUploadViewDTO.getUserName()).size() > 1){
                map.put(M + row, FAILED);
                map.put(N + row, messageUtils.getMessage(UaaServerMsgEnum.USERNAME_DUPLICATE.val()));
                continue;
            }*/

            if (userUploadViewDTO.getUserName().length() > 50) {
                map.put(M + row, FAILED);
                map.put(N + row, messageUtils.getMessage(AdminMsgEnum.FIELD_LENGTH_ILLEGAL.val(), "User Name", 50));
                continue;
            }

            if (StringUtils.isBlank(userUploadViewDTO.getFullName())) {
                map.put(M + row, FAILED);
                map.put(N + row, messageUtils.getMessage(AdminMsgEnum.FULL_NAME_EMPTY.val()));
                continue;
            }

            if (userUploadViewDTO.getFullName().length() > 25) {
                map.put(M + row, FAILED);
                map.put(N + row, messageUtils.getMessage(AdminMsgEnum.FIELD_LENGTH_ILLEGAL.val(), "Full Name", 25));
                continue;
            }

            //verify email
            if (StringUtils.isNotBlank(userUploadViewDTO.getEmail())) {
                if (!Validator.isEmail(userUploadViewDTO.getEmail())) {
                    map.put(M + row, FAILED);
                    map.put(N + row, messageUtils.getMessage(AdminMsgEnum.EMAIL_INVALID.val()));
                    continue;
                }
            }

            //verify phone
            if (StringUtils.isNotBlank(userUploadViewDTO.getPhone())) {
                if (StringUtils.isNotBlank(userUploadViewDTO.getCountryCode())) {
                    NationStrategy nationStrategy = this.nationStrategy.get(userUploadViewDTO.getCountryCode());
                    if (nationStrategy == null) {
                        map.put(M + row, FAILED);
                        map.put(N + row, messageUtils.getMessage(AdminMsgEnum.COUNTRY_CODE_INVALID.val()));
                        continue;
                    }
                    if (!nationStrategy.verifyPhone(userUploadViewDTO.getPhone())) {
                        map.put(M + row, FAILED);
                        map.put(N + row, messageUtils.getMessage(AdminMsgEnum.PHONE_INVALID.val()));
                        continue;
                    }
                } else {
                    map.put(M + row, FAILED);
                    map.put(N + row, messageUtils.getMessage(AdminMsgEnum.COUNTRY_CODE_EMPTY.val()));
                    continue;
                }
            }

            boolean startIsNull = userUploadViewDTO.getValidityStartTime() != null;
            boolean endIsNull = userUploadViewDTO.getValidityEndTime() != null;
            if ((startIsNull ^ endIsNull)) {
                map.put(M + row, FAILED);
                map.put(N + row, messageUtils.getMessage(AdminMsgEnum.VALIDITY_TIME_LACK.val()));
                continue;
            }
            if (startIsNull && endIsNull) {
                if (userUploadViewDTO.getValidityStartTime().isAfter(userUploadViewDTO.getValidityEndTime()) || userUploadViewDTO.getValidityStartTime().isEqual(userUploadViewDTO.getValidityEndTime())) {
                    map.put(M + row, FAILED);
                    map.put(N + row, messageUtils.getMessage(AdminMsgEnum.VALIDITY_TIME_ERROR.val()));
                    continue;
                }
            }

            boolean ipStartBool = StrUtil.isNotBlank(userUploadViewDTO.getIpStart());
            boolean ipEndBool = StrUtil.isNotBlank(userUploadViewDTO.getIpEnd());

            if (ipStartBool && ipEndBool) {
                if (Validator.isIpv4(userUploadViewDTO.getIpStart()) && Validator.isIpv4(userUploadViewDTO.getIpEnd())) {
                    long ipStartNum = getIpNum(userUploadViewDTO.getIpStart());
                    long ipEndNum = getIpNum(userUploadViewDTO.getIpEnd());
                    if (ipStartNum > ipEndNum) {
                        map.put(M + row, FAILED);
                        map.put(N + row, messageUtils.getMessage(AdminMsgEnum.IP_START_GT_IP_END.val()));
                        continue;
                    }
                } else {
                    map.put(M + row, FAILED);
                    map.put(N + row, messageUtils.getMessage(AdminMsgEnum.IP_INVALID.val()));
                    continue;
                }
            } else {
                if (ipStartBool ^ ipEndBool) {
                    map.put(M + row, FAILED);
                    if (ipStartBool) {
                        map.put(N + row, messageUtils.getMessage(AdminMsgEnum.IP_END_LACK.val()));
                    } else {
                        map.put(N + row, messageUtils.getMessage(AdminMsgEnum.IP_START_LACK.val()));
                    }
                    continue;
                }
            }

            //verify role
            boolean flag = true;
            Set<Integer> roleIdSet = new HashSet<>(8);
            if (StringUtils.isNotBlank(userUploadViewDTO.getRole())) {
                String[] roleArray = userUploadViewDTO.getRole().split(",");
                for (String s : roleArray) {
                    if (!roleMap.containsKey(s.trim())) {
                        map.put(M + row, FAILED);
                        map.put(N + row, messageUtils.getMessage(AdminMsgEnum.USER_ROLE_INVALID.val()));
                        flag = false;
                        break;
                    }
                    roleIdSet.add(roleMap.get(s.trim()));
                }
            } else {
                map.put(M + row, FAILED);
                map.put(N + row, messageUtils.getMessage(AdminMsgEnum.ROLE_IS_NULL.val()));
                continue;
            }


            if (flag) {
                UserModifyDTO userModifyDTO = new UserModifyDTO();
                userModifyDTO.setUserName(userUploadViewDTO.getUserName());
                userModifyDTO.setName(userUploadViewDTO.getFullName());
                userModifyDTO.setEmail(userUploadViewDTO.getEmail());
                userModifyDTO.setPhone(userUploadViewDTO.getPhone());
                if (startIsNull) {
                    Date startTime = Date.from(userUploadViewDTO.getValidityStartTime().atZone(ZoneId.systemDefault()).toInstant());
                    userModifyDTO.setValidityStartTime(startTime);
                }
                if (endIsNull) {
                    Date endTime = Date.from(userUploadViewDTO.getValidityEndTime().atZone(ZoneId.systemDefault()).toInstant());
                    userModifyDTO.setValidityEndTime(endTime);
                }
                if (StringUtils.isNotBlank(userUploadViewDTO.getUserGroupName())) {
                    UserGroup userGroup = userGroupMapper.selectOne(Wrappers.<UserGroup>lambdaQuery()
                            .eq(UserGroup::getName, userUploadViewDTO.getUserGroupName())
                            .eq(UserGroup::getTenantId, SessionUtil.getTenantId())
                    );
                    if(userGroup == null){
                        map.put(M + row, FAILED);
                        map.put(N + row, messageUtils.getMessage(AdminMsgEnum.USER_GROUP_INVALID.val()));
                        continue;
                    }
                    userModifyDTO.setUserGroupId(userGroup.getId());
                }
                if (roleIdSet.size() > 0) {
                    userModifyDTO.setRoleIds(roleIdSet);
                }
                userModifyDTO.setDescription(userUploadViewDTO.getDescription());
                userModifyDTO.setCountryCode(userUploadViewDTO.getCountryCode());
                userModifyDTO.setIpStart(userUploadViewDTO.getIpStart());
                userModifyDTO.setIpEnd(userUploadViewDTO.getIpEnd());
                userModifyDTO.setTenantId(SessionUtil.getTenantId());
                userModifyDTO.setRow(row);
                userList.add(userModifyDTO);
            }
        }
        int count = 0;
        //batch add
        for (UserModifyDTO userModifyDTO : userList) {
            try {
                if (userService.add(userModifyDTO)) {
                    map.put(M + userModifyDTO.getRow(), SUCCESS);
                    count++;
                }
            } catch (ClientServiceException e) {
                map.put(M + userModifyDTO.getRow(), FAILED);
                map.put(N + userModifyDTO.getRow(), messageUtils.getMessage(e.getCode().val()));
            }
        }
        updateExcelStatusCellValue(path, map);
        return count;
    }

    public void updateExcelStatusCellValue(String filePath, HashMap<String, String> statusAndMessageMap) {
        if (ObjectUtils.isEmpty(statusAndMessageMap.isEmpty())) {
            return;
        }
        File file = new File(ossPath + File.separator + filePath);
        try (FileInputStream excelFileInputStream = new FileInputStream(file)) {
            String extension = filePath.split("\\.")[1];
            Workbook book = null;
            if ("xls".equals(extension)) {
                book = new HSSFWorkbook(excelFileInputStream);
            } else {
                book = new XSSFWorkbook(excelFileInputStream);
            }
            if (!ObjectUtils.isEmpty(book)) {
                Sheet sheet = book.getSheetAt(0);
                CellStyle cellStyle = sheet.getRow(1).getCell(0).getCellStyle();
                statusAndMessageMap.forEach((k, v) -> {
                    this.setupCellValue(sheet, k, cellStyle).setCellValue(v);
                });
                try (FileOutputStream excelFileOutPutStream = new FileOutputStream(file)) {
                    book.write(excelFileOutPutStream);
                    excelFileOutPutStream.flush();
                } catch (IOException e) {
                    log.info("update file error msg: " + e.getLocalizedMessage());
                }
            }
        } catch (EncryptedDocumentException | IOException e) {
            log.error(e.getMessage(), e);
        }

    }

    public Cell setupCellValue(Sheet sheet, String addressKey, CellStyle cellStyle) {
        CellAddress address = new CellAddress(addressKey);
        Row row = sheet.getRow(address.getRow());
        Cell cell = row.getCell(address.getColumn());
        if (cell == null) {
            cell = row.createCell(address.getColumn());
            cell.setCellStyle(cellStyle);
        }
        return cell;
    }

    @Override
    public Set<String> getUserIdsByTenantId(String tenantId, Set<String> userIds) {
        if (CollectionUtils.isEmpty(userIds)) {
            return new HashSet<>();
        }
        return userService.getUserIdsByTenantId(tenantId, userIds);
    }

    @Override
    public Map<String, String> getUserNameMap(List<String> idList) {
        return userService.getUserNameMap(idList);
    }

    @Override
    public void updateMyselfPassword(UserSelfPasswordUpdateDTO userPasswordDTO) {
        try {
            userService.changePassword(SessionUtil.getUserId(), userPasswordDTO.getNewPassword(), userPasswordDTO.getOldPassword(), null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        // force logout
        kpUserOnlineService.forceLogout(SessionUtil.getUserId());
    }

    @Override
    public void notification(String id, Integer status) {
        userService.update(Wrappers.<User>lambdaUpdate()
                .eq(User::getId,id)
                .set(User :: getNotification,status)
        );
    }

    @Override
    public List<MenuDTO> menus() {
        List<Permission> permissions = permissionService.getPermissions(SessionUtil.getUserId(), SessionUtil.getUserLevel());
        if(CollectionUtils.isNotEmpty(permissions)){
            List<Permission> list = permissions.stream().filter(permission -> permission.getType() == null || permission.getType() != 4).collect(Collectors.toList());
            return KpUserServiceImpl.tree(list,-1);
        }
        return Collections.emptyList();
    }

    public static List<MenuDTO> tree(List<Permission> permissions,Integer parentId){
        return permissions.stream()
                .filter(parent -> Objects.equals(parent.getParentId(), parentId))
                .map(child -> {
                    MenuDTO menuDTO = new MenuDTO();
                    menuDTO.setUri(child.getUri());
                    menuDTO.setAuthorityKey(child.getAuthorityKey());
                    menuDTO.setSubs(tree(permissions,child.getId()));
                    return menuDTO;
                }).collect(Collectors.toList());
    }

    @Override
    public boolean checkUserIsNeed2fa(String userId) {
        List<Control2FA> all2faControlByUserId = control2FAService.getControl2fas(userId,StatusEnum.ACTIVE.getStatus());
        //RSA verification is not performed by default
        boolean need2faCheck = false;
        if(!ObjectUtils.isEmpty(all2faControlByUserId)){
            for (Control2FA control2FA : all2faControlByUserId) {
                if(control2FA.getStatus() != 1){
                    need2faCheck = false;
                    break;
                }
                need2faCheck = true;
            }
        }
        return need2faCheck;
    }
}
