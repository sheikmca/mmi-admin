/*  
* =========================================================================  
* Copyright 2019 NCS Pte. Ltd. All Rights Reserved  
*  
* This software is confidential and proprietary to NCS Pte. Ltd. You shall  
* use this software only in accordance with the terms of the licence  
* agreement you entered into with NCS.  No aspect or part or all of this  
* software may be reproduced, modified or disclosed without full and  
* direct written authorisation from NCS.  
*  
* NCS SUPPLIES THIS SOFTWARE ON AN AS IS BASIS. NCS MAKES NO  
* REPRESENTATIONS OR WARRANTIES, EITHER EXPRESSLY OR IMPLIEDLY, ABOUT THE  
* SUITABILITY OR NON-INFRINGEMENT OF THE SOFTWARE. NCS SHALL NOT BE LIABLE  
* FOR ANY LOSSES OR DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING,  
* MODIFYING OR DISTRIBUTING THIS SOFTWARE OR ITS DERIVATIVES.  
* =========================================================================  
*/

package sg.ncs.kp.admin.po;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;
import sg.ncs.kp.uaa.common.dto.BaseDTO;

/**
 * UMMI Entity for kp_ldap table
 * 
 * @author chen xi
 * @date 2023-07-10
 */
@TableName("kp_ldap")
@Getter
@Setter
public class AdServer extends BaseDTO {

	private static final long serialVersionUID = 1L;

	@TableId("id")
	private Integer id;
	@TableField("ad_name")
	private String adName;
	@TableField("ad_address")
	private String adAddress;
	@TableField("ad_port")
	private String adPort;
	@TableField("ad_base")
	private String adBase;
	@TableField("ad_domain")
	private String adDomain;
	@TableField("ad_status")
	private Integer adStatus;
	@TableField("tenant_id")
	private String tenantId;
	
}
