package com.scy.mytemplate.model.dto.image;

import lombok.Data;
import java.io.Serializable;

/**
 * 更新图片元数据的请求体。
 * 注意：此接口不用于替换图片文件本身，只用于修改数据库中的记录。
 */
@Data
public class ImageUpdateRequest implements Serializable {

    /**
     * 必填：要更新的图片的ID。
     */
    private String imageId;

    /**
     * 选填：图片的新名称。
     */
    private String originalFilename;

    // 未来可以扩展，比如添加图片描述
    // private String description;

    private static final long serialVersionUID = 1L;
}