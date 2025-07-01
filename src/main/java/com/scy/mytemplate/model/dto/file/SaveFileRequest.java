package com.scy.mytemplate.model.dto.file;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.io.Serializable;

@Data
public class SaveFileRequest implements Serializable {
    private Long userId;        // 用户ID
    private String text;        // 用户输入的文字
    private String color;       // 用户选择的颜色
    private Long timestamp;     // 更改时间
    private byte[] imageFile;  // 图片文件

    public SaveFileRequest() {
    }

    public SaveFileRequest(Long userId, String text, String color, Long timestamp, byte[] imageFile) {
        this.userId = userId;
        this.text = text;
        this.color = color;
        this.timestamp = timestamp;
        this.imageFile = imageFile;
    }
}
