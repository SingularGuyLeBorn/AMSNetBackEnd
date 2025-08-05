// FILE: src/main/java/com/scy/mytemplate/model/entity/Image.java
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
 * 图片元数据实体类。
 * 与数据库 `Images` 表严格对应。
 *
 * @author Bedrock
 */
@TableName(value = "Images")
@Data
public class Image implements Serializable {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 所属文件夹ID
     */
    private Long folderId;

    /**
     * 原始文件名
     */
    private String originalFilename;

    /**
     * 文件在对象存储或文件系统中的唯一路径/Key。
     */
    private String storagePath;

    /**
     * 图片宽度（像素）
     */
    private Integer width;

    /**
     * 图片高度（像素）
     */
    private Integer height;

    /**
     * 文件大小（字节）
     */
    private Long fileSize;

    /**
     * 上传者用户ID
     */
    private Long uploaderId;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 是否删除 (逻辑删除标志)
     */
    @TableLogic
    private Integer isDelete;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
// END OF FILE: src/main/java/com/scy/mytemplate/model/entity/Image.java