// FILE: src/main/java/com/scy/mytemplate/model/dto/image/ImageBatchDeleteRequest.java
package com.scy.mytemplate.model.dto.image;

import lombok.Data;
import javax.validation.constraints.NotEmpty;
import java.io.Serializable;
import java.util.List;

/**
 * 批量删除图片的请求体。
 *
 * @author Bedrock
 */
@Data
public class ImageBatchDeleteRequest implements Serializable {

    /**
     * 要删除的一组图片的ID列表。
     * 使用 @NotEmpty 注解确保列表不为空。
     */
    @NotEmpty(message = "图片ID列表不能为空")
    private List<String> ids;

    private static final long serialVersionUID = 1L;
}