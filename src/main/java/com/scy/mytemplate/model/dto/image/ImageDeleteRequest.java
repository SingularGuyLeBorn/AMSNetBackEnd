// FILE: src/main/java/com/scy/mytemplate/model/dto/image/ImageDeleteRequest.java
package com.scy.mytemplate.model.dto.image;

import lombok.Data;
import java.io.Serializable;

/**
 * 删除图片的请求体。
 *
 * @author Bedrock
 */
@Data
public class ImageDeleteRequest implements Serializable {

    /**
     * 要删除的图片的ID。
     */
    private String imageId;

    private static final long serialVersionUID = 1L;
}
// END OF FILE: src/main/java/com/scy/mytemplate/model/dto/image/ImageDeleteRequest.java