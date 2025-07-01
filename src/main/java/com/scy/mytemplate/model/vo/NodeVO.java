package com.scy.mytemplate.model.vo;

import lombok.Data;

import java.util.Map;

@Data
public class NodeVO {
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
