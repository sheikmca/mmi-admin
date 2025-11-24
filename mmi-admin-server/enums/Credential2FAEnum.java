package sg.ncs.kp.admin.enums;
/**
 * @className RsaCredentialEnum
 * @author chen xi
 * @version 1.0.0
 * @date 2022-08-19
 */
public enum Credential2FAEnum {
    SECURID("SECURID");
    private String value;
    private Credential2FAEnum(String value) {
        this.value = value;
    }
    public String getValue() {
        return value;
    }
}
