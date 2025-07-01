package com.scy.mytemplate.model.entity;

import lombok.Data;

import java.util.Map;
@Data
public class Relationship {
    private String name;
    private Map<String, Object> properties;

    public Relationship(String name, Map<String, Object> properties) {
        this.name = name;
        this.properties = properties;
    }

    // 无参构造
    public Relationship() {
    }
}
