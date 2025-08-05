// FILE: src/main/java/com/scy/mytemplate/model/dto/relationship/RelationshipDeleteRequest.java
package com.scy.mytemplate.model.dto.relationship;

import lombok.Data;
import java.io.Serializable;

/**
 * 删除关系的请求体。
 * 通过起始节点、结束节点和关系类型来唯一确定一条关系。
 */
@Data
public class RelationshipDeleteRequest implements Serializable {
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
// END OF FILE: src/main/java/com/scy/mytemplate/model/dto/relationship/RelationshipDeleteRequest.java