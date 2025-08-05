// FILE: src/main/java/com/scy/mytemplate/model/dto/relationship/RelationshipUpdateRequest.java
package com.scy.mytemplate.model.dto.relationship;

import lombok.Data;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * 更新关系的请求体。
 */
@Data
public class RelationshipUpdateRequest implements Serializable {
    // --- 用于定位关系的字段 ---
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

    // --- 用于更新的字段 ---
    /**
     * 要添加或更新的属性集合。
     */
    private Map<String, Object> propertiesToSet;
    /**
     * 要移除的属性的键列表。
     */
    private List<String> propertiesToRemove;

    private static final long serialVersionUID = 1L;
}
// END OF FILE: src/main/java/com/scy/mytemplate/model/dto/relationship/RelationshipUpdateRequest.java