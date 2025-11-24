package sg.ncs.kp.admin.pojo;
/**
 * @className DirectionEnum
 * @version 1.0.0
 * @date 2023-08-21
 */
public enum DirectionEnum {
    EAST("East",2,"East Site"),
    WEST("West",3,"West Site");

    private DirectionEnum(String key, Integer index,String value) {
        this.key = key;
        this.index = index;
        this.value = value;
    }

    private String key;
    private String value;
    private Integer index;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Integer getIndex() {
        return index;
    }

    public void setIndex(Integer index) {
        this.index = index;
    }

    public static String getValueByKey(String key){
        DirectionEnum[] directionEnums = values();
        for (int i = 0; i < directionEnums.length; i++) {
            DirectionEnum directionEnum = directionEnums[i];
            if (directionEnum.getKey().equals(key)){
                return directionEnum.getValue();
            }
        }
        return null;
    }

    public static Integer getIndexByKey(String key){
        DirectionEnum[] directionEnums = values();
        for (int i = 0; i < directionEnums.length; i++) {
            DirectionEnum directionEnum = directionEnums[i];
            if (directionEnum.getKey().equals(key)){
                return directionEnum.getIndex();
            }
        }
        return null;
    }
}
