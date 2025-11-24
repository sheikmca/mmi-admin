package sg.ncs.kp.admin.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import sg.ncs.kp.admin.dto.*;
import sg.ncs.kp.admin.enums.AdminMsgEnum;
import sg.ncs.kp.admin.pojo.AdminConstants;
import sg.ncs.kp.admin.pojo.WSMsgTypEnum;
import sg.ncs.kp.admin.service.*;
import sg.ncs.kp.common.core.response.Result;
import sg.ncs.kp.common.exception.pojo.ClientServiceException;
import sg.ncs.kp.common.i18n.pojo.CodeEnum;
import sg.ncs.kp.common.i18n.pojo.MessageEnum;
import sg.ncs.kp.common.i18n.util.MessageUtils;
import sg.ncs.kp.uaa.client.session.UserSession;
import sg.ncs.kp.uaa.client.util.SessionUtil;
import sg.ncs.kp.uaa.common.dto.RefreshTokenDTO;
import sg.ncs.kp.uaa.server.po.*;
import sg.ncs.kp.uaa.server.service.LoginService;
import sg.ncs.kp.uaa.server.service.UserGroupService;
import sg.ncs.kp.uaa.server.service.UserService;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * description: TODO
 *
 * @author Wang Shujin
 * @date 2022/8/22 11:06
 */
@RestController
@RequestMapping("/token")
@Slf4j
public class TokenController {

    @Autowired
    private LoginService loginService;

    @Autowired
    private MessageUtils messageUtils;
    
    @Autowired
    KpUserOnlineService kpUserOnlineService;
    
    @Autowired
    private UserService userService;

    @Autowired
    private KpUserService kpUserService;

    @Autowired
    private ADLoginService adLoginService;

    @Autowired
    private Control2FAService control2FAService;

    @Autowired
    KpUserGroupService kpUserGroupService;

    @Autowired
    private RedisTemplate<String,String> redisTemplate;

    @Value("${admin.check2FAEnabled}")
    private boolean loginNeed2FACheck;

    private static final String KEY_TELEPHONE_NUMBER = "telephoneNumber";
    private static final String KEY_MAIL = "mail";
    public static final String LOGIN_2FA_CHECK="check2FA";

    @PostMapping("/login")
    public Result<OAuth2AccessToken> login(@RequestBody Login2FADTO dto) {
        //check is need RSA check;
        User user = userService.getUser(dto.getUserName());
        if(ObjectUtil.isNotEmpty(user)) {
            String checkUserId = user.getId();
            if(ObjectUtil.isNotEmpty(dto.getDirection())){
                kpUserGroupService.judgeDirection(dto.getDirection(),checkUserId);
            }
            boolean currentUserRoleNeedRsaCheck = kpUserService.checkUserIsNeed2fa(checkUserId);
            check2FA(checkUserId, currentUserRoleNeedRsaCheck, dto);
        }
        Result<OAuth2AccessToken> result = loginService.login(dto);
        OAuth2AccessToken oAuth2AccessToken = result.getData();
        if(Objects.nonNull(oAuth2AccessToken)) {
            //Add user to online User Map
            kpUserOnlineService.userOnline(user.getId(), oAuth2AccessToken.getValue());                
        }
        return result;
    }

    @GetMapping("/get-all-ad-name")
    public Result<List<String>>  getAllAdNames() throws Exception {
        return new Result(adLoginService.getAllADNames(), true, MessageEnum.SUCCESS.code(), this.messageUtils.getMessage(MessageEnum.SUCCESS.val()), HttpStatus.OK.value());
    }

    @PostMapping("/ad-login")
    public Result<OAuth2AccessToken>  adLogin(@RequestBody ADLogin2FADTO params, HttpServletRequest request) throws Exception {
        log.info("param----->{}",params);
        String userName = params.getParam();
        String password = params.getPassword();
        String adName = params.getAdName();
        if(ObjectUtil.isNotEmpty(params.getDirection())){
            kpUserGroupService.judgeDirection(params.getDirection(),null);
        }
        User user = userService.getUser(userName);
        if(ObjectUtil.isNotEmpty(user)) {
            String checkUserId = user.getId();
            if(ObjectUtil.isNotEmpty(params.getDirection())){
                kpUserGroupService.judgeDirection(params.getDirection(),checkUserId);
            }
            boolean currentUserRoleNeedRsaCheck = kpUserService.checkUserIsNeed2fa(checkUserId);
            Login2FADTO dto = BeanUtil.copyProperties(params, Login2FADTO.class);
            dto.setUserName(userName);
            check2FA(checkUserId, currentUserRoleNeedRsaCheck, dto);
        }
        CheckADUserResponseDTO returnData = adLoginService.checkADUser(userName, password, adName);
        Integer num = returnData.getNum();

        // LDAP process failed
        if (num != 200) {
            return new Result(null, true, MessageEnum.SUCCESS.code(), this.messageUtils.getMessage(AdminMsgEnum.USER_INCORRECT_CREDENTIALS.val()), HttpStatus.BAD_REQUEST.value());
        } else {
            // New Implement for the AD User login
            return implAdLogin(returnData, adName,params.getDirection());
        }
    }

    /**
     * Implement of the AD User Login, there are two status of the user 1. New AD
     * User for UMMI 2. Existing AD User for UMMI
     *
     * @param
     *
     */
    private Result<OAuth2AccessToken>  implAdLogin(CheckADUserResponseDTO returnData,String adName, String direction) throws Exception {

        JSONObject jsonObject = new JSONObject();

        // Get all return AD User list
        List<ADUserDTO> userList = returnData.getUserList();
        User user = null;
        for (ADUserDTO adUser: userList) {

            // Individual AD User info
            log.info("Find out the AD user: {}", adUser.toString());

            String data = adUser.getUserName();
            String userName;
            if (data.contains(Constants.AT)) {
                // If the AD User id with "@" as same as email address
                userName = data.substring(0, data.indexOf(Constants.AT));
            } else {
                // Pure AD User
                userName = data;
            }
            log.info("The LogUtilsin AD user id is:{}",userName);

            // Add for MMI-505 by Zou Ke on 24 May 2019 - Start
            user = userService.getUser(userName);
            if (ObjectUtil.isEmpty(user)) {
                user = createAdUser(adUser,adName,direction);
            } else {
                // This is a existing AD User for UMMI
                user = updateAdUser(adUser, adName);
            }

        }
       return adLoginService.getADLoginResult(user);

    }
    /**
     * Create the new AD User in UMMI
     *
     * @param
     *
     */
    private User createAdUser(ADUserDTO obj,String adName, String direction) throws Exception {

        // This is a new AD User
        log.info("It's a new AD user account");
        // Prepare the user's full name and UMMI User Id
        String name = obj.getUserName();
        String data = obj.getUserName();

        JSONArray baseInfo = obj.getUserInfo();

        // Prepare the Mobile & Email info for the user
        String mobile = "";
        String email = "";
        if (baseInfo != null) {
            for (int j = 0; j < baseInfo.size(); j++) {
                JSONObject tmpObj = baseInfo.getJSONObject(j);
                if (ObjectUtil.isNotEmpty(tmpObj.getString(KEY_TELEPHONE_NUMBER))) {
                    mobile = tmpObj.getString(KEY_TELEPHONE_NUMBER);
                }
                if (ObjectUtil.isNotEmpty(tmpObj.getString(KEY_MAIL))) {
                    email = tmpObj.getString(KEY_MAIL);
                }
            }
        }

        String userName = "";

        // Base on the AD return user account to prepare the UMMI userId
        if (data.contains(Constants.AT)) {
            userName = data.substring(0, data.indexOf(Constants.AT));
            kpUserService.addLDAPUser(name, email, userName, adName, mobile,direction);
        } else {
            userName = data;
            kpUserService.addLDAPUser(name, email, userName,adName, mobile,direction);
        }
        return userService.getUser(userName);
    }

    /**
     * Update the existing AD User in UMMI
     */
    private User updateAdUser(ADUserDTO obj,String adName) throws Exception {

        // This is a existing AD User already in UMMI
        log.info("Existing AD user in our system");

        String data = obj.getUserName();

        JSONArray baseInfo = obj.getUserInfo();

        // Prepare the Mobile & Email info for the user
        String mobile = "";
        String email = "";

        if (baseInfo != null) {
            for (int j = 0; j < baseInfo.size(); j++) {
                JSONObject tmpObj = baseInfo.getJSONObject(j);
                if (ObjectUtil.isNotEmpty(tmpObj.getString(KEY_TELEPHONE_NUMBER))) {
                    mobile = tmpObj.getString(KEY_TELEPHONE_NUMBER);
                }
                if (ObjectUtil.isNotEmpty(tmpObj.getString(KEY_MAIL))) {
                    email = tmpObj.getString(KEY_MAIL);
                }
            }
        }

        String userName = "";

        if (data.contains(Constants.AT)) {
            userName = data.substring(0, data.indexOf(Constants.AT));
        } else {
            userName = data;
        }
        kpUserService.updateLDAPUser(data,email,userName,adName,mobile);
        return userService.getUser(userName);

    }



    @GetMapping("/logout")
    public Result<Void> logout(@RequestParam(value = "userId", required = false) String userId) {
        if(ObjectUtil.isEmpty(userId)) {
            loginService.logout();
            //Remove user from online User Map
            kpUserOnlineService.userOffline(SessionUtil.getUserId(), WSMsgTypEnum.FORCED_OFFLINE);
        }else{
            kpUserOnlineService.forceLogout(userId);
            return messageUtils.succeed(null,messageUtils.getMessage(AdminMsgEnum.DISCONNECT_SUCCESS.val()));
        }
        return messageUtils.succeed(null);
    }

    @PostMapping("/refresh")
    public Result<OAuth2AccessToken> refresh(@RequestBody RefreshTokenDTO dto) {
        Result<OAuth2AccessToken> result = messageUtils.succeed(loginService.refresh(dto));
        //Add user to online User Map
        OAuth2AccessToken oAuth2AccessToken = result.getData();
        if(Objects.nonNull(oAuth2AccessToken)) {
            kpUserOnlineService.userOnline(SessionUtil.getUserId(), oAuth2AccessToken.getValue());
        }
        return result;
    }


    @GetMapping("/get-myself")
    public Result<UserSession> getMyself() {
        return messageUtils.succeed(SessionUtil.getUserSession());
    }

    private void check2FA(String userId, Boolean currentUserRoleNeedRsaCheck, Login2FADTO dto) {
        if(loginNeed2FACheck){
            log.info(">>>>> Start Login check 2FA Code : {}",System.currentTimeMillis());
            // check now could login
            check2FANowLogin(userId, currentUserRoleNeedRsaCheck);

            String acitonStr = dto.getLoginAction();
            if(currentUserRoleNeedRsaCheck && ObjectUtil.isEmpty(acitonStr)){
                control2FAService.initFailedNum(userId);
                throw  new ClientServiceException(AdminMsgEnum.NEED_2FA_CHECK, 402);
            }else if(currentUserRoleNeedRsaCheck && !StringUtils.isEmpty(acitonStr)) {
                String code = dto.getCode2FA();
                if(acitonStr.equals(LOGIN_2FA_CHECK)){
                    control2FAService.check2FA(userId, code);
                }
            }
            log.info(">>>>> End Login check 2FA Code :{}",System.currentTimeMillis());
        }
    }

    private void check2FANowLogin(String userId, Boolean currentUserRoleNeedRsaCheck) {
        // check now could login
        String disabledLoginKey = AdminConstants.CHECK_2FA_DISABLED_LOGIN+userId;
        if(redisTemplate.hasKey(disabledLoginKey) && currentUserRoleNeedRsaCheck){
            Long waitMin = redisTemplate.getExpire(disabledLoginKey, TimeUnit.MINUTES);
            if(waitMin >= 0) {
                if(waitMin == 0){
                    waitMin += 1;
                }
                throw new ClientServiceException(AdminMsgEnum.VERITY_2FA_EXCEEDS_MAXNUM,waitMin+"");
            }else{
                redisTemplate.delete(disabledLoginKey);
            }
        }
    }

}
