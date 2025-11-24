package sg.ncs.kp.admin.po;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import sg.ncs.kp.common.mybaits.base.po.AutoIncrementPo;

/**
 * <p>
 * upload record info table
 * </p>
 *
 * @author IVAN
 * @since 2022-08-21
 */
@Getter
@Setter
@ToString
@TableName("kp_upload_record")
public class UploadRecord extends AutoIncrementPo {

    private static final long serialVersionUID = 1L;

    /**
     * file name
     */
    @TableField("file_name")
    private String fileName;
    /**
     * file size
     */
    @TableField("file_size")
    private Integer fileSize;
    /**
     * upload key
     */
    @TableField("upload_key")
    private String uploadKey;
    /**
     * path
     */
    @TableField("path")
    private String path;
    /**
     * 0.fail 1.abnormal 2.success
     */
    @TableField("status")
    private Integer status;
    /**
     * remark
     */
    @TableField("remark")
    private String remark;
}
