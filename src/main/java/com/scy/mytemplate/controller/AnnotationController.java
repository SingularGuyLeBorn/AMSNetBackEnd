// FILE: src/main/java/com/scy/mytemplate/controller/AnnotationController.java
package com.scy.mytemplate.controller;

import com.scy.mytemplate.common.BaseResponse;
import com.scy.mytemplate.common.ErrorCode;
import com.scy.mytemplate.common.ResultUtils;
import com.scy.mytemplate.exception.BusinessException;
import com.scy.mytemplate.model.dto.annotation.AnnotationCreateRequest;
import com.scy.mytemplate.model.dto.annotation.AnnotationDeleteRequest;
import com.scy.mytemplate.model.dto.annotation.AnnotationGetRequest;
import com.scy.mytemplate.model.dto.annotation.AnnotationUpdateRequest;
import com.scy.mytemplate.model.entity.User;
import com.scy.mytemplate.model.vo.AnnotationVO;
import com.scy.mytemplate.service.AnnotationService;
import com.scy.mytemplate.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * 标注数据管理接口。
 *
 * @author Bedrock
 */
@RestController
@RequestMapping("/annotation")
@Slf4j
@Api(tags = "AnnotationController")
public class AnnotationController {

    @Resource
    private AnnotationService annotationService;

    @Resource
    private UserService userService;

    /**
     * 为指定图片创建新的标注数据
     */
    @PostMapping("/create")
//    @ApiOperation(value = "创建图片标注", notes = "为指定图片创建一条新的JSON标注记录")
    public BaseResponse<AnnotationVO> createAnnotation(@RequestBody AnnotationCreateRequest createRequest, HttpServletRequest request) {
        if (createRequest == null || createRequest.getImageId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "必须提供图片ID");
        }
        User currentUser = userService.getLoginUser(request);
        AnnotationVO annotationVO = annotationService.createAnnotation(createRequest, currentUser);
        return ResultUtils.success(annotationVO);
    }

    /**
     * 获取指定图片的标注数据
     */
    @PostMapping("/get")
//    @ApiOperation(value = "获取图片标注", notes = "根据图片ID获取其对应的JSON标注数据")
    public BaseResponse<AnnotationVO> getAnnotation(@RequestBody AnnotationGetRequest getRequest, HttpServletRequest request) {
        if (getRequest == null || getRequest.getImageId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "必须提供图片ID");
        }
        User currentUser = userService.getLoginUser(request);
        AnnotationVO annotationVO = annotationService.getAnnotationByImageId(getRequest.getImageId(), currentUser);
        return ResultUtils.success(annotationVO);
    }

    /**
     * 更新指定图片的标注数据
     */
    @PostMapping("/update")
//    @ApiOperation(value = "更新图片标注", notes = "根据标注ID更新已存在的JSON标注记录")
    public BaseResponse<AnnotationVO> updateAnnotation(@RequestBody AnnotationUpdateRequest updateRequest, HttpServletRequest request) {
        if (updateRequest == null || updateRequest.getAnnotationId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "必须提供标注ID");
        }
        User currentUser = userService.getLoginUser(request);
        AnnotationVO annotationVO = annotationService.updateAnnotation(updateRequest, currentUser);
        return ResultUtils.success(annotationVO);
    }

    /**
     * 删除指定图片的标注数据
     */
    @PostMapping("/delete")
//    @ApiOperation(value = "删除图片标注", notes = "根据标注ID删除一条标注记录")
    public BaseResponse<Boolean> deleteAnnotation(@RequestBody AnnotationDeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getAnnotationId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "必须提供标注ID");
        }
        User currentUser = userService.getLoginUser(request);
        annotationService.deleteAnnotation(deleteRequest.getAnnotationId(), currentUser);
        return ResultUtils.success(true);
    }
}
// END OF FILE: src/main/java/com/scy/mytemplate/controller/AnnotationController.java