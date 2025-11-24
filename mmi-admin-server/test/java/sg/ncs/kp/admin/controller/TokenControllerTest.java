package sg.ncs.kp.admin.controller;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;

import sg.ncs.kp.admin.BaseTest;
import sg.ncs.kp.admin.dto.Login2FADTO;
import sg.ncs.kp.admin.service.Control2FAService;
import sg.ncs.kp.admin.service.KpUserOnlineService;
import sg.ncs.kp.admin.service.KpUserService;
import sg.ncs.kp.common.core.response.Result;
import sg.ncs.kp.common.i18n.util.MessageUtils;
import sg.ncs.kp.uaa.client.util.SessionUtil;
import sg.ncs.kp.uaa.common.dto.LoginDTO;
import sg.ncs.kp.uaa.common.dto.RefreshTokenDTO;
import sg.ncs.kp.uaa.server.po.User;
import sg.ncs.kp.uaa.server.service.LoginService;
import sg.ncs.kp.uaa.server.service.UserService;

/**
 * description: TODO
 *
 * @author Wang Shujin
 * @date 2022/8/26 17:23
 */
public class TokenControllerTest extends BaseTest {

    @Mock
    private LoginService loginService;

    @Mock
    private MessageUtils messageUtils;

    @InjectMocks
    private TokenController tokenController;
    
    @Mock
    private UserService userService;
    
    @Mock 
    private KpUserOnlineService kpUserOnlineService;

    @Mock
    private KpUserService kpUserService;

    @Mock
    private Control2FAService control2FAService;


    @Test
    void loginTest() {
        try (MockedStatic<SessionUtil> mockedStatic = Mockito.mockStatic(SessionUtil.class)) {
            mockedStatic.when(SessionUtil::getUserId).thenReturn("id");
            Mockito.when(userService.getUser(Mockito.any())).thenReturn(new User());
            OAuth2AccessToken oAuth2AccessToken = new DefaultOAuth2AccessToken("test");
            Result<OAuth2AccessToken> result = new Result<>();
            result.setData(oAuth2AccessToken);
            Mockito.when(loginService.login(Mockito.any())).thenReturn(result);
            Mockito.when(kpUserService.checkUserIsNeed2fa(Mockito.any())).thenReturn(false);
            Mockito.when(messageUtils.succeed(Mockito.any())).thenReturn(new Result<>());
            Login2FADTO login2FADTO = new Login2FADTO();
            login2FADTO.setUserName("userName");
            Assertions.assertDoesNotThrow(() -> tokenController.login(login2FADTO));
        }
    }

    @Test
    void logoutTest() {
        try (MockedStatic<SessionUtil> mockedStatic = Mockito.mockStatic(SessionUtil.class)) {
            mockedStatic.when(SessionUtil::getUserId).thenReturn("id");
            Assertions.assertDoesNotThrow(() -> tokenController.logout(null));
        }
    }

    @Test
    void refreshTest() {
        try (MockedStatic<SessionUtil> mockedStatic = Mockito.mockStatic(SessionUtil.class)) {
            OAuth2AccessToken oAuth2AccessToken = new DefaultOAuth2AccessToken("test");
            mockedStatic.when(SessionUtil::getUserId).thenReturn("id");
            Mockito.when(loginService.refresh(Mockito.any())).thenReturn(oAuth2AccessToken);
            Result<OAuth2AccessToken> result = new Result<>();
            result.setData(oAuth2AccessToken);
            Mockito.when(messageUtils.succeed(Mockito.any(OAuth2AccessToken.class))).thenReturn(result);
            Assertions.assertDoesNotThrow(() -> tokenController.refresh(new RefreshTokenDTO()));
        }
    }

    @Test
    void getMyselfTest() {
        try (MockedStatic<SessionUtil> mockedStatic = Mockito.mockStatic(SessionUtil.class)) {
            mockedStatic.when(SessionUtil::getUserSession).thenReturn(null);
            Assertions.assertDoesNotThrow(() -> tokenController.getMyself());
        }
    }

}
