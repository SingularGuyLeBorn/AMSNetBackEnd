// FILE: src/main/java/com/scy/mytemplate/service/PermissionService.java
package com.scy.mytemplate.service;

import com.scy.mytemplate.model.entity.Folder;
import com.scy.mytemplate.model.entity.User;
import com.scy.mytemplate.model.enums.PermissionEnum;

/**
 * 权限校验服务接口。
 * 这是系统中所有权限判断的唯一真理来源，旨在将权限逻辑与业务逻辑解耦。
 *
 * @author Bedrock
 */
public interface PermissionService {

    /**
     * 根据节点名称（关联的图片路径）检查当前用户的权限。
     *
     * @param nodeName           节点的唯一名称 (即 Image.storagePath)。
     * @param user               当前操作的用户对象。
     * @param requiredPermission 所需的权限 (READ 或 WRITE)。
     * @throws com.scy.mytemplate.exception.BusinessException 如果用户无权执行操作。
     */
    void checkNodePermission(String nodeName, User user, PermissionEnum requiredPermission);

    /**
     * 根据文件夹实体检查当前用户的权限。
     *
     * @param folder             目标文件夹实体。
     * @param user               当前操作的用户对象。
     * @param requiredPermission 所需的权限 (READ 或 WRITE)。
     * @throws com.scy.mytemplate.exception.BusinessException 如果用户无权执行操作。
     */
    void checkFolderPermission(Folder folder, User user, PermissionEnum requiredPermission);

    /**
     * 检查用户是否是指定组织的管理员。
     *
     * @param organizationId 组织ID
     * @param user           当前用户
     * @throws com.scy.mytemplate.exception.BusinessException 如果用户不是该组织的管理员。
     */
    void checkOrganizationAdmin(String organizationId, User user);
}
// END OF FILE: src/main/java/com/scy/mytemplate/service/PermissionService.java