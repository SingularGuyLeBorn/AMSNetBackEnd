package com.scy.mytemplate.model.dto.organization;

import lombok.Data;

import java.io.Serializable;

@Data
public class OrganizationInviteMemberRequest implements Serializable {
    // 目标组织的ID (必填)
    private String organizationId;
    // 被邀请用户的账号 (必填, 也可以是邮箱或手机号，取决于您的设计)
    private String inviteeUserAccount;
    // 邀请后，该成员在组织内的角色 (必填: 'admin' 或 'member')
    private String roleInOrg;
}