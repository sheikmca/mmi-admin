package sg.ncs.kp.admin.dto;

import lombok.Data;
import sg.ncs.kp.common.uti.poi.ExcelVOAttribute;

import java.time.LocalDateTime;

/**
 * @auther IVAN
 * @date 2022/8/25
 * @description
 */
@Data
public class UserUploadViewDTO {

    @ExcelVOAttribute(name = "User Name", column = "A")
    private String userName;

    @ExcelVOAttribute(name = "Full Name", column = "B")
    private String fullName;

    @ExcelVOAttribute(name = "Email", column = "C")
    private String email;

    @ExcelVOAttribute(name = "Country Code", column = "D")
    private String countryCode;

    @ExcelVOAttribute(name = "Phone", column = "E")
    private String phone;

    @ExcelVOAttribute(name = "User Group Name", column = "F")
    private String userGroupName;

    @ExcelVOAttribute(name = "Validity Start Time", column = "G")
    private LocalDateTime validityStartTime;

    @ExcelVOAttribute(name = "Validity End Time", column = "H")
    private LocalDateTime validityEndTime;

    @ExcelVOAttribute(name = "Role", column = "I")
    private String role;

    @ExcelVOAttribute(name = "IP Start", column = "J")
    private String ipStart;

    @ExcelVOAttribute(name = "IP End", column = "K")
    private String ipEnd;

    @ExcelVOAttribute(name = "Description", column = "L")
    private String description;
}
