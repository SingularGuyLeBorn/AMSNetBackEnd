// FILE: src/main/java/com/scy/mytemplate/model/entity/Organization.java
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
 * 组织实体类。
 * 与数据库 `Organizations` 表严格对应。
 *
 * @author Bedrock
 */
@TableName(value = "Organizations")
@Data
public class Organization implements Serializable {

    /**
     * 组织ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 组织名称
     */
    private String name;

    /**
     * 组织创建者/拥有者ID
     */
    private Long ownerId;

    /**
     * 组织简介
     */
    private String description;

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
// END OF FILE: src/main/java/com/scy/mytemplate/model/entity/Organization.java