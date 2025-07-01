package com.scy.mytemplate.model.dto.node;

import lombok.Data;

import java.util.Map;

/**
 * 用于封装节点创建请求的参数对象。
 * 该类提供了一种结构化的方式来传递节点创建所需的信息，包括节点名称和节点属性。
 */
@Data
public class NodeUpdateRequest {

    /**
     * 节点的名称，用于唯一标识一个节点。
     * 在知识图谱中，节点名称是节点的重要标识，通过它可以在图谱中定位和识别特定节点。
     */
    private String name;

    /**
     * 节点的属性集合，以键值对的形式存储。
     * 这些属性用于描述节点的各种特征，例如节点的类型、相关数据等。
     * 键为属性的名称，值为属性对应的值，可以是各种数据类型（如字符串、数字、布尔值等），具体取决于知识图谱的定义和应用场景。
     */
    private Map<String, Object> properties;
}
