// FILE: src/main/java/com/scy/mytemplate/controller/ImageController.java
package com.scy.mytemplate.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.scy.mytemplate.common.BaseResponse;
import com.scy.mytemplate.common.ErrorCode;
import com.scy.mytemplate.common.ResultUtils;
import com.scy.mytemplate.exception.BusinessException;
import com.scy.mytemplate.model.dto.image.*;
import com.scy.mytemplate.model.entity.User;
import com.scy.mytemplate.model.vo.ImageVO;
import com.scy.mytemplate.service.ImageService;
import com.scy.mytemplate.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 图片资源接口 (完整CRUD版)。
 * 负责处理图片的上传、查询、更新、删除、下载等所有操作。
 *
 * @author Bedrock
 */
@RestController
@RequestMapping("/image")
@Slf4j
@Api(tags = "ImageController")
public class ImageController {

    @Resource
    private ImageService imageService;

    @Resource
    private UserService userService;

    @Resource
    private ObjectMapper objectMapper;

    /**
     * 上传图片 (Create)
     */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ApiOperation(value = "上传图片(Create)", notes = "将图片上传到指定的文件夹，并创建关联的数据库记录和知识图谱节点。")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "folderId", value = "目标文件夹ID", required = true, dataType = "string", paramType = "form"),
            @ApiImplicitParam(name = "file", value = "要上传的图片文件", required = true, dataType = "__file", paramType = "form")
    })
    public BaseResponse<ImageVO> uploadImage(
            @RequestParam("folderId") String folderId,
            @RequestPart("file") MultipartFile file,
            HttpServletRequest request) {

        if (folderId == null || folderId.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "未指定目标文件夹");
        }
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "上传文件不能为空");
        }

        User currentUser = userService.getLoginUser(request);
        ImageVO savedImageVO = imageService.uploadImage(folderId, file, currentUser);
        return ResultUtils.success(savedImageVO);
    }

    /**
     * 批量上传图片 (Batch Create) - 仅图片
     */
    @PostMapping(value = "/upload/batch", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ApiOperation(value = "批量上传图片(Batch Create)", notes = "将多张图片一次性上传到指定的文件夹。")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "folderId", value = "目标文件夹ID", required = true, dataType = "string", paramType = "form"),
            @ApiImplicitParam(name = "files", value = "要上传的多个图片文件", required = true, dataType = "__file", paramType = "form", allowMultiple = true)
    })
    public BaseResponse<List<ImageVO>> uploadImagesBatch(
            @RequestParam("folderId") String folderId,
            @RequestPart("files") List<MultipartFile> files,
            HttpServletRequest request) {

        if (folderId == null || folderId.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "未指定目标文件夹");
        }
        if (files == null || files.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "上传文件不能为空");
        }

        User currentUser = userService.getLoginUser(request);
        List<ImageVO> savedImageVOs = imageService.uploadImagesBatch(folderId, files, currentUser);
        return ResultUtils.success(savedImageVOs);
    }

    /**
     * (修正) 批量上传图片及关联标注文件 (Batch Create with Annotations)
     */
    @PostMapping(value = "/upload/batch-with-annotations", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ApiOperation(value = "批量上传图片及标注", notes = "将图片(.png/.jpg), json或yolo(.txt)文件配对上传。")
    public BaseResponse<List<ImageVO>> uploadImagesWithAnnotationsBatch(
            @RequestParam("folderId") String folderId,
            @RequestParam("classMap") String classMapJson,
            @RequestPart("files") List<MultipartFile> files,
            HttpServletRequest request) {

        if (folderId == null || folderId.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "缺少 folderId 参数");
        }
        if (files == null || files.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "上传文件列表不能为空");
        }

        Map<Integer, String> classMap;
        try {
            classMap = objectMapper.readValue(classMapJson, new TypeReference<Map<Integer, String>>() {});
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "classMap JSON 格式错误");
        }

        User currentUser = userService.getLoginUser(request);
        List<ImageVO> resultVOs = imageService.uploadImagesWithAnnotationsBatch(folderId, classMap, files, currentUser);
        return ResultUtils.success(resultVOs);
    }


    /**
     * 下载/获取图片文件 (Read-File)
     */
    @GetMapping("/download/{imageId}")
    @ApiOperation(value = "获取图片文件(Read-File)", notes = "根据图片ID，返回图片的文件流，用于在前端<img>标签中显示。")
    public ResponseEntity<org.springframework.core.io.Resource> downloadImage(@PathVariable String imageId, HttpServletRequest request) {
        User currentUser = userService.getLoginUser(request);
        org.springframework.core.io.Resource resource = imageService.downloadImage(imageId, currentUser);

        String contentType = "application/octet-stream";
        try {
            Path path = resource.getFile().toPath();
            contentType = Files.probeContentType(path);
        } catch (IOException e) {
            log.warn("无法确定文件类型: {}", resource.getFilename());
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }


    /**
     * 获取图片列表 (Read-Many)
     */
    @PostMapping("/list/page")
    @ApiOperation("获取图片列表(Read-Many)")
    public BaseResponse<Page<ImageVO>> listImagesByPage(@RequestBody ImageListRequest listRequest, HttpServletRequest request) {
        if (listRequest == null || listRequest.getFolderId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "必须提供文件夹ID");
        }
        User currentUser = userService.getLoginUser(request);
        Page<ImageVO> imagePage = imageService.listImagesByPage(listRequest, currentUser);
        return ResultUtils.success(imagePage);
    }

    /**
     * 获取图片详情 (Read-One)
     */
    @PostMapping("/get")
    @ApiOperation("获取图片详情(Read-One)")
    public BaseResponse<ImageVO> getImageById(@RequestBody ImageGetRequest getRequest, HttpServletRequest request) {
        if (getRequest == null || getRequest.getImageId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "必须提供图片ID");
        }
        User currentUser = userService.getLoginUser(request);
        ImageVO imageVO = imageService.getImageVOById(getRequest.getImageId(), currentUser);
        return ResultUtils.success(imageVO);
    }

    /**
     * 更新图片信息 (Update)
     */
    @PostMapping("/update")
    @ApiOperation("更新图片信息(Update)")
    public BaseResponse<ImageVO> updateImage(@RequestBody ImageUpdateRequest updateRequest, HttpServletRequest request) {
        if (updateRequest == null || updateRequest.getImageId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User currentUser = userService.getLoginUser(request);
        ImageVO updatedImageVO = imageService.updateImage(updateRequest, currentUser);
        return ResultUtils.success(updatedImageVO);
    }

    /**
     * 删除图片 (Delete)
     */
    @PostMapping("/delete")
    @ApiOperation("删除图片(Delete)")
    public BaseResponse<Boolean> deleteImage(@RequestBody ImageDeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getImageId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User currentUser = userService.getLoginUser(request);
        imageService.deleteImage(deleteRequest.getImageId(), currentUser);
        return ResultUtils.success(true);
    }

    /**
     * 批量删除图片 (Batch Delete)
     */
    @PostMapping("/delete/batch")
    @ApiOperation("批量删除图片(Delete-Many)")
    public BaseResponse<Boolean> deleteImagesBatch(@RequestBody ImageBatchDeleteRequest batchDeleteRequest, HttpServletRequest request) {
        if (batchDeleteRequest == null || batchDeleteRequest.getIds() == null || batchDeleteRequest.getIds().isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数不能为空");
        }
        User currentUser = userService.getLoginUser(request);
        imageService.deleteImagesBatch(batchDeleteRequest, currentUser);
        return ResultUtils.success(true);
    }

    @PostMapping("/getByFolderAndName")
    @ApiOperation("根据文件夹和文件名获取图片信息")
    public BaseResponse<ImageVO> getImageByFolderAndName(@RequestBody ImageGetByNameRequest getRequest, HttpServletRequest request) {
        if (getRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User currentUser = userService.getLoginUser(request);
        ImageVO imageVO = imageService.getImageVOByFolderAndName(getRequest, currentUser);
        // 注意：即使找不到也返回成功，让前端判断data是否为null
        return ResultUtils.success(imageVO);
    }
}