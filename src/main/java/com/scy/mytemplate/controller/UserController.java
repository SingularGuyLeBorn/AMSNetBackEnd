// FILE: src/main/java/com/scy/mytemplate/controller/UserController.java
package com.scy.mytemplate.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.scy.mytemplate.common.BaseResponse;
import com.scy.mytemplate.common.DeleteRequest;
import com.scy.mytemplate.common.ErrorCode;
import com.scy.mytemplate.common.ResultUtils;
import com.scy.mytemplate.config.WxOpenConfig;
import com.scy.mytemplate.constant.UserConstant;
import com.scy.mytemplate.model.entity.User;
import com.scy.mytemplate.model.vo.LoginUserVO;
import com.scy.mytemplate.model.vo.UserVO;
import com.scy.mytemplate.service.UserService;
import com.scy.mytemplate.annotation.AuthCheck;
import com.scy.mytemplate.exception.BusinessException;
import com.scy.mytemplate.exception.ThrowUtils;
import com.scy.mytemplate.model.dto.user.UserAddRequest;
import com.scy.mytemplate.model.dto.user.UserLoginRequest;
import com.scy.mytemplate.model.dto.user.UserQueryRequest;
import com.scy.mytemplate.model.dto.user.UserRegisterRequest;
import com.scy.mytemplate.model.dto.user.UserUpdateMyRequest;
import com.scy.mytemplate.model.dto.user.UserUpdateRequest;

import java.util.List;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.bean.WxOAuth2UserInfo;
import me.chanjar.weixin.common.bean.oauth2.WxOAuth2AccessToken;
import me.chanjar.weixin.mp.api.WxMpService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户接口 (重构后)。
 * 负责处理所有与用户相关的 HTTP 请求，包括注册、登录、注销、以及管理员对用户的管理操作。
 *
 * @author Bedrock
 */
@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {

    @Resource
    private UserService userService;

    @Resource
    private WxOpenConfig wxOpenConfig;

    // region 登录与会话管理 (Login & Session Management)

    /**
     * 用户注册接口。
     *
     * @param userRegisterRequest 包含账号、密码和校验密码的请求体。
     * @return 包含新用户ID的响应。
     */
    @PostMapping("/register")
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest) {
        if (userRegisterRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数不完整");
        }
        long result = userService.userRegister(userAccount, userPassword, checkPassword);
        return ResultUtils.success(result);
    }

    /**
     * 用户登录接口。
     *
     * @param userLoginRequest 包含账号和密码的请求体。
     * @param request          HTTP请求，用于设置Session。
     * @return 包含用户详细信息（包括组织列表）的 LoginUserVO 响应。
     */
    @PostMapping("/login")
    public BaseResponse<LoginUserVO> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request) {
        if (userLoginRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        LoginUserVO loginUserVO = userService.userLogin(userAccount, userPassword, request);
        return ResultUtils.success(loginUserVO);
    }

    /**
     * 通过微信开放平台进行用户登录。
     *
     * @param request  HTTP请求
     * @param response HTTP响应
     * @param code     微信授权码
     * @return 包含用户详细信息的 LoginUserVO 响应。
     */
    @GetMapping("/login/wx_open")
    public BaseResponse<LoginUserVO> userLoginByWxOpen(HttpServletRequest request, HttpServletResponse response,
                                                       @RequestParam("code") String code) {
        try {
            WxMpService wxService = wxOpenConfig.getWxMpService();
            WxOAuth2AccessToken accessToken = wxService.getOAuth2Service().getAccessToken(code);
            WxOAuth2UserInfo userInfo = wxService.getOAuth2Service().getUserInfo(accessToken, code);
            String unionId = userInfo.getUnionId();
            if (StringUtils.isBlank(unionId)) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "微信登录失败，无法获取 UnionID");
            }
            return ResultUtils.success(userService.userLoginByMpOpen(userInfo, request));
        } catch (Exception e) {
            log.error("userLoginByWxOpen error", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "微信登录失败，系统错误");
        }
    }

    /**
     * 用户注销接口。
     *
     * @param request HTTP请求，用于清除Session。
     * @return 包含操作成功布尔值的响应。
     */
    @PostMapping("/logout")
    public BaseResponse<Boolean> userLogout(HttpServletRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean result = userService.userLogout(request);
        return ResultUtils.success(result);
    }

    /**
     * 获取当前登录用户信息接口。
     *
     * @param request HTTP请求，用于获取Session中的用户。
     * @return 包含当前用户详细信息的 LoginUserVO 响应。
     */
    @GetMapping("/get/login")
    public BaseResponse<LoginUserVO> getLoginUser(HttpServletRequest request) {
        User user = userService.getLoginUser(request);
        return ResultUtils.success(userService.getLoginUserVO(user));
    }

    // endregion

    // region 管理员用 - 用户管理 (Admin Only - User Management)

    /**
     * (管理员) 创建新用户。
     *
     * @param userAddRequest 包含新用户信息（除密码外）的请求体。
     * @return 包含新用户ID的响应。
     */
    @PostMapping("/add")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Long> addUser(@RequestBody UserAddRequest userAddRequest) {
        if (userAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = new User();
        BeanUtils.copyProperties(userAddRequest, user);
        boolean result = userService.save(user);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(user.getId());
    }

    /**
     * (管理员) 删除用户。
     *
     * @param deleteRequest 包含要删除用户ID的请求体。
     * @return 包含操作成功布尔值的响应。
     */
    @PostMapping("/delete")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> deleteUser(@RequestBody DeleteRequest deleteRequest) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 基石建议: 直接物理删除用户是危险操作，可能因外键约束失败。
        // 生产环境中，应在 Service 层实现一个更安全的业务删除方法（如先处理关联数据）。
        boolean b = userService.removeById(deleteRequest.getId());
        return ResultUtils.success(b);
    }

    /**
     * (管理员) 更新用户信息。
     *
     * @param userUpdateRequest 包含用户ID和要更新字段的请求体。
     * @return 包含操作成功布尔值的响应。
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateUser(@RequestBody UserUpdateRequest userUpdateRequest) {
        if (userUpdateRequest == null || userUpdateRequest.getId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = new User();
        BeanUtils.copyProperties(userUpdateRequest, user);
        boolean result = userService.updateById(user);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * (管理员) 根据ID获取用户公开信息。
     *
     * @param id 用户ID。
     * @return 包含用户公开信息的 UserVO 响应。
     */
    @GetMapping("/get/vo")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<UserVO> getUserVOById(long id) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getById(id);
        ThrowUtils.throwIf(user == null, ErrorCode.NOT_FOUND_ERROR);
        return ResultUtils.success(userService.getUserVO(user));
    }

    /**
     * (管理员) 分页获取用户封装列表。
     *
     * @param userQueryRequest 包含分页和筛选条件的请求体。
     * @return 分页的用户信息列表 (UserVO)。
     */
    @PostMapping("/list/page/vo")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<UserVO>> listUserVOByPage(@RequestBody UserQueryRequest userQueryRequest) {
        if (userQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        long current = userQueryRequest.getCurrent();
        long size = userQueryRequest.getPageSize();
        // 防爬虫保护
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<User> userPage = userService.page(new Page<>(current, size),
                userService.getQueryWrapper(userQueryRequest));
        Page<UserVO> userVOPage = new Page<>(current, size, userPage.getTotal());
        List<UserVO> userVO = userService.getUserVO(userPage.getRecords());
        userVOPage.setRecords(userVO);
        return ResultUtils.success(userVOPage);
    }

    // endregion

    /**
     * 用户更新自己的个人信息。
     *
     * @param userUpdateMyRequest 包含要更新的个人信息的请求体。
     * @param request             HTTP请求，用于获取当前登录用户。
     * @return 包含操作成功布尔值的响应。
     */
    @PostMapping("/update/my")
    public BaseResponse<Boolean> updateMyUser(@RequestBody UserUpdateMyRequest userUpdateMyRequest,
                                              HttpServletRequest request) {
        if (userUpdateMyRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        User user = new User();
        BeanUtils.copyProperties(userUpdateMyRequest, user);
        user.setId(loginUser.getId());
        boolean result = userService.updateById(user);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }
}
// END OF FILE: src/main/java/com/scy/mytemplate/controller/UserController.java