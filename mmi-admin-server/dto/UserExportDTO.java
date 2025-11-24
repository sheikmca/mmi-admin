package sg.ncs.kp.admin.dto;

import lombok.Data;
import sg.ncs.kp.common.uti.poi.ExcelVOAttribute;

/**
 * @auther IVAN
 * @date 2022/8/25
 * @description
 */@Data
public class UserExportDTO {

    @ExcelVOAttribute(name = "User Name", column = "A")
    private String userName;

    @ExcelVOAttribute(name = "Status", column = "B")
    private String status;

    @ExcelVOAttribute(name = "Email", column = "C")
    private String email;

    @ExcelVOAttribute(name = "Phone", column = "D")
    private String phone;

    @ExcelVOAttribute(name = "User Group Name", column = "E")
    private String userGroupName;

    @ExcelVOAttribute(name = "Role", column = "F")
    private String roleName;

    @ExcelVOAttribute(name = "Validity Start Time", column = "G")
    private String validityStartTime;

    @ExcelVOAttribute(name = "Validity End Time", column = "H")
    private String validityEndTime;

    @ExcelVOAttribute(name = "Last Login", column = "I")
    private String lastLogin;
}
