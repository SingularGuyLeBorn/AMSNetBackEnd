// FILE: src/main/java/com/scy/mytemplate/controller/PlatformAdminController.java
package com.scy.mytemplate.controller;

import com.scy.mytemplate.annotation.AuthCheck;
import com.scy.mytemplate.common.BaseResponse;
import com.scy.mytemplate.common.ErrorCode;
import com.scy.mytemplate.common.ResultUtils;
import com.scy.mytemplate.constant.UserConstant;
import com.scy.mytemplate.exception.BusinessException;
import com.scy.mytemplate.exception.ThrowUtils;
import com.scy.mytemplate.model.dto.organization.OrganizationCreateRequest;
import com.scy.mytemplate.model.dto.organization.OrganizationUpdateRequest;
import com.scy.mytemplate.model.dto.user.UserAddRequest;
import com.scy.mytemplate.model.dto.user.UserUpdateRequest;
import com.scy.mytemplate.model.entity.User;
import com.scy.mytemplate.model.vo.OrganizationVO;
import com.scy.mytemplate.service.OrganizationService;
import com.scy.mytemplate.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * 平台管理员专属接口
 *
 * @author Bedrock
 */
@RestController
@RequestMapping("/admin/platform")
@Slf4j
@Api(tags = "PlatformAdminController")
@AuthCheck(mustRole = UserConstant.ADMIN_ROLE) // 整个 Controller 都需要平台管理员权限
public class PlatformAdminController {

    @Resource
    private UserService userService;

    @Resource
    private OrganizationService organizationService;

    // --- 用户管理 ---

    @PostMapping("/user/add")
    @ApiOperation("平台管理员创建新用户")
    public BaseResponse<String> addUser(@RequestBody UserAddRequest userAddRequest) {
        if (userAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 核心逻辑直接在这里实现，调用已注入的 service
        User user = new User();
        BeanUtils.copyProperties(userAddRequest, user);
        // 通常，新用户的密码需要一个默认值或通过其他方式设置，这里简化处理
        // 实际项目中可能需要更复杂的逻辑，例如生成随机密码并通过邮件发送
        user.setUserPassword("12345678"); // 示例：设置一个默认密码
        boolean result = userService.save(user);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(user.getId());
    }

    @PostMapping("/user/update")
    @ApiOperation("平台管理员更新用户信息")
    public BaseResponse<Boolean> updateUser(@RequestBody UserUpdateRequest userUpdateRequest) {
        if (userUpdateRequest == null || userUpdateRequest.getId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 核心逻辑直接在这里实现
        User user = new User();
        BeanUtils.copyProperties(userUpdateRequest, user);
        boolean result = userService.updateById(user);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    // --- 组织管理 ---

    @PostMapping("/organization/create")
    @ApiOperation("平台管理员创建新组织")
    public BaseResponse<OrganizationVO> createOrganization(@RequestBody OrganizationCreateRequest createRequest, HttpServletRequest request) {
        User currentUser = userService.getLoginUser(request);
        OrganizationVO organizationVO = organizationService.createOrganization(createRequest, currentUser);
        return ResultUtils.success(organizationVO);
    }

    @PostMapping("/organization/update")
    @ApiOperation("平台管理员更新组织信息")
    public BaseResponse<OrganizationVO> updateOrganization(@RequestBody OrganizationUpdateRequest updateRequest, HttpServletRequest request) {
        User currentUser = userService.getLoginUser(request);
        OrganizationVO organizationVO = organizationService.updateOrganization(updateRequest, currentUser);
        return ResultUtils.success(organizationVO);
    }

    @PostMapping("/organization/delete")
    @ApiOperation("平台管理员解散组织")
    public BaseResponse<Boolean> deleteOrganization(@RequestParam String organizationId, HttpServletRequest request) {
        User currentUser = userService.getLoginUser(request);
        organizationService.deleteOrganization(organizationId, currentUser);
        return ResultUtils.success(true);
    }
}
// END OF FILE: src/main/java/com/scy/mytemplate/controller/PlatformAdminController.java