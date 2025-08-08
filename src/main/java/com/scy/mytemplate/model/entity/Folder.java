// FILE: src/main/java/com/scy/mytemplate/model/entity/Folder.java
package com.scy.mytemplate.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 文件夹/工作区实体类。
 * 与数据库 `Folders` 表严格对应。
 *
 * @author Bedrock
 */
@TableName(value = "Folders")
@Data
public class Folder implements Serializable {

    /**
     * 主键ID
     */
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    /**
     * 文件夹名称
     */
    private String name;

    /**
     * 空间类型: 'platform_public', 'organization_public', 'user_public', 'user_private'
     */
    private String space;

    /**
     * 私有/个人公共文件夹的拥有者用户ID
     */
    private String ownerUserId;

    /**
     * 组织文件夹的拥有者组织ID
     */
    private String ownerOrganizationId;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 是否删除 (逻辑删除标志)
     */
    @TableLogic
    private Integer isDelete;

    /**
     * 合并状态: 'NONE', 'PENDING', 'APPROVED', 'REJECTED'
     */
    private String mergeState;

    /**
     * 请求合并的目标组织ID
     */
    private String targetOrganizationId;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
// END OF FILE: src/main/java/com/scy/mytemplate/model/entity/Folder.java