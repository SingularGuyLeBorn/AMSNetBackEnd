
// FILE: src/main/java/com/scy/mytemplate/service/impl/FolderServiceImpl.java
package com.scy.mytemplate.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.scy.mytemplate.common.ErrorCode;
import com.scy.mytemplate.exception.BusinessException;
import com.scy.mytemplate.mapper.FolderMapper;
import com.scy.mytemplate.mapper.UserMapper;
import com.scy.mytemplate.model.dto.folder.*;
import com.scy.mytemplate.model.dto.image.ImageBatchDeleteRequest;
import com.scy.mytemplate.model.dto.node.NodeCreateRequest;
import com.scy.mytemplate.model.dto.node.NodeQueryRequest;
import com.scy.mytemplate.model.entity.Annotation;
import com.scy.mytemplate.model.entity.Folder;
import com.scy.mytemplate.model.entity.Image;
import com.scy.mytemplate.model.entity.User;
import com.scy.mytemplate.model.enums.PermissionEnum;
import com.scy.mytemplate.model.vo.FolderVO;
import com.scy.mytemplate.model.vo.NodeVO;
import com.scy.mytemplate.model.vo.UserVO;
import com.scy.mytemplate.service.*;
        import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 文件夹服务实现。
 *
 * @author Bedrock
 */
@Service
@Slf4j
public class FolderServiceImpl extends ServiceImpl<FolderMapper, Folder> implements FolderService {

    @Resource
    private PermissionService permissionService;
    @Resource
    private FolderMapper folderMapper;
    @Resource
    private ImageService imageService;
    @Resource
    private AnnotationService annotationService;
    @Resource
    private GraphService graphService;
    @Resource
    private UserService userService;
    @Resource
    private UserMapper userMapper;

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Override
    @Transactional
    public FolderVO createFolder(FolderCreateRequest request, User currentUser) {
        if (StringUtils.isBlank(request.getName()) || StringUtils.isBlank(request.getSpace())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件夹名称或空间类型不能为空");
        }

        Folder folder = new Folder();
        folder.setName(request.getName());
        folder.setSpace(request.getSpace());

        switch (request.getSpace()) {
            case "user_private":
            case "user_public":
                folder.setOwnerUserId(currentUser.getId());
                break;
            case "organization_public":
                if (StringUtils.isBlank(request.getOwnerOrganizationId())) {
                    throw new BusinessException(ErrorCode.PARAMS_ERROR, "创建组织文件夹时必须指定组织ID");
                }
                permissionService.checkOrganizationAdmin(request.getOwnerOrganizationId(), currentUser);
                folder.setOwnerOrganizationId(request.getOwnerOrganizationId());
                break;
            case "platform_public":
                permissionService.checkFolderPermission(new Folder(), currentUser, PermissionEnum.WRITE);
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
    @Transactional
    public void deleteFolder(String folderId, User currentUser) {
        Folder folder = this.getById(folderId);
        if (folder == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "文件夹不存在");
        }
        permissionService.checkFolderPermission(folder, currentUser, PermissionEnum.WRITE);

        List<Image> images = imageService.list(new QueryWrapper<Image>().eq("folderId", folderId));
        if (!images.isEmpty()) {
            List<String> imageIds = images.stream().map(Image::getId).collect(Collectors.toList());
            ImageBatchDeleteRequest deleteRequest = new ImageBatchDeleteRequest();
            deleteRequest.setIds(imageIds);
            imageService.deleteImagesBatch(deleteRequest, currentUser);
        }

        boolean success = this.removeById(folderId);
        if (!success) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "删除文件夹失败，数据库错误");
        }
    }

    @Override
    @Transactional
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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public FolderVO copyFolder(FolderCopyRequest request, User currentUser) {
        // 1. 权限校验
        Folder sourceFolder = this.getById(request.getSourceFolderId());
        if (sourceFolder == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "源文件夹不存在");
        }
        permissionService.checkFolderPermission(sourceFolder, currentUser, PermissionEnum.READ);
        if (!"user_private".equals(request.getTargetSpace()) && !"user_public".equals(request.getTargetSpace())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "只能复制到个人私有或个人公共空间");
        }

        // 2. 创建新文件夹记录
        Folder newFolder = new Folder();
        newFolder.setName(sourceFolder.getName() + "_copy_" + System.currentTimeMillis() % 1000);
        newFolder.setSpace(request.getTargetSpace());
        newFolder.setOwnerUserId(currentUser.getId());
        this.save(newFolder);

        // 3. 深度复制内容
        List<Image> sourceImages = imageService.list(new QueryWrapper<Image>().eq("folderId", sourceFolder.getId()));
        for (Image sourceImage : sourceImages) {
            try {
                deepCopyImageEntry(sourceImage, newFolder, currentUser);
            } catch (IOException e) {
                log.error("复制文件时发生IO错误, source: {}", sourceImage.getStoragePath(), e);
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "复制文件失败，请检查服务器磁盘空间和权限。");
            }
        }
        return FolderVO.fromEntity(newFolder);
    }

    private void deepCopyImageEntry(Image sourceImage, Folder targetFolder, User currentUser) throws IOException {
        // 1. 物理文件复制
        Path sourcePath = Paths.get(uploadDir, sourceImage.getStoragePath());
        if (!Files.exists(sourcePath)) {
            log.warn("源文件不存在，跳过复制: {}", sourcePath);
            return;
        }

        String newUniqueSuffix = UUID.randomUUID().toString().substring(0, 8) + "-" + sourceImage.getOriginalFilename();
        Path relativePath = Paths.get("user", currentUser.getId(), newUniqueSuffix);
        String newStoragePath = relativePath.toString().replace("\\", "/");
        Path destPath = Paths.get(uploadDir, newStoragePath);
        Files.createDirectories(destPath.getParent());
        Files.copy(sourcePath, destPath);

        // 2. 创建新的 Image 数据库记录
        Image newImage = new Image();
        newImage.setFolderId(targetFolder.getId());
        newImage.setOriginalFilename(sourceImage.getOriginalFilename());
        newImage.setStoragePath(newStoragePath);
        newImage.setWidth(sourceImage.getWidth());
        newImage.setHeight(sourceImage.getHeight());
        newImage.setFileSize(sourceImage.getFileSize());
        newImage.setUploaderId(currentUser.getId());
        imageService.save(newImage);

        // 3. 复制标注信息
        Annotation sourceAnnotation = annotationService.getOne(new QueryWrapper<Annotation>().eq("imageId", sourceImage.getId()));
        if(sourceAnnotation != null) {
            Annotation newAnnotation = new Annotation();
            newAnnotation.setImageId(newImage.getId());
            newAnnotation.setJsonContent(sourceAnnotation.getJsonContent());
            newAnnotation.setLastEditorId(currentUser.getId());
            annotationService.save(newAnnotation);
        }

        // 4. 创建新的知识图谱节点
        NodeCreateRequest nodeCreateRequest = new NodeCreateRequest();
        nodeCreateRequest.setName(newStoragePath);
        Map<String, Object> properties = new HashMap<>();
        properties.put("space", targetFolder.getSpace());
        properties.put("ownerUserId", currentUser.getId());
        // 复制源节点的非权限属性
        NodeVO sourceNode = graphService.findNode(new NodeQueryRequest() {{ setName(sourceImage.getStoragePath()); }}, currentUser);
        if (sourceNode != null && sourceNode.getProperties() != null) {
            sourceNode.getProperties().forEach((key, value) -> {
                if (!List.of("space", "ownerUserId", "ownerOrganizationId").contains(key)) {
                    properties.put(key, value);
                }
            });
        }
        nodeCreateRequest.setProperties(properties);
        graphService.createNode(nodeCreateRequest, currentUser);
    }

    @Override
    @Transactional
    public void requestMerge(FolderMergeRequest request, User currentUser) {
        Folder folder = this.getById(request.getFolderId());
        if (folder == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "文件夹不存在");
        }
        permissionService.checkFolderPermission(folder, currentUser, PermissionEnum.WRITE);
        if (!"user_public".equals(folder.getSpace())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "只能从个人公共空间发起合并请求");
        }

        folder.setMergeState("PENDING");
        folder.setTargetOrganizationId(request.getTargetOrganizationId());
        this.updateById(folder);
    }

    @Override
    public List<FolderMergeResponse> listPendingMerges(String organizationId, User currentUser) {
        permissionService.checkOrganizationAdmin(organizationId, currentUser);
        QueryWrapper<Folder> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("targetOrganizationId", organizationId).eq("mergeState", "PENDING");
        List<Folder> folders = this.list(queryWrapper);

        return folders.stream().map(folder -> {
            FolderMergeResponse response = new FolderMergeResponse();
            response.setFolder(FolderVO.fromEntity(folder));
            User requestUser = userMapper.selectById(folder.getOwnerUserId());
            response.setRequestUser(userService.getUserVO(requestUser));
            return response;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void approveMergeRequest(String folderId, User currentUser) {
        Folder folder = this.getById(folderId);
        if (folder == null || !"PENDING".equals(folder.getMergeState())) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "待合并的文件夹不存在或状态不正确");
        }
        permissionService.checkOrganizationAdmin(folder.getTargetOrganizationId(), currentUser);

        folder.setSpace("organization_public");
        folder.setOwnerOrganizationId(folder.getTargetOrganizationId());
        folder.setOwnerUserId(null);
        folder.setMergeState("APPROVED");
        this.updateById(folder);

        List<Image> images = imageService.list(new QueryWrapper<Image>().eq("folderId", folder.getId()));
        for (Image image : images) {
            Map<String, Object> newProps = new HashMap<>();
            newProps.put("space", "organization_public");
            newProps.put("ownerOrganizationId", folder.getOwnerOrganizationId());
            newProps.put("ownerUserId", null); // 使用null来移除属性
            graphService.updateNodePermissions(image.getStoragePath(), newProps);
        }
    }

    @Override
    @Transactional
    public void rejectMergeRequest(String folderId, User currentUser) {
        Folder folder = this.getById(folderId);
        if (folder == null || !"PENDING".equals(folder.getMergeState())) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "待合并的文件夹不存在或状态不正确");
        }
        permissionService.checkOrganizationAdmin(folder.getTargetOrganizationId(), currentUser);

        folder.setMergeState("REJECTED");
        this.updateById(folder);
    }
}
// END OF FILE: src/main/java/com/scy/mytemplate/service/impl/FolderServiceImpl.java
