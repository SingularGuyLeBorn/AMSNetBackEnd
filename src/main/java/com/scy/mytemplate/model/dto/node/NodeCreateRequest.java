// FILE: src/main/java/com/scy/mytemplate/model/dto/node/NodeCreateRequest.java
package com.scy.mytemplate.model.dto.node;

import lombok.Data;
import java.io.Serializable;
import java.util.Map;

/**
 * 创建新节点的请求体。
 * 用于封装从前端传递过来的、用于创建一个新图谱节点的数据。
 */
@Data
public class NodeCreateRequest implements Serializable {
    /**
     * 节点的唯一名称。
     * <strong>关键</strong>: 这个名称必须与 MySQL `Images` 表中的 `storagePath` 字段完全一致，
     * 以此建立图数据库和关系数据库之间的关联。
     */
    private String name;

    /**
     * 节点的初始属性集合。
     * 归属信息(space, ownerId, organizationId)将由后端自动注入，前端无需关心。
     * { "type": "bandgap_reference", "voltage": "1.2V" }
     */
    private Map<String, Object> properties;

    private static final long serialVersionUID = 1L;
}
// END OF FILE: src/main/java/com/scy/mytemplate/model/dto/node/NodeCreateRequest.java