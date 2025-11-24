package sg.ncs.kp.admin.pojo;

import lombok.Getter;
import lombok.Setter;
/**
 * 
 * @date 2022/07/26
 * @Description Dictionary Query Dto
 */
@Getter
@Setter
public class DictionaryQueryDto{
    private String name;
    private Integer pageNo = 1;
    private Integer pageSize = 10;
}
