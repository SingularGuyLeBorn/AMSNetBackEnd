package com.scy.mytemplate.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.io.Serializable;

@Data
public class FileEntity implements Serializable {
    /**
     * id
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long userId;        // 用户ID
    private String text;        // 用户输入的文字
    private String color;       // 用户选择的颜色
    private Long timestamp;     // 更改时间
    private byte[] imageFile;  // 图片文件

    public FileEntity() {
    }

    public FileEntity(Long userId, String text, String color, Long timestamp, byte[] imageFile) {
        this.userId = userId;
        this.text = text;
        this.color = color;
        this.timestamp = timestamp;
        this.imageFile = imageFile;
    }
}
