package sg.ncs.kp.admin.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sg.ncs.kp.admin.pojo.DictionaryItemQueryDto;
import sg.ncs.kp.admin.pojo.DictionaryItemVo;
import sg.ncs.kp.admin.service.DictionaryItemService;
import sg.ncs.kp.common.core.response.PageResult;

import javax.validation.Valid;
import java.util.Objects;

/**
 * 
 * @author Lai Yin BO
 * @date 2022/07/26 
 * @Description Dictionary Item Controller
 */
@RestController
@RequestMapping("/dictionary-item")
public class DictionaryItemController {
    @Autowired
    private DictionaryItemService dictionaryItemService;

    /**
     * 
     * @Title pageList 
     * @Description Page list dictionary item based on dictionary code
     * @param dictionaryItemQueryDto
     * @return PageResult<DictionaryItemVo>
     */
    @PostMapping("/page-list-by-dictionary-code")
    public PageResult<DictionaryItemVo> pageList(@Valid @RequestBody DictionaryItemQueryDto dictionaryItemQueryDto) {
        if(Objects.isNull(dictionaryItemQueryDto.getPageNo())|| dictionaryItemQueryDto.getPageNo() < 1) {
            dictionaryItemQueryDto.setPageNo(1);
        }
        
        if(Objects.isNull(dictionaryItemQueryDto.getPageSize())|| dictionaryItemQueryDto.getPageSize() < 1) {
            dictionaryItemQueryDto.setPageSize(10);
        }
        return dictionaryItemService.pageListByDictionaryCode(dictionaryItemQueryDto);
    }

}
