// FILE: src/main/java/com/scy/mytemplate/model/entity/Annotation.java
package com.scy.mytemplate.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 标注数据实体类。
 * 与数据库 `Annotations` 表严格对应。
 *
 * @author Bedrock
 */
@TableName(value = "Annotations")
@Data
public class Annotation implements Serializable {

    /**
     * 主键ID
     */
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    /**
     * 对应的图片ID (一对一关系)
     */
    private String imageId;

    /**
     * 标注数据 (JSON 格式字符串)
     * 注意：实体类中使用 String 类型接收，MyBatis-Plus 和 JDBC 会处理与数据库 JSON 类型的转换。
     */
    private String jsonContent;

    /**
     * 最后修改者ID
     */
    private String lastEditorId;

    /**
     * 更新时间
     */
    private Date updateTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
// END OF FILE: src/main/java/com/scy/mytemplate/model/entity/Annotation.java