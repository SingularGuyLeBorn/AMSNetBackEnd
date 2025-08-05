// FILE: src/main/java/com/scy/mytemplate/model/vo/LoginUserVO.java
package com.scy.mytemplate.model.vo;

import lombok.Data;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 登录用户信息视图对象 (Login User View Object)。
 * 当用户成功登录后，后端将返回此对象。
 * 它包含了前端构建用户初始界面所需的所有核心信息，特别是用户的多组织身份。
 *
 * @author Bedrock
 */
@Data
public class LoginUserVO implements Serializable {

    /**
     * 用户ID
     */
    private Long id;

    /**
     * 用户昵称
     */
    private String userName;

    /**
     * 用户头像 URL
     */
    private String userAvatar;

    /**
     * 用户简介
     */
    private String userProfile;

    /**
     * 用户的全局角色: 'user', 'admin', 'ban'
     */
    private String userRole;

    /**
     * 用户所属的所有组织列表。
     * 前端可以根据这个列表渲染组织切换器、展示不同的工作区等。
     * 这是一个 `UserOrganizationInfoVO` 对象的列表，每一项都代表用户在一个组织中的“名片”。
     */
    private List<UserOrganizationInfoVO> organizations;

    /**
     * 账户创建时间
     */
    private Date createTime;

    /**
     * 账户最后更新时间
     */
    private Date updateTime;

    private static final long serialVersionUID = 1L;
}
// END OF FILE: src/main/java/com/scy/mytemplate/model/vo/LoginUserVO.java