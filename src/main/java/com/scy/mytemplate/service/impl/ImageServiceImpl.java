// FILE: src/main/java/com/scy/mytemplate/service/impl/ImageServiceImpl.java
package com.scy.mytemplate.service.impl;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.scy.mytemplate.common.ErrorCode;
import com.scy.mytemplate.exception.BusinessException;
import com.scy.mytemplate.mapper.FolderMapper;
import com.scy.mytemplate.mapper.ImageMapper;
import com.scy.mytemplate.model.dto.image.*;
import com.scy.mytemplate.model.dto.node.NodeCreateRequest;
import com.scy.mytemplate.model.dto.node.NodeDeleteRequest;
import com.scy.mytemplate.model.entity.Annotation;
import com.scy.mytemplate.model.entity.Folder;
import com.scy.mytemplate.model.entity.Image;
import com.scy.mytemplate.model.entity.User;
import com.scy.mytemplate.model.enums.PermissionEnum;
import com.scy.mytemplate.model.vo.ImageVO;
import com.scy.mytemplate.service.AnnotationService;
import com.scy.mytemplate.service.GraphService;
import com.scy.mytemplate.service.ImageService;
import com.scy.mytemplate.service.PermissionService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import javax.annotation.Resource;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ImageServiceImpl extends ServiceImpl<ImageMapper, Image> implements ImageService {
    @Resource
    private FolderMapper folderMapper;
    @Resource
    private PermissionService permissionService;
    @Resource
    private GraphService graphService;
    @Resource
    private AnnotationService annotationService;

    @Value("${file.upload-dir}")
    private String uploadDir;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ImageVO uploadImage(String folderId, MultipartFile file, User currentUser) {
        Folder folder = folderMapper.selectById(folderId);
        if (folder == null || folder.getIsDelete() == 1) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "目标文件夹不存在");
        }
        return processAndSaveSingleImageEntry(folder, file, null, currentUser);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<ImageVO> uploadImagesWithAnnotationsBatch(String folderId, Map<Integer, String> classMap, List<MultipartFile> files, User currentUser) {
        Folder folder = folderMapper.selectById(folderId);
        if (folder == null || folder.getIsDelete() == 1) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "目标文件夹不存在");
        }
        permissionService.checkFolderPermission(folder, currentUser, PermissionEnum.WRITE);

        Map<String, MultipartFile> imageFiles = new HashMap<>();
        Map<String, MultipartFile> jsonFiles = new HashMap<>();
        for (MultipartFile file : files) {
            String baseName = FilenameUtils.getBaseName(file.getOriginalFilename());
            String extension = FilenameUtils.getExtension(file.getOriginalFilename()).toLowerCase();
            if (Arrays.asList("png", "jpg", "jpeg", "bmp", "gif").contains(extension)) {
                imageFiles.put(baseName, file);
            } else if ("json".equals(extension)) {
                jsonFiles.put(baseName, file);
            }
        }

        List<ImageVO> resultVOs = new ArrayList<>();
        for (Map.Entry<String, MultipartFile> entry : imageFiles.entrySet()) {
            String baseName = entry.getKey();
            MultipartFile imageFile = entry.getValue();
            MultipartFile jsonFile = jsonFiles.get(baseName);

            try {
                ImageVO savedImageVO = processAndSaveSingleImageEntry(folder, imageFile, jsonFile, currentUser);
                resultVOs.add(savedImageVO);
            } catch (Exception e) {
                log.error("批量上传中处理文件 {} 失败: {}", imageFile.getOriginalFilename(), e.getMessage(), e);
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "处理文件 " + imageFile.getOriginalFilename() + " 失败: " + e.getMessage());
            }
        }
        return resultVOs;
    }

    private ImageVO processAndSaveSingleImageEntry(Folder folder, MultipartFile imageFile, MultipartFile jsonFile, User currentUser) {
        String originalFilename = imageFile.getOriginalFilename();
        String uniqueFilenameSuffix = UUID.randomUUID().toString().substring(0, 8) + "-" + originalFilename;
        Path relativePath;
        switch (folder.getSpace()) {
            case "organization_public":
                relativePath = Paths.get("organization", folder.getOwnerOrganizationId(), uniqueFilenameSuffix);
                break;
            case "user_private": case "user_public":
                relativePath = Paths.get("user", folder.getOwnerUserId(), uniqueFilenameSuffix);
                break;
            default:
                relativePath = Paths.get("public", uniqueFilenameSuffix);
                break;
        }
        String storagePath = relativePath.toString().replace("\\", "/");
        File destFile = new File(uploadDir, storagePath);
        try {
            Files.createDirectories(destFile.getParentFile().toPath());
            imageFile.transferTo(destFile);
        } catch (IOException e) {
            log.error("文件保存失败, storagePath: {}", storagePath, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "文件保存失败");
        }

        Image image = new Image();
        try {
            image.setFolderId(folder.getId());
            image.setOriginalFilename(originalFilename);
            image.setStoragePath(storagePath);
            image.setUploaderId(currentUser.getId());
            image.setFileSize(imageFile.getSize());
            BufferedImage bufferedImage = ImageIO.read(destFile);
            if (bufferedImage != null) {
                image.setWidth(bufferedImage.getWidth());
                image.setHeight(bufferedImage.getHeight());
            }
            this.save(image);

            if (jsonFile != null) {
                String jsonContent = new String(jsonFile.getBytes());
                Annotation annotation = new Annotation();
                annotation.setImageId(image.getId());
                annotation.setJsonContent(jsonContent);
                annotation.setLastEditorId(currentUser.getId());
                annotationService.save(annotation);
            }

            NodeCreateRequest nodeCreateRequest = new NodeCreateRequest();
            nodeCreateRequest.setName(storagePath);
            Map<String, Object> properties = new HashMap<>();
            properties.put("space", folder.getSpace());
            properties.put("ownerUserId", folder.getOwnerUserId());
            properties.put("ownerOrganizationId", folder.getOwnerOrganizationId());
            nodeCreateRequest.setProperties(properties);
            String nodeName = graphService.createNode(nodeCreateRequest, currentUser);

            // 成功创建节点后，触发关系自动创建
            graphService.triggerAutoRelationshipCreation(nodeName);

        } catch (Exception e) {
            rollbackFile(destFile);
            log.error("图片处理或入库失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "图片信息入库或知识图谱节点创建失败");
        }
        return ImageVO.fromEntity(image);
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteImage(String imageId, User currentUser) {
        Image image = this.getById(imageId);
        if (image == null) {
            return;
        }
        permissionService.checkNodePermission(image.getStoragePath(), currentUser, PermissionEnum.WRITE);

        boolean success = this.removeById(imageId);
        if (!success) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "删除图片数据库记录失败");
        }

        annotationService.remove(new QueryWrapper<Annotation>().eq("imageId", imageId));

        try {
            NodeDeleteRequest nodeDeleteRequest = new NodeDeleteRequest();
            nodeDeleteRequest.setName(image.getStoragePath());
            graphService.deleteNode(nodeDeleteRequest, currentUser);
        } catch (Exception e) {
            log.error("删除 Neo4j 节点失败，将回滚操作", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "删除知识图谱节点失败");
        }

        rollbackFile(new File(uploadDir, image.getStoragePath()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteImagesBatch(ImageBatchDeleteRequest request, User currentUser) {
        if (request == null || request.getIds() == null || request.getIds().isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数错误，未提供要删除的图片ID");
        }
        for (String imageId : request.getIds()) {
            this.deleteImage(imageId, currentUser);
        }
    }

    @Override
    public Page<ImageVO> listImagesByPage(ImageListRequest request, User currentUser) {
        Folder folder = folderMapper.selectById(request.getFolderId());
        if (folder == null || folder.getIsDelete() == 1) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "文件夹不存在");
        }
        permissionService.checkFolderPermission(folder, currentUser, PermissionEnum.READ);
        Page<Image> page = new Page<>(request.getCurrent(), request.getPageSize());
        QueryWrapper<Image> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("folderId", request.getFolderId());
        queryWrapper.like(StringUtils.isNotBlank(request.getSearchKeyword()), "originalFilename", request.getSearchKeyword());
        queryWrapper.orderByDesc("createTime");
        Page<Image> imagePage = this.page(page, queryWrapper);
        Page<ImageVO> imageVOPage = new Page<>(imagePage.getCurrent(), imagePage.getSize(), imagePage.getTotal());
        imageVOPage.setRecords(imagePage.getRecords().stream()
                .map(ImageVO::fromEntity)
                .collect(Collectors.toList()));
        return imageVOPage;
    }

    @Override
    public ImageVO getImageVOById(String imageId, User currentUser) {
        Image image = this.getById(imageId);
        if (image == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "图片不存在");
        }
        permissionService.checkNodePermission(image.getStoragePath(), currentUser, PermissionEnum.READ);
        return ImageVO.fromEntity(image);
    }

    @Override
    public ImageVO updateImage(ImageUpdateRequest request, User currentUser) {
        Image image = this.getById(request.getImageId());
        if (image == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "图片不存在");
        }
        permissionService.checkNodePermission(image.getStoragePath(), currentUser, PermissionEnum.WRITE);
        if (StringUtils.isNotBlank(request.getOriginalFilename())) {
            image.setOriginalFilename(request.getOriginalFilename());
        }
        this.updateById(image);
        return ImageVO.fromEntity(image);
    }

    @Override
    public ImageVO getImageVOByFolderAndName(ImageGetByNameRequest request, User currentUser) {
        String folderId = request.getFolderId();
        String filename = request.getOriginalFilename();
        if (folderId == null || StringUtils.isBlank(filename)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件夹ID和文件名不能为空");
        }
        Folder folder = folderMapper.selectById(folderId);
        if (folder == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "文件夹不存在");
        }
        permissionService.checkFolderPermission(folder, currentUser, PermissionEnum.READ);
        QueryWrapper<Image> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("folderId", folderId);
        queryWrapper.eq("originalFilename", filename);
        Image image = this.getOne(queryWrapper);
        return ImageVO.fromEntity(image);
    }

    @Override
    public org.springframework.core.io.Resource downloadImage(String imageId, User currentUser) {
        Image image = this.getById(imageId);
        if (image == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "图片不存在");
        }
        permissionService.checkNodePermission(image.getStoragePath(), currentUser, PermissionEnum.READ);
        try {
            Path filePath = Paths.get(uploadDir).resolve(image.getStoragePath()).normalize();
            org.springframework.core.io.Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                log.error("物理文件不存在或不可读: {}", filePath);
                throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "无法读取文件或文件不存在");
            }
        } catch (MalformedURLException e) {
            log.error("文件路径URL格式错误: {}", image.getStoragePath(), e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "文件路径格式错误");
        }
    }

    private void rollbackFile(File file) {
        if (file != null && file.exists()) {
            try {
                Files.delete(file.toPath());
            } catch (IOException e) {
                log.error("回滚物理文件删除失败: {}", file.getAbsolutePath(), e);
            }
        }
    }
}
// END OF FILE: src/main/java/com/scy/mytemplate/service/impl/ImageServiceImpl.java