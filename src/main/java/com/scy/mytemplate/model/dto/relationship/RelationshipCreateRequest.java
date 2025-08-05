// FILE: src/main/java/com/scy/mytemplate/model/dto/relationship/RelationshipCreateRequest.java
package com.scy.mytemplate.model.dto.relationship;

import lombok.Data;
import java.io.Serializable;
import java.util.Map;

/**
 * 创建新关系的请求体。
 */
@Data
public class RelationshipCreateRequest implements Serializable {
    /**
     * 关系的类型/名称, 例如 "SIMILAR_DESIGN"。
     */
    private String name;

    /**
     * 起始节点的唯一名称。
     */
    private String fromNode;

    /**
     * 结束节点的唯一名称。
     */
    private String toNode;

    /**
     * 关系自身的属性，可以为空。
     */
    private Map<String, Object> properties;

    private static final long serialVersionUID = 1L;
}
// END OF FILE: src/main/java/com/scy/mytemplate/model/dto/relationship/RelationshipCreateRequest.java