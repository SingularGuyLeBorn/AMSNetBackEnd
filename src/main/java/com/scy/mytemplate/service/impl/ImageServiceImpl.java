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
import com.scy.mytemplate.model.dto.annotation.AnnotationCreateRequest;
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
import lombok.Data;
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
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
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
        List<ImageVO> result = this.uploadImagesWithAnnotationsBatch(folderId, Collections.emptyMap(), Collections.singletonList(file), currentUser);
        if (result.isEmpty()) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "图片上传失败");
        }
        return result.get(0);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<ImageVO> uploadImagesBatch(String folderId, List<MultipartFile> files, User currentUser) {
        return this.uploadImagesWithAnnotationsBatch(folderId, Collections.emptyMap(), files, currentUser);
    }

    @Data
    private static class GroupedFiles {
        MultipartFile imageFile;
        MultipartFile jsonFile;
        MultipartFile yoloFile;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<ImageVO> uploadImagesWithAnnotationsBatch(String folderId, Map<Integer, String> classMap, List<MultipartFile> files, User currentUser) {
        Folder folder = folderMapper.selectById(folderId);
        if (folder == null || folder.getIsDelete() == 1) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "目标文件夹不存在");
        }
        permissionService.checkFolderPermission(folder, currentUser, PermissionEnum.WRITE);

        Map<String, GroupedFiles> fileGroups = new HashMap<>();
        for (MultipartFile file : files) {
            String baseName = FilenameUtils.getBaseName(file.getOriginalFilename());
            if (StringUtils.isBlank(baseName)) continue;

            fileGroups.putIfAbsent(baseName, new GroupedFiles());
            GroupedFiles group = fileGroups.get(baseName);
            String extension = FilenameUtils.getExtension(file.getOriginalFilename()).toLowerCase();

            switch (extension) {
                case "png":
                case "jpg":
                case "jpeg":
                    group.setImageFile(file);
                    break;
                case "json":
                    group.setJsonFile(file);
                    break;
                case "txt":
                    group.setYoloFile(file);
                    break;
            }
        }

        List<ImageVO> resultVOs = new ArrayList<>();
        for (Map.Entry<String, GroupedFiles> entry : fileGroups.entrySet()) {
            GroupedFiles group = entry.getValue();
            if (group.getImageFile() == null) {
                log.warn("Skipping annotation file group '{}' as it has no corresponding image file.", entry.getKey());
                continue;
            }

            MultipartFile imageFile = group.getImageFile();
            Map<String, Object> annotationJson = determineAnnotationJson(group, classMap);

            ImageVO savedImageVO = saveImageAndAnnotation(folder, imageFile, annotationJson, currentUser);
            resultVOs.add(savedImageVO);
        }

        return resultVOs;
    }

    private Map<String, Object> determineAnnotationJson(GroupedFiles group, Map<Integer, String> classMap) {
        try {
            if (group.getJsonFile() != null) {
                String jsonContent = new String(group.getJsonFile().getBytes(), StandardCharsets.UTF_8);
                return objectMapper.readValue(jsonContent, Map.class);
            }
            if (group.getYoloFile() != null) {
                return convertYoloToJson(group.getYoloFile(), group.getImageFile(), classMap);
            }
        } catch (IOException e) {
            log.error("Failed to read annotation file for base name '{}', defaulting to empty. Error: {}", FilenameUtils.getBaseName(group.getImageFile().getOriginalFilename()), e.getMessage());
        }
        return new HashMap<>();
    }

    private Map<String, Object> convertYoloToJson(MultipartFile yoloFile, MultipartFile imageFile, Map<Integer, String> classMap) throws IOException {
        BufferedImage bimg = ImageIO.read(imageFile.getInputStream());
        int imageWidth = bimg.getWidth();
        int imageHeight = bimg.getHeight();

        List<Map<String, Object>> cpnts = new ArrayList<>();
        Map<String, Integer> nameCounters = new HashMap<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(yoloFile.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.trim().split("\\s+");
                if (parts.length < 5) continue;

                try {
                    int classIndex = Integer.parseInt(parts[0]);
                    double relX = Double.parseDouble(parts[1]);
                    double relY = Double.parseDouble(parts[2]);
                    double relW = Double.parseDouble(parts[3]);
                    double relH = Double.parseDouble(parts[4]);

                    String label = classMap.getOrDefault(classIndex, "unknown_" + classIndex);
                    int counter = nameCounters.getOrDefault(label, 0);
                    String uniqueName = label + "_" + counter;
                    nameCounters.put(label, counter + 1);

                    double absWidth = relW * imageWidth;
                    double absHeight = relH * imageHeight;
                    double absCenterX = relX * imageWidth;
                    double absCenterY = relY * imageHeight;

                    Map<String, Object> component = new LinkedHashMap<>();
                    component.put("b", absCenterY + absHeight / 2);
                    component.put("l", absCenterX - absWidth / 2);
                    component.put("r", absCenterX + absWidth / 2);
                    component.put("t", absCenterY - absHeight / 2);
                    component.put("type", label);
                    component.put("name", uniqueName);
                    cpnts.add(component);
                } catch (NumberFormatException e) {
                    log.warn("Skipping invalid YOLO line: {}", line);
                }
            }
        }

        Map<String, Object> finalJson = new LinkedHashMap<>();
        finalJson.put("key_points", Collections.emptyList());
        finalJson.put("segments", Collections.emptyList());
        finalJson.put("cpnts", cpnts);
        finalJson.put("netlist_scs", "");
        finalJson.put("netlist_cdl", "");
        finalJson.put("local", new HashMap<>());
        finalJson.put("global", new HashMap<>());
        return finalJson;
    }


    private ImageVO saveImageAndAnnotation(Folder folder, MultipartFile file, Map<String, Object> annotationJson, User currentUser) {
        String originalFilename = file.getOriginalFilename();
        String uniqueFilenameSuffix = UUID.randomUUID().toString().substring(0, 8) + "-" + originalFilename;
        Path relativePath;
        switch (folder.getSpace()) {
            case "organization":
                relativePath = Paths.get("organization", folder.getOwnerOrganizationId(), uniqueFilenameSuffix);
                break;
            case "private":
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
            file.transferTo(destFile);
        } catch (IOException e) {
            log.error("File saving failed, storagePath: {}", storagePath, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "文件保存失败");
        }

        Image image = new Image();
        try {
            image.setFolderId(folder.getId());
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
            log.error("Image metadata persistence failed", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "图片信息入库失败");
        }

        try {
            NodeCreateRequest nodeCreateRequest = new NodeCreateRequest();
            nodeCreateRequest.setName(storagePath);
            Map<String, Object> properties = new HashMap<>();
            properties.put("space", folder.getSpace());
            properties.put("ownerUserId", folder.getOwnerUserId());
            properties.put("ownerOrganizationId", folder.getOwnerOrganizationId());
            nodeCreateRequest.setProperties(properties);
            graphService.createNode(nodeCreateRequest, currentUser);
        } catch (Exception e) {
            rollbackFile(destFile);
            log.error("Neo4j node creation failed", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "创建知识图谱节点失败");
        }

        try {
            AnnotationCreateRequest annotationRequest = new AnnotationCreateRequest();
            annotationRequest.setImageId(image.getId());
            annotationRequest.setJsonContent(annotationJson);
            annotationService.createAnnotation(annotationRequest, currentUser);
        } catch (Exception e) {
            rollbackFile(destFile);
            log.error("Annotation creation failed", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "创建标注数据失败: " + e.getMessage());
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