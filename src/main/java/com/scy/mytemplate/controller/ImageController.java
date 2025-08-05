// FILE: src/main/java/com/scy/mytemplate/controller/ImageController.java
package com.scy.mytemplate.controller;

import com.scy.mytemplate.common.BaseResponse;
import com.scy.mytemplate.common.ErrorCode;
import com.scy.mytemplate.common.ResultUtils;
import com.scy.mytemplate.exception.BusinessException;
import com.scy.mytemplate.model.dto.image.ImageDeleteRequest;
import com.scy.mytemplate.model.entity.Image;
import com.scy.mytemplate.model.entity.User;
import com.scy.mytemplate.service.ImageService;
import com.scy.mytemplate.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * 图片资源接口。
 * 负责处理图片的上传、删除等操作。
 *
 * @author Bedrock
 */
@RestController
@RequestMapping("/image")
@Slf4j
@Api(tags = "imageController")
public class ImageController {

    @Resource
    private ImageService imageService;

    @Resource
    private UserService userService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ApiOperation(value = "上传图片", notes = "将图片上传到指定的文件夹，并创建关联的数据库记录和知识图谱节点。")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "folderId", value = "目标文件夹ID", required = true, dataType = "long", paramType = "form"),
            @ApiImplicitParam(name = "file", value = "要上传的图片文件", required = true, dataType = "__file", paramType = "form")
    })
    public BaseResponse<Image> uploadImage(
            @RequestParam("folderId") Long folderId,
            @RequestPart("file") MultipartFile file,
            HttpServletRequest request) {

        if (folderId == null || folderId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "未指定目标文件夹");
        }
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "上传文件不能为空");
        }

        User currentUser = userService.getLoginUser(request);
        Image savedImage = imageService.uploadImage(folderId, file, currentUser);

        return ResultUtils.success(savedImage);
    }

    @PostMapping("/delete")
    @ApiOperation(value = "删除图片（逻辑删除）")
    public BaseResponse<Boolean> deleteImage(@RequestBody ImageDeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getImageId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User currentUser = userService.getLoginUser(request);
        imageService.deleteImage(deleteRequest.getImageId(), currentUser);
        return ResultUtils.success(true);
    }
}
// END OF FILE: src/main/java/com/scy/mytemplate/controller/ImageController.java