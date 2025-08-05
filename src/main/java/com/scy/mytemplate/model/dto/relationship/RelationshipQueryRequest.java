// FILE: src/main/java/com/scy/mytemplate/model/dto/relationship/RelationshipQueryRequest.java
package com.scy.mytemplate.model.dto.relationship;

import lombok.Data;
import java.io.Serializable;

/**
 * 查询单个关系的请求体。
 */
@Data
public class RelationshipQueryRequest implements Serializable {
    /**
     * 关系的类型/名称。
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

    private static final long serialVersionUID = 1L;
}
// END OF FILE: src/main/java/com/scy/mytemplate/model/dto/relationship/RelationshipQueryRequest.java