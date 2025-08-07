package com.scy.mytemplate.model.dto.organization;

import lombok.Data;

import java.io.Serializable;

@Data
public class OrganizationUpdateRequest implements Serializable {
    // 要更新的组织的ID (必填)
    private String id;
    // 新的组织名称 (选填)
    private String name;
    // 新的组织简介 (选填)
    private String description;
}