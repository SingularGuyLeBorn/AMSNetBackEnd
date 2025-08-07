// 文件路径: src/main/java/com/scy/mytemplate/controller/OrganizationController.java
package com.scy.mytemplate.controller;

import com.scy.mytemplate.common.BaseResponse;
import com.scy.mytemplate.common.DeleteRequest;
import com.scy.mytemplate.common.ErrorCode;
import com.scy.mytemplate.common.ResultUtils;
import com.scy.mytemplate.exception.BusinessException;
import com.scy.mytemplate.model.dto.organization.*;
import com.scy.mytemplate.model.entity.User;
import com.scy.mytemplate.model.vo.OrganizationVO;
import com.scy.mytemplate.model.vo.UserVO;
import com.scy.mytemplate.service.OrganizationService;
import com.scy.mytemplate.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/organization")
@Api(tags = "OrganizationController")
@Slf4j
public class OrganizationController {

    @Resource
    private OrganizationService organizationService;

    @Resource
    private UserService userService;

    @PostMapping("/create")
    @ApiOperation("创建新组织")
    public BaseResponse<OrganizationVO> createOrganization(@RequestBody OrganizationCreateRequest createRequest, HttpServletRequest request) {
        if (createRequest == null) throw new BusinessException(ErrorCode.PARAMS_ERROR);
        User currentUser = userService.getLoginUser(request);
        OrganizationVO organizationVO = organizationService.createOrganization(createRequest, currentUser);
        return ResultUtils.success(organizationVO);
    }

    @PostMapping("/update")
    @ApiOperation("更新组织信息")
    public BaseResponse<OrganizationVO> updateOrganization(@RequestBody OrganizationUpdateRequest updateRequest, HttpServletRequest request) {
        if (updateRequest == null || updateRequest.getId() == null) throw new BusinessException(ErrorCode.PARAMS_ERROR);
        User currentUser = userService.getLoginUser(request);
        OrganizationVO organizationVO = organizationService.updateOrganization(updateRequest, currentUser);
        return ResultUtils.success(organizationVO);
    }

    @PostMapping("/delete")
    @ApiOperation("解散组织 (高危操作)")
    public BaseResponse<Boolean> deleteOrganization(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() == null || deleteRequest.getId() == null) throw new BusinessException(ErrorCode.PARAMS_ERROR);
        User currentUser = userService.getLoginUser(request);
        organizationService.deleteOrganization(deleteRequest.getId(), currentUser);
        return ResultUtils.success(true);
    }

    @PostMapping("/invite")
    @ApiOperation("邀请成员加入组织")
    public BaseResponse<Boolean> inviteMember(@RequestBody OrganizationInviteMemberRequest inviteRequest, HttpServletRequest request) {
        if (inviteRequest == null) throw new BusinessException(ErrorCode.PARAMS_ERROR);
        User currentUser = userService.getLoginUser(request);
        organizationService.inviteMember(inviteRequest, currentUser);
        return ResultUtils.success(true);
    }

    @PostMapping("/removeMember")
    @ApiOperation("将成员移出组织")
    public BaseResponse<Boolean> removeMember(@RequestBody OrganizationRemoveMemberRequest removeRequest, HttpServletRequest request) {
        if (removeRequest == null) throw new BusinessException(ErrorCode.PARAMS_ERROR);
        User currentUser = userService.getLoginUser(request);
        organizationService.removeMember(removeRequest, currentUser);
        return ResultUtils.success(true);
    }

    @PostMapping("/listMembers")
    @ApiOperation("获取组织成员列表")
    public BaseResponse<List<UserVO>> listMembers(@RequestBody OrganizationListMembersRequest listRequest, HttpServletRequest request) {
        if (listRequest == null || listRequest.getOrganizationId() == null) throw new BusinessException(ErrorCode.PARAMS_ERROR);
        User currentUser = userService.getLoginUser(request);
        List<UserVO> userVOList = organizationService.listMembers(listRequest, currentUser);
        return ResultUtils.success(userVOList);
    }
}