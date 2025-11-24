package sg.ncs.kp.admin.mapper;

import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import sg.ncs.kp.admin.po.DictionaryItemPo;



public interface DictionaryItemMapper extends BaseMapper<DictionaryItemPo>{
	Page<DictionaryItemPo> pageListByDictionaryCode(Page<DictionaryItemPo> page,@Param("dictionaryCode") String dictionaryCode);
}
