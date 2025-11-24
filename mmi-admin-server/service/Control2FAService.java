package sg.ncs.kp.admin.service;

import org.apache.ibatis.annotations.Param;
import sg.ncs.kp.admin.po.Control2FA;
import sg.ncs.kp.common.exception.pojo.ServiceException;

import java.util.List;

/**
 * @className Control2FAService
 * @author chen xi
 * @version 1.0.0
 * @date 2023-07-11
 */

public interface Control2FAService {

    Boolean getStatusByRoleId(Integer roleId);

    void addOrUpdateControl2FA(Integer roleId, Boolean controlStatus);

    void deleteControl2FA(Integer roleId);

    List<Control2FA> getControl2fas(String userId, Integer roleStatus);

    /**
     * @description 2fa check
     * @param userId, code
     * @return void
     * @author chen xi
     * @version 1.0.0
     * @date 2022-08-19
     */
   void check2FA(String userId, String code);
    /**
     * @description init failed number
     * @param userId
     * @return void
     * @author chen xi
     * @version 1.0.0
     * @date 2022-08-26
     */
    void initFailedNum(String userId);
}
