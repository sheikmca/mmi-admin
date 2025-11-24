package sg.ncs.kp.admin.enums;

import lombok.Getter;

/**
 * @author chen xi
 * @version 1.0.0
 * @className FrontSizeTypeEnum
 * @date 2023-07-25
 */
@Getter
public enum ThemeTypeEnum {
    DARK(1,"Dark"),
    LIGHT(2,"Light");

    ThemeTypeEnum(Integer key, String value){
        this.key = key;
        this.value = value;
    }

    private Integer key;
    private String value;
    public static Integer getKeyByValue(String value){
        ThemeTypeEnum[] typeEnums = values();
        for (int i = 0; i < typeEnums.length; i++) {
            ThemeTypeEnum typeEnum = typeEnums[i];
            if (typeEnum.getValue().equals(value)){
                return typeEnum.getKey();
            }
        }
        return null;
    }

    public static String getValueByKey(Integer key){
        ThemeTypeEnum[] typeEnums = values();
        for (int i = 0; i < typeEnums.length; i++) {
            ThemeTypeEnum typeEnum = typeEnums[i];
            if (typeEnum.getKey().equals(key)){
                return typeEnum.getValue();
            }
        }
        return null;
    }
}
