// FILE: src/main/java/com/scy/mytemplate/model/dto/node/NodeDeleteRequest.java
package com.scy.mytemplate.model.dto.node;

import lombok.Data;
import java.io.Serializable;

/**
 * 删除节点的请求体。
 */
@Data
public class NodeDeleteRequest implements Serializable {
    /**
     * 要删除的节点的唯一名称。
     */
    private String name;

    private static final long serialVersionUID = 1L;
}
// END OF FILE: src/main/java/com/scy/mytemplate/model/dto/node/NodeDeleteRequest.java