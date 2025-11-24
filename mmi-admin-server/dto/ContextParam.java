package sg.ncs.kp.admin.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @className ContextParam
 * @author chen xi
 * @version 1.0.0
 * @date 2022-08-19
 */

@Getter
@Setter
@ToString
public class ContextParam {
    private String authnAttemptId;
    private String messageId;
    private String inResponseTo;
}
