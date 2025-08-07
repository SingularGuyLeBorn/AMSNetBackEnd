package com.scy.mytemplate.model.dto.organization;

import lombok.Data;

import java.io.Serializable;

@Data
public class OrganizationCreateRequest implements Serializable {
    // 组织的名称 (必填)
    private String name;
    // 组织的简介 (选填)
    private String description;
}