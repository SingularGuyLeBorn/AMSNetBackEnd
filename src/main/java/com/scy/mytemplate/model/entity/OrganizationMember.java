// FILE: src/main/java/com/scy/mytemplate/model/entity/OrganizationMember.java
package com.scy.mytemplate.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 组织成员关系实体类。
 * 与数据库 `OrganizationMembers` 表严格对应。
 *
 * @author Bedrock
 */
@TableName(value = "OrganizationMembers")
@Data
public class OrganizationMember implements Serializable {

    /**
     * 主键ID
     */
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    /**
     * 组织ID
     */
    private String organizationId;

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 成员在组织内的角色: 'admin', 'member'
     */
    private String roleInOrg;

    /**
     * 加入时间
     */
    private Date joinTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
// END OF FILE: src/main/java/com/scy/mytemplate/model/entity/OrganizationMember.java