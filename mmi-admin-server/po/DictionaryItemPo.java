package sg.ncs.kp.admin.po;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Getter;
import lombok.Setter;
import sg.ncs.kp.common.mybaits.base.po.AutoIncrementPo;
/**
 * 
 * @author Lai Yin BO
 * @date 2022/07/26 
 * @Description Persistent object of Dictionary Item
 */
@TableName("kp_dictionary_item")
@Getter
@Setter
public class DictionaryItemPo extends AutoIncrementPo {

    private static final long serialVersionUID = 1L;
    @TableField("dict_id")
    private String dictId;
    @TableField("name")
    private String name;
    @TableField("value")
    private String value;
    @TableField("description")
    private String description;
    @TableField("sort_order")
    private Integer sortOrder;
    @TableField("version")
    private Integer version;
    @TableField("tenant_id")
    private String tenantId;
    @Override
    public int hashCode() {
        return super.hashCode();
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        DictionaryItemPo other = (DictionaryItemPo) obj;
        if (super.getId() == null) {
            if (other.getId() != null)
                return false;
        } else if (!super.getId().equals(other.getId()))
            return false;
        return true;
    }

    
}
