// FILE: src/main/java/com/scy/mytemplate/service/UserService.java
package com.scy.mytemplate.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.scy.mytemplate.model.dto.user.UserQueryRequest;
import com.scy.mytemplate.model.entity.User;
import com.scy.mytemplate.model.vo.LoginUserVO;
import com.scy.mytemplate.model.vo.UserVO;
import me.chanjar.weixin.common.bean.WxOAuth2UserInfo;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 用户服务接口。
 * 定义了用户相关的核心业务逻辑，包括注册、登录、信息获取和权限判断。
 *
 * @author Bedrock
 */
public interface UserService extends IService<User> {

    /**
     * 用户注册。
     *
     * @param userAccount   用户账户
     * @param userPassword  用户密码
     * @param checkPassword 校验密码
     * @return 新创建用户的 ID
     */
    String userRegister(String userAccount, String userPassword, String checkPassword);

    /**
     * 用户登录。
     * 成功后将返回一个 LoginUserVO，其中包含了用户的基本信息以及其所属的所有组织列表。
     *
     * @param userAccount  用户账户
     * @param userPassword 用户密码
     * @param request      HTTP 请求对象，用于管理 Session。
     * @return 脱敏后的用户信息，包含其组织列表。
     */
    LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request);

    /**
     * 用户通过微信开放平台登录。
     *
     * @param wxOAuth2UserInfo 从微信获取的用户信息
     * @param request          HTTP 请求对象
     * @return 脱敏后的用户信息，包含其组织列表。
     */
    LoginUserVO userLoginByMpOpen(WxOAuth2UserInfo wxOAuth2UserInfo, HttpServletRequest request);

    /**
     * 获取当前登录用户实体。
     * 如果用户未登录，将抛出业务异常。
     *
     * @param request HTTP 请求对象
     * @return 当前登录用户的完整实体信息。
     */
    User getLoginUser(HttpServletRequest request);

    /**
     * 获取当前登录用户实体（允许为空）。
     * 如果用户未登录，将返回 null，不会抛出异常。
     *
     * @param request HTTP 请求对象
     * @return 当前登录用户的实体信息，或 null。
     */
    User getLoginUserPermitNull(HttpServletRequest request);

    /**
     * 判断当前登录用户是否为平台管理员。
     *
     * @param request HTTP 请求对象
     * @return 如果是平台管理员则返回 true，否则返回 false。
     */
    boolean isAdmin(HttpServletRequest request);

    /**
     * 判断指定用户实体是否为平台管理员。
     *
     * @param user 用户实体
     * @return 如果是平台管理员则返回 true，否则返回 false。
     */
    boolean isAdmin(User user);

    /**
     * 用户注销。
     *
     * @param request HTTP 请求对象
     * @return 注销成功返回 true。
     */
    boolean userLogout(HttpServletRequest request);

    /**
     * 获取脱敏的、包含完整组织信息的已登录用户信息视图对象。
     *
     * @param user 用户实体
     * @return LoginUserVO 对象，包含了用户的多组织身份信息。
     */
    LoginUserVO getLoginUserVO(User user);

    /**
     * 获取脱敏的通用用户信息视图对象。
     *
     * @param user 用户实体
     * @return UserVO 对象，仅包含公开信息。
     */
    UserVO getUserVO(User user);

    /**
     * 批量获取脱敏的通用用户信息视图对象。
     *
     * @param userList 用户实体列表
     * @return UserVO 列表。
     */
    List<UserVO> getUserVO(List<User> userList);

    /**
     * 根据查询请求构建 MyBatis-Plus 的 QueryWrapper。
     *
     * @param userQueryRequest 用户查询请求 DTO
     * @return 封装了查询条件的 QueryWrapper<User> 对象。
     */
    QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest);

}
// END OF FILE: src/main/java/com/scy/mytemplate/service/UserService.java