// FILE: src/main/java/com/scy/mytemplate/model/dto/node/NodeUpdateRequest.java
package com.scy.mytemplate.model.dto.node;

import lombok.Data;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * 更新节点的请求体。
 * 提供了强大的更新能力，可以同时添加/修改属性，以及移除指定属性。
 */
@Data
public class NodeUpdateRequest implements Serializable {
    /**
     * 要更新的节点的唯一名称。
     */
    private String name;

    /**
     * 要添加或更新的属性集合。
     * 如果属性键已存在，则更新其值；如果不存在，则添加新属性。
     */
    private Map<String, Object> propertiesToSet;

    /**
     * 要移除的属性的键（Key）列表。
     * 后端将根据这个列表中的字符串来移除节点对应的属性。
     */
    private List<String> propertiesToRemove;

    private static final long serialVersionUID = 1L;
}
// END OF FILE: src/main/java/com/scy/mytemplate/model/dto/node/NodeUpdateRequest.java