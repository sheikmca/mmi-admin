package sg.ncs.kp.admin.enums;

import lombok.Getter;

/**
 * @author chen xi
 * @version 1.0.0
 * @className SetTypeEnum
 * @date 2023-08-29
 */
@Getter
public enum SetTypeEnum {
    DEFAULT(1,"Default"),
    NORMAL(2,"Normal");

    SetTypeEnum(Integer key, String value){
        this.key = key;
        this.value = value;
    }

    private Integer key;
    private String value;
    public static Integer getKeyByValue(String value){
        SetTypeEnum[] typeEnums = values();
        for (int i = 0; i < typeEnums.length; i++) {
            SetTypeEnum typeEnum = typeEnums[i];
            if (typeEnum.getValue().equals(value)){
                return typeEnum.getKey();
            }
        }
        return null;
    }

    public static String getValueByKey(Integer key){
        SetTypeEnum[] typeEnums = values();
        for (int i = 0; i < typeEnums.length; i++) {
            SetTypeEnum typeEnum = typeEnums[i];
            if (typeEnum.getKey().equals(key)){
                return typeEnum.getValue();
            }
        }
        return null;
    }
}
