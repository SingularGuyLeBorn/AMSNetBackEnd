package com.scy.mytemplate.model.dto.image;

import com.scy.mytemplate.common.PageRequest; // 假设您有一个通用的分页请求基类
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.io.Serializable;

/**
 * 获取文件夹内图片列表的请求体。
 */
@Data
@EqualsAndHashCode(callSuper = true) // 如果继承了PageRequest
public class ImageListRequest extends PageRequest implements Serializable {

    /**
     * 必填：要查询的文件夹ID。
     */
    private String folderId;

    /**
     * 选填：根据原始文件名进行模糊搜索的关键词。
     */
    private String searchKeyword;

    private static final long serialVersionUID = 1L;
}