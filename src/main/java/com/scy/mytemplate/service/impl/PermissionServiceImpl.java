// FILE: src/main/java/com/scy/mytemplate/service/impl/PermissionServiceImpl.java
package com.scy.mytemplate.service.impl;

import com.scy.mytemplate.common.ErrorCode;
import com.scy.mytemplate.exception.BusinessException;
import com.scy.mytemplate.mapper.FolderMapper;
import com.scy.mytemplate.mapper.ImageMapper;
import com.scy.mytemplate.mapper.OrganizationMemberMapper;
import com.scy.mytemplate.model.entity.Folder;
import com.scy.mytemplate.model.entity.Image;
import com.scy.mytemplate.model.entity.User;
import com.scy.mytemplate.model.enums.PermissionEnum;
import com.scy.mytemplate.model.enums.UserRoleEnum;
import com.scy.mytemplate.service.PermissionService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * 权限校验服务实现。
 * 封装了系统中所有关于文件夹和节点访问权限的判断逻辑。
 *
 * @author Bedrock
 */
@Service
public class PermissionServiceImpl implements PermissionService {

    @Resource
    private ImageMapper imageMapper;
    @Resource
    private FolderMapper folderMapper;
    @Resource
    private OrganizationMemberMapper memberMapper;

    @Override
    public void checkNodePermission(String nodeName, User user, PermissionEnum requiredPermission) {
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        // 平台管理员拥有所有权限，直接放行
        if (UserRoleEnum.ADMIN.getValue().equals(user.getUserRole())) {
            return;
        }

        Image image = imageMapper.findByStoragePath(nodeName);
        if (image == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "资源不存在或已被删除: " + nodeName);
        }
        Folder folder = folderMapper.selectById(image.getFolderId());
        if (folder == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "资源所属的文件夹不存在");
        }
        checkFolderPermission(folder, user, requiredPermission);
    }

    @Override
    public void checkFolderPermission(Folder folder, User user, PermissionEnum requiredPermission) {
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        // 平台管理员拥有所有权限，直接放行
        if (UserRoleEnum.ADMIN.getValue().equals(user.getUserRole())) {
            return;
        }

        switch (folder.getSpace()) {
            case "public":
                // 公共空间：所有人可读，但只有平台管理员可写（已在上一层处理）
                if (requiredPermission == PermissionEnum.WRITE) {
                    throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权修改公共空间内容");
                }
                break;
            case "organization":
                String orgId = folder.getOwnerOrganizationId();
                String roleInOrg = memberMapper.findUserRoleInOrg(user.getId(), orgId);
                if (roleInOrg == null) {
                    throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "您不属于该组织，无权访问");
                }
                // 组织空间：组织成员可读，但只有组织管理员可写
                if (requiredPermission == PermissionEnum.WRITE && !"admin".equals(roleInOrg)) {
                    throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "您不是组织管理员，无权修改此内容");
                }
                break;
            case "private":
                // 私人空间：只有文件夹拥有者有全部权限
                if (!folder.getOwnerUserId().equals(user.getId())) {
                    throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权访问他人的私人空间");
                }
                break;
            default:
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "未知的空间类型，权限检查失败");
        }
    }
}
// END OF FILE: src/main/java/com/scy/mytemplate/service/impl/PermissionServiceImpl.java