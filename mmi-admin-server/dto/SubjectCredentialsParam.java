package sg.ncs.kp.admin.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

/**
 * @className SubjectCredentialsParam
 * @author chen xi
 * @version 1.0.0
 * @date 2022-08-19
 */

@Getter
@Setter
@ToString
public class SubjectCredentialsParam {
    private String methodId;
    private List<CollectedInputsParam> collectedInputs;
}
