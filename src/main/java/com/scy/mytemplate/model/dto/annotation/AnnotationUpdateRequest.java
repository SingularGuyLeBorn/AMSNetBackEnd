// FILE: src/main/java/com/scy/mytemplate/model/dto/annotation/AnnotationUpdateRequest.java
package com.scy.mytemplate.model.dto.annotation;

import lombok.Data;
import java.io.Serializable;
import java.util.Map;

/**
 * 更新现有标注的请求体。
 *
 * @author Bedrock
 */
@Data
public class AnnotationUpdateRequest implements Serializable {

    /**
     * 要更新的标注记录本身的主键ID。
     */
    private Long annotationId;

    /**
     * 最新的标注内容 (JSON 对象)。
     */
    private Map<String, Object> jsonContent;

    private static final long serialVersionUID = 1L;
}
// END OF FILE: src/main/java/com/scy/mytemplate/model/dto/annotation/AnnotationUpdateRequest.java