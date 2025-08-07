package com.scy.mytemplate.model.dto.image;

import lombok.Data;
import java.io.Serializable;

/**
 * 获取单张图片详细信息的请求体。
 */
@Data
public class ImageGetRequest implements Serializable {

    /**
     * 必填：要获取的图片的ID。
     */
    private String imageId;

    private static final long serialVersionUID = 1L;
}