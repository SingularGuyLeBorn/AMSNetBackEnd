package com.scy.mytemplate.model.dto.organization;

import lombok.Data;

import java.io.Serializable;

@Data
public class OrganizationRemoveMemberRequest implements Serializable {
    // 目标组织的ID (必填)
    private String organizationId;
    // 要被移除的成员的用户ID (必填)
    private String memberUserId;
}