package sg.ncs.kp.admin.util;

/**
 * 
 * HttpHeader
 * @author P1317470
 * @date May 7, 2019
 */
public enum HttpHeader {
    AUTHORIZATION("Authorization"),
    X_REAL_IP("X-Real-IP"),
    AUTHENTICATION_TYPE_BASIC("Basic"),
    X_AUTH_TOKEN("X-AUTH-TOKEN"),
    WWW_Authenticate("WWW-Authenticate"),
    X_FORWARDED_FOR("X-Forwarded-For"),
    PROXY_CLIENT_IP("Proxy-Client-IP"),
    WL_PROXY_CLIENT_IP("WL-Proxy-Client-IP"),
    HTTP_CLIENT_IP("HTTP_CLIENT_IP"),
    HTTP_X_FORWARDED_FOR("HTTP_X_FORWARDED_FOR");

    private String key;

    private HttpHeader(String key) {
        this.key = key;
    }

    public String key() {
        return this.key;
    }
}