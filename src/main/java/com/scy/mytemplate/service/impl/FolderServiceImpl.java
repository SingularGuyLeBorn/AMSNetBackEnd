// FILE: src/main/java/com/scy/mytemplate/service/impl/FolderServiceImpl.java
package com.scy.mytemplate.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.scy.mytemplate.common.ErrorCode;
import com.scy.mytemplate.exception.BusinessException;
import com.scy.mytemplate.mapper.FolderMapper;
import com.scy.mytemplate.model.dto.folder.FolderCreateRequest;
import com.scy.mytemplate.model.dto.folder.FolderUpdateRequest;
import com.scy.mytemplate.model.entity.Folder;
import com.scy.mytemplate.model.entity.User;
import com.scy.mytemplate.model.enums.PermissionEnum;
import com.scy.mytemplate.model.enums.UserRoleEnum;
import com.scy.mytemplate.model.vo.FolderVO;
import com.scy.mytemplate.service.FolderService;
import com.scy.mytemplate.service.PermissionService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 文件夹服务实现。
 *
 * @author Bedrock
 */
@Service
public class FolderServiceImpl extends ServiceImpl<FolderMapper, Folder> implements FolderService {

    @Resource
    private PermissionService permissionService;

    @Resource
    private FolderMapper folderMapper;

    @Override
    public FolderVO createFolder(FolderCreateRequest request, User currentUser) {
        if (StringUtils.isBlank(request.getName()) || StringUtils.isBlank(request.getSpace())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件夹名称或空间类型不能为空");
        }

        Folder folder = new Folder();
        folder.setName(request.getName());
        folder.setSpace(request.getSpace());

        switch (request.getSpace()) {
            case "private":
                folder.setOwnerUserId(currentUser.getId());
                break;
            case "organization":
                if (request.getOwnerOrganizationId() == null) {
                    throw new BusinessException(ErrorCode.PARAMS_ERROR, "创建组织文件夹时必须指定组织ID");
                }
                // 校验用户是否有权在该组织下创建文件夹 (必须是组织管理员)
                permissionService.checkFolderPermission(new Folder() {{ setOwnerOrganizationId(request.getOwnerOrganizationId()); setSpace("organization"); }}, currentUser, PermissionEnum.WRITE);
                folder.setOwnerOrganizationId(request.getOwnerOrganizationId());
                break;
            case "public":
                // 只有平台管理员才能创建公共文件夹
                if (!UserRoleEnum.ADMIN.getValue().equals(currentUser.getUserRole())) {
                    throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "只有平台管理员才能创建公共文件夹");
                }
                break;
            default:
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "无效的空间类型");
        }

        boolean success = this.save(folder);
        if (!success) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "创建文件夹失败，数据库错误");
        }
        return FolderVO.fromEntity(folder);
    }

    @Override
    public void deleteFolder(Long folderId, User currentUser) {
        Folder folder = this.getById(folderId);
        if (folder == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "文件夹不存在");
        }
        permissionService.checkFolderPermission(folder, currentUser, PermissionEnum.WRITE);
        // 使用 MyBatis-Plus 的逻辑删除
        boolean success = this.removeById(folderId);
        if (!success) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "删除文件夹失败，数据库错误");
        }
    }

    @Override
    public FolderVO updateFolder(FolderUpdateRequest request, User currentUser) {
        if (StringUtils.isBlank(request.getNewName())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "新文件夹名称不能为空");
        }
        Folder folder = this.getById(request.getFolderId());
        if (folder == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "文件夹不存在");
        }
        permissionService.checkFolderPermission(folder, currentUser, PermissionEnum.WRITE);

        folder.setName(request.getNewName());
        boolean success = this.updateById(folder);
        if (!success) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "更新文件夹失败，数据库错误");
        }
        return FolderVO.fromEntity(folder);
    }

    @Override
    public List<FolderVO> listFoldersForCurrentUser(User currentUser) {
        List<Folder> folders = folderMapper.findVisibleFoldersForUser(currentUser.getId());
        return folders.stream()
                .map(FolderVO::fromEntity)
                .collect(Collectors.toList());
    }
}
// END OF FILE: src/main/java/com/scy/mytemplate/service/impl/FolderServiceImpl.java