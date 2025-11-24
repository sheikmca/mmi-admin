package sg.ncs.kp.admin.enums;

/**
 * @auther IVAN
 * @date 2022/8/25
 * @description
 */
public enum UploadStatusEnum {
    SUCCESS(2),
    ABNORMAL(1),
    FAIL(0);

    private UploadStatusEnum(Integer key) {
        this.key = key;
    }

    private Integer key;

    public Integer getKey() {
        return key;
    }

    public void setKey(Integer key) {
        this.key = key;
    }
}
