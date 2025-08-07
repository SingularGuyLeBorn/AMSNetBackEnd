// FILE: src/main/java/com/scy/mytemplate/model/vo/UserOrganizationInfoVO.java
package com.scy.mytemplate.model.vo;

import lombok.Data;
import java.io.Serializable;

/**
 * 用户组织信息视图对象 (User Organization Info View Object)。
 * 这是一个专门用于描述用户与某一个特定组织隶属关系的数据传输对象。
 * 它像一张“组织名片”，清晰地表明了用户属于哪个组织，以及在该组织中的角色。
 *
 * @author Bedrock
 */
@Data
public class UserOrganizationInfoVO implements Serializable {

    /**
     * 组织的唯一ID。
     */
    private String organizationId;

    /**
     * 组织的名称。
     * 此字段用于前端直接显示，避免了前端需要根据 organizationId 再次请求组织详情的额外开销。
     */
    private String organizationName;

    /**
     * 用户在此组织中的角色。
     * 例如: 'admin', 'member'。这是前端实现权限控制（如显示/隐藏管理按钮）的关键依据。
     */
    private String roleInOrg;

    private static final long serialVersionUID = 1L;
}
// END OF FILE: src/main/java/com/scy/mytemplate/model/vo/UserOrganizationInfoVO.java