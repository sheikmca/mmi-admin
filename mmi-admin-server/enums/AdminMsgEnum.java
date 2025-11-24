package sg.ncs.kp.admin.enums;

import sg.ncs.kp.common.i18n.pojo.BaseMsgEnum;

/**
 * @author Duan Ran
 * @date 2022/8/23
 */
public enum AdminMsgEnum implements BaseMsgEnum {
    AREA_NAME_EXIST("02000001", "area.name.empty"),
    AREA_NAME_REPEAT("02000002", "area.name.repeat"),
    AREA_NOT_EXIST("02000003", "area.not.exist"),
    PARENT_AREA_NOT_EXIST("02000004", "parent.area.not.exist"),
    FILE_IS_EMPTY("02000005","file.empty"),
    UN_SUPPORT_FILE_FORMAT("02000006","un.support.file.format"),
    PHONE_INVALID("02000007", "phone.invalid"),
    EMAIL_INVALID("02000008", "email.invalid"),
    COUNTRY_CODE_EMPTY("02000009","country.code.empty"),
    IP_START_GT_IP_END("02000010","ip.start.gt.ip.end"),
    IP_INVALID("02000011","ip.invalid"),
    IP_START_LACK("02000012","ip.start.lack"),
    IP_END_LACK("02000013","ip.end.lack"),
    VALIDITY_TIME_LACK("02000014","validity.time.lack" ),
    VALIDITY_TIME_ERROR("02000015","validity.time.error" ),
    EXCEL_NO_DATA("02000016","excel.no.data"),
    EXCEL_ROW_LIMIT("02000017","excel.row.limit"),
    FILE_LOAD_FAIL("02000018","file.load.fail"),
    USER_FILE_ERROR("02000019","user.file.error"),
    USERNAME_EMPTY("02000020","username.empty"),
    COUNTRY_CODE_INVALID("02000021","country.code.invalid"),
    USER_ROLE_INVALID("02000022","user.role.invalid"),
    USER_DOWNLOAD_FILE_FAIL("02000023","user.download.file.fail"),
    IDS_EMPTY("02000024","ids.empty"),
    FIELD_LENGTH_ILLEGAL("02000025","field.length.illegal"),
    USER_HAVE_CHILD_USER("02000026","user.have.child.user"),
    SUPER_ADMIN_CANNOT_OPERATE("02000027","super.admin.cannot.operate"),
    FORCED_OFFLINE("02000028","forced.offline"),
    ROLE_IS_NULL("02000029","role.is.null"),
    FULL_NAME_EMPTY("02000030","full.name.empty"),
    USER_GROUP_INVALID("02000031","user.group.invalid"),
    USER_NOT_OPERATE_SELF("02000032","user.can.not.operate.self"),
    USER_INCORRECT_CREDENTIALS("02000033","user.incorrect.credentials"),
    VERITY_2FA_FAILED("02000033","verity.2fa.failed"),
    VERITY_2FA_EXCEEDS_MAXNUM("02000034","verity.2fa.exceeds.maxnum"),
    NEED_2FA_CHECK("02000035","need.2fa.check"),
    WORKSPACE_NAME_EXIST("02000036", "workspace.name.empty"),
    WORKSPACE_NAME_REPEAT("02000037", "workspace.name.repeat"),
    WORKSPACE_NOT_EXIST("02000038", "workspace.not.exist"),
    DISCONNECT_SUCCESS("02000039", "disconnect.success"),
    BATCH_USER_NUM_ERROR("02000040","batch.user.num.error"),
    WORKSPACE_RESET_TYPE_ERROR("02000041", "workspace.reset.type.error"),
    WORKSPACE_DEFAULT_ALREADY_DEL("02000041", "workspace.default.already.del"),
    USER_GROUP_AUTH_ERROR("02000041", "user.group.auth.error"),
    END("01003001", "role.name.exist");

    private String code;
    private String val;

    private AdminMsgEnum(String code, String val) {
        this.code = code;
        this.val = val;
    }

    public String code() {
        return this.code;
    }

    public String val() {
        return this.val;
    }
}
