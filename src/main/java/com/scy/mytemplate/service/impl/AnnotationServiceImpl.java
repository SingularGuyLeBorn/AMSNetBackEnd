// FILE: src/main/java/com/scy/mytemplate/service/impl/AnnotationServiceImpl.java
package com.scy.mytemplate.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.scy.mytemplate.common.ErrorCode;
import com.scy.mytemplate.exception.BusinessException;
import com.scy.mytemplate.mapper.AnnotationMapper;
import com.scy.mytemplate.mapper.ImageMapper;
import com.scy.mytemplate.model.dto.annotation.AnnotationCreateRequest;
import com.scy.mytemplate.model.dto.annotation.AnnotationUpdateRequest;
import com.scy.mytemplate.model.entity.Annotation;
import com.scy.mytemplate.model.entity.Image;
import com.scy.mytemplate.model.entity.User;
import com.scy.mytemplate.model.enums.PermissionEnum;
import com.scy.mytemplate.model.vo.AnnotationVO;
import com.scy.mytemplate.service.AnnotationService;
import com.scy.mytemplate.service.PermissionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Map;

/**
 * 标注数据服务实现。
 *
 * @author Bedrock
 */
@Service
@Slf4j
public class AnnotationServiceImpl extends ServiceImpl<AnnotationMapper, Annotation> implements AnnotationService {

    @Resource
    private ImageMapper imageMapper;

    @Resource
    private PermissionService permissionService;

    @Resource
    private AnnotationMapper annotationMapper;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    @Transactional
    public AnnotationVO createAnnotation(AnnotationCreateRequest request, User currentUser) {
        String imageId = request.getImageId();
        Image image = imageMapper.selectById(imageId);
        if (image == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "关联的图片不存在");
        }
        // 权限检查：必须对图片有写权限才能为其创建标注
        permissionService.checkNodePermission(image.getStoragePath(), currentUser, PermissionEnum.WRITE);

        // 业务规则检查：一张图片只能有一个标注
        QueryWrapper<Annotation> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("imageId", imageId);
        if (annotationMapper.selectCount(queryWrapper) > 0) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "该图片已存在标注，请使用更新接口");
        }

        String jsonContentStr = serializeJsonContent(request.getJsonContent());

        Annotation annotation = new Annotation();
        annotation.setImageId(imageId);
        annotation.setJsonContent(jsonContentStr);
        annotation.setLastEditorId(currentUser.getId());

        boolean success = this.save(annotation);
        if (!success) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "创建标注失败，数据库错误");
        }
        return AnnotationVO.fromEntity(annotation);
    }

    @Override
    public AnnotationVO getAnnotationByImageId(String imageId, User currentUser) {
        Image image = imageMapper.selectById(imageId);
        if (image == null) {
            // 在前端导航时，可能会请求一个已被删除的图片的标注，这不应是错误，而是返回空数据
            return null;
        }
        // 权限检查：必须对图片有读权限才能查看其标注
        permissionService.checkNodePermission(image.getStoragePath(), currentUser, PermissionEnum.READ);

        QueryWrapper<Annotation> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("imageId", imageId);
        Annotation annotation = annotationMapper.selectOne(queryWrapper);

        return AnnotationVO.fromEntity(annotation);
    }

    @Override
    @Transactional
    public AnnotationVO updateAnnotation(AnnotationUpdateRequest request, User currentUser) {
        String annotationId = request.getAnnotationId();
        Annotation annotation = this.getById(annotationId);
        if (annotation == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "要更新的标注记录不存在");
        }

        Image image = imageMapper.selectById(annotation.getImageId());
        if (image == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "标注关联的图片不存在，数据可能已损坏");
        }
        // 权限检查：必须对图片有写权限才能修改其标注
        permissionService.checkNodePermission(image.getStoragePath(), currentUser, PermissionEnum.WRITE);

        String jsonContentStr = serializeJsonContent(request.getJsonContent());

        annotation.setJsonContent(jsonContentStr);
        annotation.setLastEditorId(currentUser.getId());

        boolean success = this.updateById(annotation);
        if (!success) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "更新标注数据失败");
        }
        return AnnotationVO.fromEntity(annotation);
    }

    @Override
    @Transactional
    public void deleteAnnotation(String annotationId, User currentUser) {
        Annotation annotation = this.getById(annotationId);
        if (annotation == null) {
            // 如果不存在，可能已被删除，幂等处理
            return;
        }

        Image image = imageMapper.selectById(annotation.getImageId());
        if (image == null) {
            // 即使关联图片不存在，也应允许删除孤立的标注数据
            log.warn("正在删除一个孤立的标注记录 (ID: {}), 其关联的图片 (ID: {}) 已不存在。", annotation.getId(), annotation.getImageId());
        } else {
            // 如果图片存在，则进行权限检查
            permissionService.checkNodePermission(image.getStoragePath(), currentUser, PermissionEnum.WRITE);
        }

        int deletedRows = annotationMapper.deleteById(annotationId);
        if (deletedRows == 0) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "删除标注失败，数据库错误");
        }
    }

    /**
     * 将 Map 对象序列化为 JSON 字符串的辅助方法。
     *
     * @param content Map 形式的 JSON 内容
     * @return JSON 字符串
     */
    private String serializeJsonContent(Map<String, Object> content) {
        try {
            if (content == null) {
                return "{}";
            }
            return objectMapper.writeValueAsString(content);
        } catch (JsonProcessingException e) {
            log.error("JSON 序列化失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "无效的JSON内容");
        }
    }
}
// END OF FILE: src/main/java/com/scy/mytemplate/service/impl/AnnotationServiceImpl.java
