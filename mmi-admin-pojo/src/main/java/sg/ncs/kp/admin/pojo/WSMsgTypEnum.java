package sg.ncs.kp.admin.pojo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Objects;

/**
 * @auther 
 * @date 2022/4/15
 * @description
 */
public enum WSMsgTypEnum {

    
    FORCED_OFFLINE("forced.offline"),
    EXPIRED_OFFLINE("expired.offline"),
    PERMISSION_UPDATE("permission.update"),
    ACCOUNT_DISABLE("account.disable"),
    FORCED_LOGOUT("forced.logout"),
    CROSS_SITE_FORCED_LOGOUT("cross.site.forced.logout");

    private final String code;

    private WSMsgTypEnum(String code) {
        this.code = code;
    }

    @JsonValue
    public String getCode() {
        return this.code;
    }
    @JsonCreator
    public static WSMsgTypEnum getItem(String code){
        for(WSMsgTypEnum item : values()){
            if(Objects.equals(item.getCode(), code)){
                return item;
            }
        }
        return null;
    }
}
