// FILE: src/main/java/com/scy/mytemplate/model/dto/annotation/AnnotationCreateRequest.java
package com.scy.mytemplate.model.dto.annotation;

import lombok.Data;
import java.io.Serializable;
import java.util.Map;

/**
 * 创建新标注的请求体。
 *
 * @author Bedrock
 */
@Data
public class AnnotationCreateRequest implements Serializable {

    /**
     * 标注将要关联的图片ID。
     */
    private String imageId;

    /**
     * 初始的标注内容 (JSON 对象)。
     */
    private Map<String, Object> jsonContent;

    private static final long serialVersionUID = 1L;
}
// END OF FILE: src/main/java/com/scy/mytemplate/model/dto/annotation/AnnotationCreateRequest.java