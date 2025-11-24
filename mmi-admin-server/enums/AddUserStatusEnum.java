package sg.ncs.kp.admin.enums;

/**
 * @author P1312422
 * @auther Chen Xi
 * @date 2023-08-24
 * @description
 */
public enum AddUserStatusEnum {
    SUCCESS("Success"),
    FAIL("Fail");

    private AddUserStatusEnum(String value) {
        this.value = value;
    }

    private String value;

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
