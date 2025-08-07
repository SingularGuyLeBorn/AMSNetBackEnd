// 文件路径: src/main/java/com/scy/mytemplate/model/vo/ImageVO.java
package com.scy.mytemplate.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.scy.mytemplate.model.entity.Image;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;
import java.util.Date;

/**
 * 图片视图对象 (Image View Object)。
 * 用于向前端安全地返回图片的元数据信息。
 *
 * @author Bedrock
 */
@Data
public class ImageVO implements Serializable {

    /**
     * 图片的唯一ID
     */
    private String id;

    /**
     * 所属文件夹的ID
     */
    private String folderId;

    /**
     * 原始文件名，用于前端展示
     */
    private String originalFilename;

    /**
     * 文件在服务器上的存储路径。
     * 前端可以根据这个路径拼接成完整的URL来访问或显示图片。
     */
    private String storagePath;

    /**
     * 图片宽度（像素）
     */
    private Integer width;

    /**
     * 图片高度（像素）
     */
    private Integer height;

    /**
     * 文件大小（字节）
     */
    private Long fileSize;

    /**
     * 上传者的用户ID
     */
    private String uploaderId;

    /**
     * 上传时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    /**
     * 将数据库实体对象 (Image) 转换为前端视图对象 (ImageVO) 的静态工厂方法。
     * 这种方式可以保持实体类的纯净，将转换逻辑封装在VO中。
     *
     * @param image 数据库查出的 Image 实体
     * @return 转换后的 ImageVO 对象
     */
    public static ImageVO fromEntity(Image image) {
        if (image == null) {
            return null;
        }
        ImageVO imageVO = new ImageVO();
        BeanUtils.copyProperties(image, imageVO);
        return imageVO;
    }

    private static final long serialVersionUID = 1L;
}