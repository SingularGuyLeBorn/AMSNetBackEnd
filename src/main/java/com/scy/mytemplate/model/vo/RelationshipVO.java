// FILE: src/main/java/com/scy/mytemplate/model/vo/RelationshipVO.java
package com.scy.mytemplate.model.vo;

import lombok.Data;
import java.io.Serializable;
import java.util.Map;

/**
 * 关系视图对象 (Relationship View Object)。
 * 作为数据传输层，用于向前端安全地返回关系的完整信息。
 */
@Data
public class RelationshipVO implements Serializable {
    /**
     * 关系的类型或名称，例如 "SIMILAR_DESIGN"。
     */
    private String name;

    /**
     * 关系的起始节点的唯一名称。
     */
    private String fromNode;

    /**
     * 关系的结束节点的唯一名称。
     */
    private String toNode;

    /**
     * 关系自身的属性集合。
     */
    private Map<String, Object> properties;

    private static final long serialVersionUID = 1L;
}
// END OF FILE: src/main/java/com/scy/mytemplate/model/vo/RelationshipVO.java