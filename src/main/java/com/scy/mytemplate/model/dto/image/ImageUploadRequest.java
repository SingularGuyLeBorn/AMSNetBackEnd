// FILE: src/main/java/com/scy/mytemplate/model/dto/image/ImageUploadRequest.java
package com.scy.mytemplate.model.dto.image;

import lombok.Data;
import java.io.Serializable;

/**
 * 图片上传请求的数据传输对象。
 * 注意：实际的图片文件将通过 multipart/form-data 形式传递，
 * 这个类只用于传递额外的元数据，如目标文件夹ID。
 *
 * @author Bedrock
 */
@Data
public class ImageUploadRequest implements Serializable {

    /**
     * 目标文件夹的 ID。
     * 指示图片应该被上传到哪个工作区/文件夹中。
     */
    private Long folderId;

    private static final long serialVersionUID = 1L;
}
// END OF FILE: src/main/java/com/scy/mytemplate/model/dto/image/ImageUploadRequest.java