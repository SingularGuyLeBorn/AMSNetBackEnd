package com.scy.mytemplate.model.dto.relationship;

import lombok.Data;

import java.util.Map;

/**
 * 用于封装关系创建请求的参数对象。
 * 该类提供了一种结构化的方式来传递关系创建所需的信息，包括关系名称和关系属性。
 */
@Data
public class RelationshipCreateRequest {
    /**
     * 关系的名称，用于标识关系的类型。
     * 在知识图谱中，关系名称确定了节点之间的连接类型，例如“父子关系”、“朋友关系”等。
     */
    private String name;

    /**
     * 关系的属性集合，以键值对的形式存储。
     * 这些属性用于描述关系的各种特征，例如关系的权重、方向等。
     * 键为属性的名称，值为属性对应的值，可以是各种数据类型（如字符串、数字、布尔值等），具体取决于知识图谱的定义和应用场景。
     */
    private Map<String, Object> properties;
}