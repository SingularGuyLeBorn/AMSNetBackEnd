// FILE: src/main/java/com/scy/mytemplate/model/vo/UserVO.java
package com.scy.mytemplate.model.vo;

import lombok.Data;
import java.io.Serializable;
import java.util.Date;

/**
 * 通用用户视图对象 (User View Object)。
 *
 * <strong>设计原则：</strong> 此对象遵循“最小权限暴露”安全原则。
 * 它专门用于向其他用户展示一个用户的公开、脱敏信息（例如，在成员列表或内容创建者信息中）。
 *
 * <strong>安全考量：</strong> 此对象 **绝对不能** 包含任何敏感的权限信息，
 * 例如 `userRole` 或 `userPassword`。暴露用户的全局角色 (`admin`) 会带来不必要的安全风险。
 * 需要当前用户完整权限信息的场景，请使用 `LoginUserVO`。
 *
 * @author Bedrock
 */
@Data
public class UserVO implements Serializable {

    /**
     * 用户ID
     */
    private String id;

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
     * 账户创建时间
     */
    private Date createTime;

    private static final long serialVersionUID = 1L;
}
// END OF FILE: src/main/java/com/scy/mytemplate/model/vo/UserVO.java