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
import sg.ncs.kp.common.mybaits.base.po.AutoIncrementPo;
import sg.ncs.kp.uaa.common.dto.BaseDTO;

/**
 * UMMI Entity for kp_ldap table
 * 
 * @author chen xi
 * @date 2023-07-10
 */
@TableName("kp_workspace")
@Getter
@Setter
public class Workspace extends AutoIncrementPo {

	private static final long serialVersionUID = 1L;

	@TableField("name")
	private String name;
	@TableField("front_style")
	private String frontStyle;
	@TableField("front_size")
	private Integer frontSize;
	@TableField("set_type")
	private Integer setType;
	@TableField("binding_id")
	private Integer bindingId;
	@TableField("theme")
	private Integer theme;
	@TableField("track_num")
	private Integer trackNum;
	@TableField("roi_color")
	private String roiColor;
	@TableField("eo_setting")
	private String eoSetting;
	@TableField("nfoa_setting")
	private String nfoaSetting;
	@TableField("dashboard_setting")
	private String dashboardSetting;
	@TableField("tenant_id")
	private String tenantId;
	
}
