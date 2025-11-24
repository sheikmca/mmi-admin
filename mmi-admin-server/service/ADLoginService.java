package sg.ncs.kp.admin.service;

import com.alibaba.fastjson.JSONObject;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import sg.ncs.kp.admin.dto.CheckADUserResponseDTO;
import sg.ncs.kp.common.core.response.Result;
import sg.ncs.kp.uaa.server.po.User;

import java.util.List;

/**
 * @className ADLoginService
 * @author chen xi
 * @version 1.0.0
 * @date 2023-07-10
 */
public interface ADLoginService {
    CheckADUserResponseDTO checkADUser(String param, String password, String adName);

    Result<OAuth2AccessToken> getADLoginResult(User user);

    List<String> getAllADNames();
}
