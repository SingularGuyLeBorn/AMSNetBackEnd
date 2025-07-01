package com.scy.mytemplate.model.entity;

import lombok.Data;

import java.util.Map;

@Data
// 节点内部类
public class Node {
    private String name;
    private Map<String, Object> properties;

    public Node(String name, Map<String, Object> properties) {
        this.name = name;
        this.properties = properties;
    }

    // 无参构造函数
    public Node() {
    }
}
