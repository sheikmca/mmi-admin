package sg.ncs.kp.admin.util;

import javax.servlet.http.HttpServletRequest;


/**
 * 
 * RemoteIpHelper
 * @author P1317470
 * @date May 7, 2019
 */
public class RemoteIpHelper {

    private static final String UNKNOWN = "unknown";

    /**
     * 
     * @param request
     * @return
     * @author  P1317470
     * @date May 7, 2019
     */
    public static String getRemoteIpFrom(HttpServletRequest request) {
        String ip = null;
        int tryCount = 0;

        while (!isIpFound(ip) && tryCount <= 6) {
            switch (tryCount) {
                case 0:
                	break;
                case 1:
                	ip = request.getHeader(HttpHeader.X_REAL_IP.key());
                	break;
                case 2:
                	ip = request.getHeader(HttpHeader.X_FORWARDED_FOR.key());
                	break;
                case 3:
                    ip = request.getHeader(HttpHeader.PROXY_CLIENT_IP.key());
                    break;
                case 4:
                    ip = request.getHeader(HttpHeader.WL_PROXY_CLIENT_IP.key());
                    break;
                case 5:
                    ip = request.getHeader(HttpHeader.HTTP_CLIENT_IP.key());
                    break;
                case 6:
                    ip = request.getHeader(HttpHeader.HTTP_X_FORWARDED_FOR.key());
                    break;
                default:
                    ip = request.getRemoteAddr();
            }
            tryCount++;
        }

        return ip;
    }

    /**
     * 
     * @param ip
     * @return
     * @author  P1317470
     * @date May 7, 2019
     */
    private static boolean isIpFound(String ip) {
        return ip != null && ip.length() > 0 && !UNKNOWN.equalsIgnoreCase(ip);
    }
}