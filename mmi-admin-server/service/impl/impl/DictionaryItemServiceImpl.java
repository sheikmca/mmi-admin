package sg.ncs.kp.admin.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.convert.Convert;
import sg.ncs.kp.admin.mapper.DictionaryItemMapper;
import sg.ncs.kp.admin.po.DictionaryItemPo;
import sg.ncs.kp.admin.pojo.DictionaryItemQueryDto;
import sg.ncs.kp.admin.pojo.DictionaryItemVo;
import sg.ncs.kp.admin.service.DictionaryItemService;
import sg.ncs.kp.common.core.response.PageResult;
import sg.ncs.kp.common.i18n.util.MessageUtils;


@Service
public class DictionaryItemServiceImpl implements DictionaryItemService {
    @Autowired
    private DictionaryItemMapper dictionaryItemMapper;
    @Autowired
    private MessageUtils messageUtils;

    @Override
    public PageResult<DictionaryItemVo> pageListByDictionaryCode(DictionaryItemQueryDto dictionaryItemQueryDto) {
        Page<DictionaryItemPo> dictionaryItemPoPage = dictionaryItemMapper.pageListByDictionaryCode(
                new Page<>(dictionaryItemQueryDto.getPageNo(), dictionaryItemQueryDto.getPageSize()),
                dictionaryItemQueryDto.getDictionaryCode());

        PageResult<DictionaryItemVo> pageResult = messageUtils.pageResult(
                Convert.toInt(dictionaryItemPoPage.getCurrent()), Convert.toInt(dictionaryItemPoPage.getSize()), dictionaryItemPoPage.getTotal(), null);
        if (!dictionaryItemPoPage.getRecords().isEmpty()) {
            pageResult.setData(BeanUtil.copyToList(dictionaryItemPoPage.getRecords(), DictionaryItemVo.class));
        }
        return pageResult;
    }
    

}
