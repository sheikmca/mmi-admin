package sg.ncs.kp.admin.enums;

import lombok.Getter;
import lombok.Setter;

/**
 * @author chen xi
 * @version 1.0.0
 * @className FrontSizeTypeEnum
 * @date 2023-07-25
 */
@Getter
public enum FrontSizeTypeEnum {
    SMALL(1,"Small"),
    DEFAULT(2,"Default"),
    MEDIUM(3,"Medium"),
    LARGE(4,"Large")    ;

    FrontSizeTypeEnum(Integer key, String value){
        this.key = key;
        this.value = value;
    }

    private Integer key;
    private String value;

    public static Integer getKeyByValue(String value){
        FrontSizeTypeEnum[] typeEnums = values();
        for (int i = 0; i < typeEnums.length; i++) {
            FrontSizeTypeEnum typeEnum = typeEnums[i];
            if (typeEnum.getValue().equals(value)){
                return typeEnum.getKey();
            }
        }
        return null;
    }

    public static String getValueByKey(Integer key){
        FrontSizeTypeEnum[] typeEnums = values();
        for (int i = 0; i < typeEnums.length; i++) {
            FrontSizeTypeEnum typeEnum = typeEnums[i];
            if (typeEnum.getKey().equals(key)){
                return typeEnum.getValue();
            }
        }
        return null;
    }
}
