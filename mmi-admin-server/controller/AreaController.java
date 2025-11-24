package sg.ncs.kp.admin.controller;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sg.ncs.kp.admin.dto.AreaDTO;
import sg.ncs.kp.admin.service.AreaService;
import sg.ncs.kp.common.core.response.Result;
import sg.ncs.kp.common.i18n.util.MessageUtils;
import sg.ncs.kp.uaa.common.dto.UserModifyDTO;

import javax.annotation.Resource;
import javax.validation.Valid;

/**
 * @author Duan Ran
 * @date 2022/8/23
 */
@RestController
@RequestMapping("/area")
public class AreaController {

    @Resource
    private AreaService areaService;
    @Resource
    private MessageUtils messageUtils;


    @PostMapping
    public Result save(@Valid @RequestBody AreaDTO areaDTO){
        areaService.saveArea(areaDTO);
        return messageUtils.addSucceed(null);
    }

    @DeleteMapping("/{id}")
    public Result delete(@PathVariable("id") Integer id){
        areaService.delete(id);
        return messageUtils.deleteSucceed(null);
    }
}
