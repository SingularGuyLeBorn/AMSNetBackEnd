// FILE: src/main/java/com/scy/mytemplate/model/dto/annotation/AnnotationDeleteRequest.java
package com.scy.mytemplate.model.dto.annotation;

import lombok.Data;
import java.io.Serializable;

/**
 * (逻辑)删除标注的请求体。
 *
 * @author Bedrock
 */
@Data
public class AnnotationDeleteRequest implements Serializable {

    /**
     * 要删除的标注记录的主键ID。
     */
    private String annotationId;

    private static final long serialVersionUID = 1L;
}
// END OF FILE: src/main/java/com/scy/mytemplate/model/dto/annotation/AnnotationDeleteRequest.java