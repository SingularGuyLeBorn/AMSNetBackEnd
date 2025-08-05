// FILE: src/main/java/com/scy/mytemplate/model/dto/annotation/AnnotationGetRequest.java
package com.scy.mytemplate.model.dto.annotation;

import lombok.Data;
import java.io.Serializable;

/**
 * 获取图片标注数据的请求体。
 *
 * @author Bedrock
 */
@Data
public class AnnotationGetRequest implements Serializable {

    /**
     * 要获取标注数据的图片ID。
     */
    private Long imageId;

    private static final long serialVersionUID = 1L;
}
// END OF FILE: src/main/java/com/scy/mytemplate/model/dto/annotation/AnnotationGetRequest.java