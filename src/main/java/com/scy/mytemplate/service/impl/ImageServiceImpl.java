// FILE: src/main/java/com/scy/mytemplate/service/impl/ImageServiceImpl.java
package com.scy.mytemplate.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.scy.mytemplate.common.ErrorCode;
import com.scy.mytemplate.exception.BusinessException;
import com.scy.mytemplate.mapper.FolderMapper;
import com.scy.mytemplate.mapper.ImageMapper;
import com.scy.mytemplate.model.dto.node.NodeCreateRequest;
import com.scy.mytemplate.model.dto.node.NodeDeleteRequest;
import com.scy.mytemplate.model.entity.Folder;
import com.scy.mytemplate.model.entity.Image;
import com.scy.mytemplate.model.entity.User;
import com.scy.mytemplate.model.enums.PermissionEnum;
import com.scy.mytemplate.service.GraphService;
import com.scy.mytemplate.service.ImageService;
import com.scy.mytemplate.service.PermissionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.UUID;

/**
 * 图片服务实现。
 *
 * @author Bedrock
 */
@Service
@Slf4j
public class ImageServiceImpl extends ServiceImpl<ImageMapper, Image> implements ImageService {

    @Resource
    private FolderMapper folderMapper;

    @Resource
    private PermissionService permissionService;

    @Resource
    private GraphService graphService;

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Image uploadImage(Long folderId, MultipartFile file, User currentUser) {
        if (file.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "上传的文件不能为空");
        }
        Folder folder = folderMapper.selectById(folderId);
        if (folder == null || folder.getIsDelete() == 1) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "目标文件夹不存在");
        }

        permissionService.checkFolderPermission(folder, currentUser, PermissionEnum.WRITE);

        String originalFilename = file.getOriginalFilename();
        String uniqueFilename = UUID.randomUUID().toString() + "-" + originalFilename;

        String relativePath;
        switch (folder.getSpace()) {
            case "organization":
                relativePath = Paths.get("organization", folder.getOwnerOrganizationId().toString(), uniqueFilename).toString();
                break;
            case "private":
                relativePath = Paths.get("user", folder.getOwnerUserId().toString(), uniqueFilename).toString();
                break;
            default:
                relativePath = Paths.get("public", uniqueFilename).toString();
                break;
        }
        String storagePath = relativePath.replace("\\", "/");
        File destFile = new File(uploadDir + File.separator + storagePath);

        try {
            Files.createDirectories(destFile.getParentFile().toPath());
            file.transferTo(destFile);
        } catch (IOException e) {
            log.error("文件保存失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "文件保存失败");
        }

        Image image = new Image();
        try {
            image.setFolderId(folderId);
            image.setOriginalFilename(originalFilename);
            image.setStoragePath(storagePath);
            image.setUploaderId(currentUser.getId());
            image.setFileSize(file.getSize());
            BufferedImage bufferedImage = ImageIO.read(destFile);
            if (bufferedImage != null) {
                image.setWidth(bufferedImage.getWidth());
                image.setHeight(bufferedImage.getHeight());
            }
            this.save(image);
        } catch (Exception e) {
            rollbackFile(destFile);
            log.error("图片元数据入库失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "图片信息入库失败");
        }

        try {
            NodeCreateRequest nodeCreateRequest = new NodeCreateRequest();
            nodeCreateRequest.setName(storagePath);
            nodeCreateRequest.setProperties(new HashMap<>());
            graphService.createNode(nodeCreateRequest, currentUser);
        } catch (Exception e) {
            rollbackFile(destFile);
            log.error("创建 Neo4j 节点失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "创建知识图谱节点失败");
        }

        return image;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteImage(Long imageId, User currentUser) {
        Image image = this.getById(imageId);
        if (image == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "图片不存在");
        }
        // 权限检查：用户必须对图片所在的文件夹有写权限才能删除图片
        permissionService.checkNodePermission(image.getStoragePath(), currentUser, PermissionEnum.WRITE);

        // 1. 逻辑删除 MySQL 中的图片记录
        boolean success = this.removeById(imageId);
        if (!success) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "删除图片数据库记录失败");
        }

        // 2. 删除 Neo4j 中的对应节点
        try {
            NodeDeleteRequest nodeDeleteRequest = new NodeDeleteRequest();
            nodeDeleteRequest.setName(image.getStoragePath());
            graphService.deleteNode(nodeDeleteRequest, currentUser);
        } catch (Exception e) {
            // 抛出异常以触发事务回滚
            log.error("删除 Neo4j 节点失败，将回滚操作", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "删除知识图谱节点失败");
        }

        // 注意：物理文件并未删除，以支持未来的恢复功能。
        // 如果需要物理删除，可以在此处添加删除 destFile 的逻辑。
    }

    private void rollbackFile(File file) {
        try {
            Files.deleteIfExists(file.toPath());
        } catch (IOException rollbackEx) {
            log.error("回滚物理文件删除失败: " + file.getPath(), rollbackEx);
        }
    }
}
// END OF FILE: src/main/java/com/scy/mytemplate/service/impl/ImageServiceImpl.java