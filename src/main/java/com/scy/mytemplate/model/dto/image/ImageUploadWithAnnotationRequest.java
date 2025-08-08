// FILE: src/main/java/com/scy/mytemplate/model/dto/image/ImageUploadWithAnnotationRequest.java
package com.scy.mytemplate.model.dto.image;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.io.Serializable;
import java.util.List;

/**
 * 批量上传图片及标注文件的请求体。
 *
 * @author Bedrock
 */
@Data
public class ImageUploadWithAnnotationRequest implements Serializable {

    private String folderId;

    // 前端传递的 classMap JSON 字符串，后端接收但忽略
    private String classMap;

    private List<MultipartFile> files;

    private static final long serialVersionUID = 1L;
}
// END OF FILE: src/main/java/com/scy/mytemplate/model/dto/image/ImageUploadWithAnnotationRequest.java