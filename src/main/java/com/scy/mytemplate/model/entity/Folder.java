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
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 文件夹名称
     */
    private String name;

    /**
     * 空间类型: 'public', 'private', 'organization'
     */
    private String space;

    /**
     * 私有文件夹的拥有者用户ID (当 space="private" 时)
     */
    private Long ownerUserId;

    /**
     * 组织文件夹的拥有者组织ID (当 space="organization" 时)
     */
    private Long ownerOrganizationId;

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

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
// END OF FILE: src/main/java/com/scy/mytemplate/model/entity/Folder.java