// 文件路径: src/main/java/com/scy/mytemplate/service/impl/OrganizationServiceImpl.java
package com.scy.mytemplate.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.scy.mytemplate.common.ErrorCode;
import com.scy.mytemplate.exception.BusinessException;
import com.scy.mytemplate.mapper.OrganizationMapper;
import com.scy.mytemplate.mapper.OrganizationMemberMapper;
import com.scy.mytemplate.mapper.UserMapper;
import com.scy.mytemplate.model.dto.organization.*;
import com.scy.mytemplate.model.entity.Organization;
import com.scy.mytemplate.model.entity.OrganizationMember;
import com.scy.mytemplate.model.entity.User;
import com.scy.mytemplate.service.OrganizationService;
import com.scy.mytemplate.service.UserService;
import com.scy.mytemplate.model.vo.OrganizationVO;
import com.scy.mytemplate.model.vo.UserVO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrganizationServiceImpl extends ServiceImpl<OrganizationMapper, Organization> implements OrganizationService {

    @Resource
    private UserMapper userMapper;

    @Resource
    private UserService userService;

    @Resource
    private OrganizationMemberMapper memberMapper;

    @Override
    @Transactional
    public OrganizationVO createOrganization(OrganizationCreateRequest request, User currentUser) {
        if (StringUtils.isBlank(request.getName())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "组织名称不能为空");
        }
        // 检查组织是否重名
        if (this.count(new QueryWrapper<Organization>().eq("name", request.getName())) > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "已存在同名组织");
        }

        // 1. 创建组织
        Organization organization = new Organization();
        organization.setName(request.getName());
        organization.setDescription(request.getDescription());
        organization.setOwnerId(currentUser.getId());
        this.save(organization);

        // 2. 将创建者自动设为该组织的管理员
        OrganizationMember member = new OrganizationMember();
        member.setOrganizationId(organization.getId());
        member.setUserId(currentUser.getId());
        member.setRoleInOrg("admin");
        memberMapper.insert(member);

        return OrganizationVO.fromEntity(organization);
    }

    @Override
    public OrganizationVO updateOrganization(OrganizationUpdateRequest request, User currentUser) {
        Organization organization = this.getById(request.getId());
        if (organization == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "组织不存在");
        }
        // 权限校验：只有组织管理员或平台管理员能修改
        String roleInOrg = memberMapper.findUserRoleInOrg(currentUser.getId(), organization.getId());
        if (!"admin".equals(roleInOrg) && !userService.isAdmin(currentUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权修改该组织信息");
        }

        BeanUtils.copyProperties(request, organization);
        this.updateById(organization);
        return OrganizationVO.fromEntity(organization);
    }

    @Override
    @Transactional
    public void deleteOrganization(String organizationId, User currentUser) {
        Organization organization = this.getById(organizationId);
        if (organization == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "组织不存在");
        }
        // 权限校验：只有组织创建者(owner)或平台管理员能解散组织
        if (!organization.getOwnerId().equals(currentUser.getId()) && !userService.isAdmin(currentUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "只有组织创建者或平台管理员能解散组织");
        }

        // 1. 删除组织下的所有成员关系
        memberMapper.delete(new QueryWrapper<OrganizationMember>().eq("organizationId", organizationId));

        // 2. 逻辑删除组织本身
        // 实际项目中，还需处理组织下的文件夹、文件等资源，此处简化
        this.removeById(organizationId);
    }

    @Override
    public void inviteMember(OrganizationInviteMemberRequest request, User currentUser) {
        String orgId = request.getOrganizationId();
        Organization organization = this.getById(orgId);
        if (organization == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "组织不存在");
        }
        // 权限校验：只有组织管理员才能邀请成员
        String roleInOrg = memberMapper.findUserRoleInOrg(currentUser.getId(), orgId);
        if (!"admin".equals(roleInOrg)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "您不是组织管理员，无权邀请成员");
        }

        User invitee = userMapper.selectOne(new QueryWrapper<User>().eq("userAccount", request.getInviteeUserAccount()));
        if (invitee == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "要邀请的用户不存在");
        }
        // 检查是否已在组织内
        if (memberMapper.findUserRoleInOrg(invitee.getId(), orgId) != null) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "该用户已在组织中");
        }

        OrganizationMember newMember = new OrganizationMember();
        newMember.setOrganizationId(orgId);
        newMember.setUserId(invitee.getId());
        newMember.setRoleInOrg(request.getRoleInOrg()); // 'admin' or 'member'
        memberMapper.insert(newMember);
    }

    @Override
    public void removeMember(OrganizationRemoveMemberRequest request, User currentUser) {
        String orgId = request.getOrganizationId();
        String memberUserId = request.getMemberUserId();
        Organization organization = this.getById(orgId);
        if (organization == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "组织不存在");
        }
        // 权限校验：只有组织管理员才能移除成员
        String roleInOrg = memberMapper.findUserRoleInOrg(currentUser.getId(), orgId);
        if (!"admin".equals(roleInOrg)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "您不是组织管理员，无权移除成员");
        }
        // 组织的创建者不能被移除
        if (organization.getOwnerId().equals(memberUserId)) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "不能移除组织的创建者");
        }

        QueryWrapper<OrganizationMember> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("organizationId", orgId).eq("userId", memberUserId);
        memberMapper.delete(queryWrapper);
    }

    @Override
    public List<UserVO> listMembers(OrganizationListMembersRequest request, User currentUser) {
        String orgId = request.getOrganizationId();
        Organization organization = this.getById(orgId);
        if (organization == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "组织不存在");
        }
        // 权限校验：只有组织成员才能查看成员列表
        String roleInOrg = memberMapper.findUserRoleInOrg(currentUser.getId(), orgId);
        if (roleInOrg == null && !userService.isAdmin(currentUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权查看该组织成员");
        }

        List<User> members = memberMapper.findUsersInOrganization(orgId);
        return members.stream().map(userService::getUserVO).collect(Collectors.toList());
    }
}