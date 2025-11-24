package sg.ncs.kp.admin.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

/**
 * @className VerifyParam
 * @author chen xi
 * @version 1.0.0
 * @date 2022-08-19
 */

@Getter
@Setter
@ToString
public class VerifyParam {
    private String clientId;
    private String subjectName;
    private ContextParam context;
    private List<SubjectCredentialsParam> subjectCredentials;
}
