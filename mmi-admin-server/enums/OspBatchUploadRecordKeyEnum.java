package sg.ncs.kp.admin.enums;

/**
 * @author Duan Ran
 * @date 2022/3/15
 */
public enum OspBatchUploadRecordKeyEnum {
    USER_BATCH_UPLOAD("user_batch_upload");

    private OspBatchUploadRecordKeyEnum(String key) {
        this.key = key;
    }

    private String key;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
}
