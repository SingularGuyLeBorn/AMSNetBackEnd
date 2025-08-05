// FILE: src/main/java/com/scy/mytemplate/service/AnnotationService.java
package com.scy.mytemplate.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.scy.mytemplate.model.dto.annotation.AnnotationCreateRequest;
import com.scy.mytemplate.model.dto.annotation.AnnotationUpdateRequest;
import com.scy.mytemplate.model.entity.Annotation;
import com.scy.mytemplate.model.entity.User;
import com.scy.mytemplate.model.vo.AnnotationVO;

/**
 * 标注数据服务接口。
 *
 * @author Bedrock
 */
public interface AnnotationService extends IService<Annotation> {

    /**
     * 为一张图片创建新的标注数据。
     * 如果该图片已存在标注，将抛出异常。
     *
     * @param request     包含 imageId 和 jsonContent 的创建请求。
     * @param currentUser 当前操作的用户。
     * @return 成功创建后的标注视图对象。
     */
    AnnotationVO createAnnotation(AnnotationCreateRequest request, User currentUser);

    /**
     * 根据图片ID获取其标注数据。
     *
     * @param imageId     目标图片的ID。
     * @param currentUser 当前操作的用户。
     * @return 标注数据的视图对象，如果不存在则返回 null。
     */
    AnnotationVO getAnnotationByImageId(Long imageId, User currentUser);

    /**
     * 更新一个已存在的标注数据。
     *
     * @param request     包含 annotationId 和新的 jsonContent 的更新请求。
     * @param currentUser 当前操作的用户。
     * @return 成功更新后的标注数据视图对象。
     */
    AnnotationVO updateAnnotation(AnnotationUpdateRequest request, User currentUser);

    /**
     * (逻辑)删除一个标注数据。
     *
     * @param annotationId 要删除的标注记录ID。
     * @param currentUser  当前操作的用户。
     */
    void deleteAnnotation(Long annotationId, User currentUser);
}
// END OF FILE: src/main/java/com/scy/mytemplate/service/AnnotationService.java