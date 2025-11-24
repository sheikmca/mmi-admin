package sg.ncs.kp.admin.pojo;

public class AdminConstants {
    private static final String SIERRA_PREFIX ="sierra:";
    public static final String USER_ONLINE_REDIS_KEY="user::online";
    public static final String OLD_AUTH_TOKEN = "old::auth::token::";

    public static final String RECIPIENT_REDIS_CACHE_KEY = "kp_recipient_cache";
    public static final String CHECK_2FA_DISABLED_LOGIN=SIERRA_PREFIX+"check2FADisabledLogin:";
    public static final String CHECK_2FA_FAILED=SIERRA_PREFIX+"check2FAFailed:";
    public static final String AD_GROUP_CN="CN=";
    public static final String USER_HEARTBEAT_KEY = SIERRA_PREFIX + "user:heartbeat:";
    private AdminConstants() {}
    
}
