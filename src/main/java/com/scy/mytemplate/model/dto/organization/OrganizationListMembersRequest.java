package com.scy.mytemplate.model.dto.organization;

import lombok.Data;

import java.io.Serializable;

@Data
public class OrganizationListMembersRequest implements Serializable {
    // 目标组织的ID (必填)
    private String organizationId;
    // (可选) 为未来的分页和搜索预留
    // private int current = 1;
    // private int pageSize = 20;
    // private String searchKeyword;
}