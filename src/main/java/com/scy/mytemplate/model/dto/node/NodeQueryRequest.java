// FILE: src/main/java/com/scy/mytemplate/model/dto/node/NodeQueryRequest.java
package com.scy.mytemplate.model.dto.node;

import lombok.Data;
import java.io.Serializable;

/**
 * 查询单个节点的请求体。
 */
@Data
public class NodeQueryRequest implements Serializable {
    /**
     * 要查询的节点的唯一名称。
     */
    private String name;

    private static final long serialVersionUID = 1L;
}
// END OF FILE: src/main/java/com/scy/mytemplate/model/dto/node/NodeQueryRequest.java