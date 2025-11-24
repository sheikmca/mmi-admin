package sg.ncs.kp.admin.service;

import sg.ncs.kp.admin.pojo.DictionaryItemQueryDto;
import sg.ncs.kp.admin.pojo.DictionaryItemVo;
import sg.ncs.kp.common.core.response.PageResult;

/**
 * 
 * @author Lai Yin BO
 * @date 2022/07/26 
 * @Description Dictionary item Service
 */
public interface DictionaryItemService {
    /**
     * 
     * @Title pageListByDictionaryCode 
     * @Description Page list DictionaryItem by dictionary code
     * @param dictionaryItemQueryDto page information and dictionary code
     * @return PageResult<DictionaryItemVo>
     */
    public PageResult<DictionaryItemVo> pageListByDictionaryCode(DictionaryItemQueryDto dictionaryItemQueryDto);
}
