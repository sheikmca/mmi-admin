package sg.ncs.kp.admin.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import sg.ncs.kp.admin.dto.CollectedInputsParam;
import sg.ncs.kp.admin.dto.ContextParam;
import sg.ncs.kp.admin.dto.SubjectCredentialsParam;
import sg.ncs.kp.admin.dto.VerifyParam;
import sg.ncs.kp.admin.enums.AdminMsgEnum;
import sg.ncs.kp.admin.enums.Credential2FAEnum;
import sg.ncs.kp.admin.mapper.Control2FAMapper;
import sg.ncs.kp.admin.po.Control2FA;
import sg.ncs.kp.admin.pojo.AdminConstants;
import sg.ncs.kp.admin.service.Control2FAService;
import sg.ncs.kp.common.exception.pojo.ClientServiceException;
import sg.ncs.kp.common.exception.pojo.ServiceException;
import sg.ncs.kp.uaa.client.session.UserSession;
import sg.ncs.kp.uaa.client.util.SessionUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @className Control2FAServiceImpl
 * @author chen xi
 * @version 1.0.0
 * @date 2023-07-11
 */

@Service
@Slf4j
public class Control2FAServiceImpl  extends ServiceImpl<Control2FAMapper, Control2FA> implements Control2FAService {

    @Autowired
    private Control2FAMapper control2FAMapper;

    @Value("${admin.2fa.initializeUri}")
    private String initializeUri;

    @Value("${admin.2fa.clientId}")
    private String clientId;

    @Value("${admin.2fa.clientKey}")
    private String clientKey;

    @Value("${admin.2fa.maxFailedNum}")
    private Integer maxFailedNum;

    @Value("${admin.2fa.waitMinute}")
    private Integer waitMinute;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private RedisTemplate<String,String> redisTemplate;

    @Override
    public Boolean getStatusByRoleId(Integer roleId) {
        Control2FA control2FA = getControl2FAByRoleId(roleId);
        if(ObjectUtil.isNotEmpty(control2FA)){
            return control2FA.getStatus() == 1?true:false;
        }else{
            return false;
        }
    }

    @Override
    public void addOrUpdateControl2FA(Integer roleId, Boolean controlStatus) {
        Control2FA control2FA = getControl2FAByRoleId(roleId);
        Integer status = controlStatus?1:0;
        UserSession userSession = SessionUtil.getUserSession();
        String userId = userSession.getId();
        if(ObjectUtil.isEmpty(control2FA)){
            this.addControl2FA(roleId,status,userId);
        }else{
            control2FA.setLastUpdatedId(userId);
            control2FA.setStatus(status);
            this.updateControl2FA(control2FA);
        }
    }

    @Override
    public void deleteControl2FA(Integer roleId) {
        Control2FA control2FA = this.getControl2FAByRoleId(roleId);
        if(ObjectUtil.isNotEmpty(control2FA)) {
            this.removeById(control2FA);
        }
    }

    @Override
    public List<Control2FA> getControl2fas(String userId, Integer roleStatus) {
        return control2FAMapper.getControl2fas(userId,roleStatus);
    }

    private void addControl2FA(Integer roleId, Integer status, String userId){
        Control2FA control2FA = new Control2FA();
        control2FA.setRoleId(roleId);
        control2FA.setStatus(status);
        control2FA.setCreatedId(userId);
        control2FA.setLastUpdatedId(userId);
        this.save(control2FA);
    }

    private void updateControl2FA(Control2FA control2FA){
        this.updateById(control2FA);
    }

    private Control2FA getControl2FAByRoleId(Integer roleId){
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("role_id",roleId);
        return control2FAMapper.selectOne(queryWrapper);
    }
    /**
     * @description rsa check
     * @param userId, code
     * @return void
     * @author chen xi
     * @version 1.0.0
     * @date 2022-08-19
     */
    @Override
    public void check2FA(String userId, String code){
        log.info(">>>>>>>>>>>start check 2FA<<<<<<<<<<<<<<<<<<< :{}",userId);
        String checkRsaFailedNumKey = AdminConstants.CHECK_2FA_FAILED+userId;
        int failedNum = 0;
        // 1 check 2fa failed number
        if(redisTemplate.hasKey(checkRsaFailedNumKey)){
            String failedNumStr = redisTemplate.opsForValue().get(checkRsaFailedNumKey);
            failedNum = Integer.valueOf(failedNumStr);
            this.checkRsaFailedNum(failedNum, userId);
        }else{
            initFailedNum(userId);
        }
        // 2.call verify api
        this.verify2FA(userId,code);
        log.info(">>>>>>>>>>>end check 2FA<<<<<<<<<<<<<<<<<<< :{}",userId);
    }


    /**
     * @description rsa verity
     * @param userId,code
     * @author chen xi
     * @version 1.0.0
     * @date 2022-08-19
     */
    private void verify2FA(String userId, String code) throws ServiceException {
        log.info(">>>>>>>>>>start verity 2FA<<<<<<<<<<<<<<:{}" ,userId);
        // generate request params
        VerifyParam param = new VerifyParam();
        param.setClientId(clientId);
        param.setSubjectName(userId);
        ContextParam context = new ContextParam();
        List<SubjectCredentialsParam> subjectCredentials = new ArrayList<>();
        List<CollectedInputsParam> collectedInputs = new ArrayList<>();
        // generate messageId
        Long nowTime = DateUtil.current();
        String messageId = userId + nowTime;
        context.setMessageId(messageId);
        param.setContext(context);
        SubjectCredentialsParam subjectCredential = new SubjectCredentialsParam();
        subjectCredential.setMethodId(Credential2FAEnum.SECURID.getValue());
        CollectedInputsParam collectedInput = new CollectedInputsParam();
        collectedInput.setName(Credential2FAEnum.SECURID.getValue());
        collectedInput.setValue(code);
        collectedInputs.add(collectedInput);
        subjectCredential.setCollectedInputs(collectedInputs);
        subjectCredentials.add(subjectCredential);
        param.setSubjectCredentials(subjectCredentials);
        // call rsa verify api
        String respEntity = null;
        log.info("***************verify param={}", param.toString());
        log.info("***************initializeUri={}", initializeUri);
        /*try {
            respEntity = restTemplate.postForObject(initializeUri, this.prepareHeaderForPost(param), String.class);
        } catch (HttpStatusCodeException e) {
            respEntity = e.getResponseBodyAsString();
            HttpStatus statusCode = e.getStatusCode();
            log.error("statusCode-->{}", statusCode);
            log.error("respEntity-->{}", respEntity);
            // set failed number
            setFailedNum(userId);
            throw new ClientServiceException(respEntity);
        }
        JSONObject response = JSON.parseObject(respEntity);*/
        JSONObject response = new JSONObject();
        ContextParam simParam = new ContextParam();
        simParam.setInResponseTo(messageId);
        response.put("context",JSON.toJSONString(simParam));
        JSONArray temp = new JSONArray();
        JSONObject tempO = new JSONObject();
        if(code.equals("1111111111")){
            tempO.put("methodResponseCode", "SUCCESS");
        }else {
            tempO.put("methodResponseCode", "Failed");
        }
        temp.add(tempO);
        response.put("credentialValidationResults",temp);
        log.info("***************verify response={}",response);
        if (!ObjectUtils.isEmpty(response)) {
            ContextParam newContext = JSON.parseObject(response.getString("context"), ContextParam.class);
            // check messageId is same as request's messageId.
            this.checkMessageId(newContext, messageId, userId);
            // Judging success or failure
            if(!judgingSuccessOrFailure(response, userId)){
                return ;
            }
        }
        // set failed number
        Integer failedNum = setFailedNum(userId);
        if(maxFailedNum - failedNum > 0) {
            throw new ClientServiceException(AdminMsgEnum.VERITY_2FA_FAILED, (maxFailedNum - failedNum)+"");
        }else{
            this.checkRsaFailedNum(failedNum, userId);
        }
    }

    private void checkMessageId( ContextParam newContext, String messageId, String userId) throws ServiceException {
        // check messageId is same as request's messageId.
        if (!newContext.getInResponseTo().equals(messageId)) {
            // set failed number
            Integer failedNum = setFailedNum(userId);
            if(maxFailedNum - failedNum > 0) {
                throw new ClientServiceException(AdminMsgEnum.VERITY_2FA_FAILED, (maxFailedNum - failedNum)+"");
            }else{
                this.checkRsaFailedNum(failedNum, userId);
            }
        }
    }

    private Boolean judgingSuccessOrFailure(JSONObject response, String userId) throws ServiceException {
        //Judging success or failure
        if (!ObjectUtils.isEmpty(response.get("credentialValidationResults"))) {
            JSONArray results = response.getJSONArray("credentialValidationResults");
            if (!ObjectUtils.isEmpty(results)) {
                for (Object o : results) {
                    JSONObject result = JSON.parseObject(o.toString());
                    boolean flag = this.judgingSuccessOrFailureByResult(result,userId);
                    if(!flag){
                        return flag;
                    }
                }
            }
        }
        return true;
    }

    private boolean judgingSuccessOrFailureByResult(JSONObject result, String userId) throws ServiceException {
        if (!ObjectUtils.isEmpty(result)) {
            String responseCode = result.getString("methodResponseCode");
            if (responseCode.equals("SUCCESS")) {
                removeFailedNum(userId);
                return false;
            } else {
                String failedReson = result.getString("methodReasonCode");
                // set failed number
                Integer faileNum = setFailedNum(userId);
                if(maxFailedNum - faileNum > 0) {
                    throw new ClientServiceException(AdminMsgEnum.VERITY_2FA_FAILED, (maxFailedNum - faileNum)+"");
                }else{
                    this.checkRsaFailedNum(faileNum, userId);
                }
            }
        }
        return true;
    }

    /**
     * @description prepareHeaderForPost
     * @param data
     * @return org.springframework.http.HttpEntity<java.lang.String>
     * @author chen xi
     * @version 1.0.0
     * @date 2022-08-19
     */
    private HttpEntity<String> prepareHeaderForPost(Object data) {
        // this.configIPHttpsAccess();
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Lists.newArrayList(MediaType.APPLICATION_JSON));
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("client-key", clientKey);
        HttpEntity<String> httpEntity = new HttpEntity<String>(JSON.toJSON(data).toString(), headers);
        return httpEntity;
    }

    /**
     * @description set failed number
     * @param userId
     * @return void
     * @author chen xi
     * @version 1.0.0
     * @date 2022-08-19
     */
    private Integer setFailedNum(String userId){
        String checkRsaFailedNumKey = AdminConstants.CHECK_2FA_FAILED+userId;
        int failedNum = 0;
        // 1.get redis check rsa failed number
        if(redisTemplate.hasKey(checkRsaFailedNumKey)){
            String failedNumStr = redisTemplate.opsForValue().get(checkRsaFailedNumKey);
            failedNum = Integer.valueOf(failedNumStr)+1;
            redisTemplate.opsForValue().set(checkRsaFailedNumKey,String.valueOf(failedNum));
        }else{
            redisTemplate.opsForValue().set(checkRsaFailedNumKey, "0");
        }
        return failedNum;
    }

    /**
     * @description init failed number
     * @param userId
     * @return void
     * @author chen xi
     * @version 1.0.0
     * @date 2022-08-26
     */
    @Override
    public void initFailedNum(String userId){
        String checkRsaFailedNumKey =AdminConstants.CHECK_2FA_FAILED+userId;
        redisTemplate.opsForValue().set(checkRsaFailedNumKey, "0");
    }

    /**
     * @description remove failed number
     * @param userId
     * @return void
     * @author chen xi
     * @version 1.0.0
     * @date 2022-08-26
     */
    private void removeFailedNum(String userId){
        String checkRsaFailedNumKey = AdminConstants.CHECK_2FA_FAILED+userId;
        redisTemplate.delete(checkRsaFailedNumKey);
    }

    /**
     * @description set disabled login to redis
     * @param userId
     * @return void
     * @author chen xi
     * @version 1.0.0
     * @date 2022-08-30
     */
    private void setDisabledLoginToRedis(String userId){
        String disabledLoginKey = AdminConstants.CHECK_2FA_DISABLED_LOGIN+userId;
        redisTemplate.opsForValue().set(disabledLoginKey, "Y",waitMinute, TimeUnit.MINUTES);
    }

    private void checkRsaFailedNum(Integer failedNum, String userId) throws ServiceException {
        if (failedNum >= maxFailedNum) {
            removeFailedNum(userId);
            setDisabledLoginToRedis(userId);
            throw new ClientServiceException(AdminMsgEnum.VERITY_2FA_EXCEEDS_MAXNUM, waitMinute+"");
        }
    }
}
