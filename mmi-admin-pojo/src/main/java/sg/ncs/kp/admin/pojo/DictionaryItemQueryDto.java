package sg.ncs.kp.admin.pojo;

import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.Length;

import lombok.Getter;
import lombok.Setter;
/**
 * 
 * @date 2022/07/26 
 * @Description Dictionary Item Query Dto
 */
@Getter
@Setter
public class DictionaryItemQueryDto{
    @Length(max = 100, message = "{dictionary.code.length}")
    @NotNull(message = "{dictionary.code.notNull}")
    private String dictionaryCode;
    
    private Integer pageNo = 1;
    private Integer pageSize = 10;
}
