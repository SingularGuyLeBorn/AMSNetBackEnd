// 文件路径: src/main/java/com/scy/mytemplate/model/dto/image/ImageGetByNameRequest.java
package com.scy.mytemplate.model.dto.image;

import lombok.Data;
import java.io.Serializable;

@Data
public class ImageGetByNameRequest implements Serializable {
    private String folderId;
    private String originalFilename; // 我们通过原始文件名来查找
    private static final long serialVersionUID = 1L;
}