// 文件路径: src/main/java/com/scy/mytemplate/service/OrganizationService.java
package com.scy.mytemplate.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.scy.mytemplate.model.dto.organization.*;
import com.scy.mytemplate.model.entity.Organization;
import com.scy.mytemplate.model.entity.User;
import com.scy.mytemplate.model.vo.OrganizationVO;
import com.scy.mytemplate.model.vo.UserVO;

import java.util.List;

public interface OrganizationService extends IService<Organization> {

    OrganizationVO createOrganization(OrganizationCreateRequest request, User currentUser);

    OrganizationVO updateOrganization(OrganizationUpdateRequest request, User currentUser);

    void deleteOrganization(String organizationId, User currentUser);

    void inviteMember(OrganizationInviteMemberRequest request, User currentUser);

    void removeMember(OrganizationRemoveMemberRequest request, User currentUser);

    List<UserVO> listMembers(OrganizationListMembersRequest request, User currentUser);
}