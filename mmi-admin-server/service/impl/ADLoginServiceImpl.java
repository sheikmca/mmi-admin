package sg.ncs.kp.admin.service.impl;
/**
 * @className ADLoginServiceImpl
 * @author chen xi
 * @version 1.0.0
 * @date 2023-07-10
 */

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.mysql.cj.util.LogUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.*;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.stereotype.Service;
import sg.ncs.kp.admin.config.LdapSocketFactory;
import sg.ncs.kp.admin.dto.ADUserDTO;
import sg.ncs.kp.admin.dto.CheckADUserResponseDTO;
import sg.ncs.kp.admin.mapper.AdServerMapper;
import sg.ncs.kp.admin.po.AdServer;
import sg.ncs.kp.admin.pojo.AdminConstants;
import sg.ncs.kp.admin.service.ADLoginService;
import sg.ncs.kp.common.core.response.Result;
import sg.ncs.kp.common.i18n.pojo.MessageEnum;
import sg.ncs.kp.common.i18n.util.MessageUtils;
import sg.ncs.kp.common.mybaits.base.po.AutoIncrementPo;
import sg.ncs.kp.uaa.client.session.UserSession;
import sg.ncs.kp.uaa.common.enums.PasswordPolicyEnum;
import sg.ncs.kp.uaa.common.enums.StatusEnum;
import sg.ncs.kp.uaa.common.enums.UserLevelEnum;
import sg.ncs.kp.uaa.server.mapper.LoginLogMapper;
import sg.ncs.kp.uaa.server.mapper.RoleMapper;
import sg.ncs.kp.uaa.server.po.*;
import sg.ncs.kp.uaa.server.service.*;

import javax.annotation.Resource;
import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.*;
import javax.naming.ldap.InitialLdapContext;
import java.util.*;
import java.util.stream.Collectors;
@Service
@Slf4j
public class ADLoginServiceImpl implements ADLoginService {
    @Autowired
    private AdServerMapper adServerMapper;
    @Autowired
    private UserGroupService userGroupService;
    @Autowired
    private UserService userService;
    @Autowired
    private PermissionService permissionService;
    @Autowired
    private RoleMapper roleMapper;
    @Autowired
    private ClientDetailsService clientDetailsService;

    @Autowired
    private PasswordPolicyService passwordPolicyService;
    @Autowired
    private LoginLogMapper loginLogMapper;

    @Autowired
    private DefaultTokenServices defaultTokenServices;
    @Autowired
    private MessageUtils messageUtils;

    private static final String KEY_DESCRIPTION = "description";
    private static final String KEY_WORK = "work";
    private static final String KEY_DEPARTMENT = "department";
    private static final String KEY_MOBILE = "mobile";
    private static final String KEY_DISTINGUISHED_NAME = "distinguishedName";
    private static final String KEY_CN = "cn";
    private static final String KEY_SAMACCOUNT_NAME = "samaccountName";
    private static final String KEY_UID = "uid";
    private static final String KEY_UPDATED = "updated";
    private static final String KEY_MAIL = "mail";
    private static final String KEY_TELEPHONE_NUMBER = "telephoneNumber";
    private static final String KEY_USER_INFO = "userInfo";
    private static final String KEY_USER_ID = "userId";
    private static final String KEY_NAME = "name";


    @Override
    public CheckADUserResponseDTO checkADUser(String userName, String password, String adName) {
        CheckADUserResponseDTO msg = new CheckADUserResponseDTO();

        try {
            log.info("LDAP LogUtilsin start >>>>>>>>>>>>>>");
            AdServer adSvc = getAdServerInfo(adName);
            String hostSg = adSvc.getAdAddress();
            String port = adSvc.getAdPort();
            String domain = adSvc.getAdDomain();
            String url = new String("ldaps://" + hostSg + ":" + port);
            String principal = userName+Constants.AT+domain;
            log.info("LDAP PRINCIPAL: {0} with CREDENTIALS: {1}", principal,password);
            Hashtable<String, String> env = new Hashtable<String, String>();

            env.put(Context.SECURITY_AUTHENTICATION, "simple");
            env.put(Context.SECURITY_PRINCIPAL, principal);
            env.put(Context.SECURITY_CREDENTIALS, password);
            env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
            env.put(Context.PROVIDER_URL, url);
            env.put(Context.SECURITY_PROTOCOL, "ssl");
            env.put("java.naming.ldap.factory.socket", LdapSocketFactory.class.getName());
            //System.setProperty("javax.net.ssl.trustStore", jks);
            //System.setProperty("javax.net.ssl.trustStorePassword", jksPassword);
            DirContext ctx = new InitialDirContext(env);
            String searchBase = AdminConstants.AD_GROUP_CN + userName + Constants.COMMA + adSvc.getAdBase();
            String searchFilter = "(objectClass=" + "organizationalPerson" + ")";
            List<ADUserDTO> userList = new ArrayList<>();
            if (ctx != null) {
                log.info("LDAP LogUtilsin successful <<<<<<<<<<<<");
                SearchControls searchCtls = new SearchControls();
                searchCtls.setSearchScope(SearchControls.SUBTREE_SCOPE);
                String[] returnedAtts = { KEY_UID, KEY_SAMACCOUNT_NAME, KEY_CN, KEY_DISTINGUISHED_NAME, KEY_MOBILE, KEY_MAIL, KEY_TELEPHONE_NUMBER,
                        KEY_DEPARTMENT, KEY_WORK, KEY_DESCRIPTION, KEY_NAME};
                searchCtls.setReturningAttributes(returnedAtts);
                NamingEnumeration entries = ctx.search(searchBase, searchFilter, searchCtls);
                while (entries.hasMoreElements()) {
                    ADUserDTO user = new ADUserDTO();
                    SearchResult entry = (SearchResult) entries.next();
                    user.setUserName(userName);

                    Attributes at = entry.getAttributes();
                    if (at != null) {
                        JSONArray tmpAttr = new JSONArray();
                        for (NamingEnumeration ne = at.getAll(); ne.hasMore(); ) {
                            Attribute attr = (Attribute) ne.next();
                            String attrid = attr.getID().toString();
                            log.info("----->>>>{}\t", attrid);
                            String attrvalue = attr.get().toString();
                            if (KEY_TELEPHONE_NUMBER.equals(attrid) || KEY_MAIL.equals(attrid)) {
                                JSONObject tmpJson = new JSONObject();
                                tmpJson.put(attrid, attrvalue);
                                tmpAttr.add(tmpJson);
                            }
                            user.setUserInfo(tmpAttr);
                        }
                        userList.add(user);
                    }
                    log.info("LDAP LogUtilsin successful with user list: {}",userList);
                }
            }
            msg.setUserList(userList);
            // Add for IVHFATOSAT-282 by Zou Ke - End
            ctx.close();
            log.info("The verification successful...");
           msg.setNum(200);

        } catch (NamingException e) {
            log.error("The verification failed...");
            msg.setNum(400);
        } catch (Exception e) {
            log.error("The verification failed...");
            msg.setNum(500);
        }
        return msg;
    }

    private AdServer getAdServerInfo(String adName) {
        AdServer adInfo = new AdServer();
        adInfo = adServerMapper.getByAdName(adName);
        return adInfo;
    }

    @Override
    public Result<OAuth2AccessToken> getADLoginResult(User user){
        UserSession userSession = userService.getUserSession(user);
        List<Role> roles = roleMapper.getRoles(user.getId(), StatusEnum.ACTIVE.getStatus());
        if (roles != null) {
            Set<Integer> roleIds = (Set)roles.stream().map(AutoIncrementPo::getId).collect(Collectors.toSet());
            userSession.setRoleId(roleIds);
        }

        UserGroup userGroup = userGroupService.getByUserId(user.getId());
        if (userGroup != null) {
            HashSet<Integer> set = new HashSet();
            set.add(userGroup.getId());
            userSession.setUserGroupId(set);
        }

        List<Permission> permissions = this.permissionService.getPermissions(user.getId(), UserLevelEnum.byValue(user.getLevel()));
        if (permissions != null) {
            userSession.setAuthorities((List)permissions.stream().map(Permission::getAuthorityKey).collect(Collectors.toList()));
        }

        ClientDetails clientDetails = this.clientDetailsService.loadClientByClientId("");
        TokenRequest tokenRequest = new TokenRequest(new HashMap(), clientDetails.getClientId(), clientDetails.getScope(), "password");
        OAuth2Request oAuth2Request = tokenRequest.createOAuth2Request(clientDetails);
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("tenant_id", user.getTenantId());
        queryWrapper.eq("policy_key",PasswordPolicyEnum.PASSWORD_DEFAULT.getKey());
        PasswordPolicy passwordPolicy = this.passwordPolicyService.getOne(queryWrapper);
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(userSession, passwordPolicy.getPolicyValue(), userSession.getAuthorities());
        OAuth2Authentication oauth2Authentication = new OAuth2Authentication(oAuth2Request, authenticationToken);
      //  this.loginLogMapper.insert(new LoginLog((Integer)null, user.getId(), user.getTenantId(), user.getUserName(), userSession.getCurrentIp(), new Date(),null, user.getPhone(), user.getEmail(), userGroup != null ? userGroup.getName() : null));
        long id =  1950083460652933121L;
               // Math.abs(UUID.randomUUID().getMostSignificantBits());
        LoginLog log = new LoginLog();
      //  log.setId(id);
        log.setUserId(user.getId());
        log.setUserIdNum(user.getUserId());
        log.setTenantId(user.getTenantId());
        log.setUserName(user.getUserName());
        log.setIp(userSession.getCurrentIp());
        log.setLoginTime(new Date());
        log.setUserGroupName(userGroup != null ? userGroup.getName() : null);
       // this.loginLogMapper.insert(new LoginLog());
        this.loginLogMapper.insert(log);
        OAuth2AccessToken accessToken = this.defaultTokenServices.createAccessToken(oauth2Authentication);
        return new Result(accessToken, true, MessageEnum.SUCCESS.code(), this.messageUtils.getMessage(MessageEnum.SUCCESS.val()), HttpStatus.OK.value());
    }

    @Override
    public List<String> getAllADNames() {
        return adServerMapper.getAllAdNames();
    }
}
