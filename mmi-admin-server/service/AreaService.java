package sg.ncs.kp.admin.service;

import sg.ncs.kp.admin.dto.AreaDTO;

/**
 * @author Duan Ran
 * @date 2022/8/23
 */
public interface AreaService {

    /**
     * sava area
     * @param areaDTO id=null is add |id!=null modify
     */
    void saveArea(AreaDTO areaDTO);

    /**
     * delete area
     * @param id area id
     */
    void delete(Integer id);
}
