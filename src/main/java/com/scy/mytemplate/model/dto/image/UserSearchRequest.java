package com.scy.mytemplate.model.dto.image;

import lombok.Data;

import java.io.Serializable;

// 放在 model/dto/user/ 包下
@Data
public class UserSearchRequest implements Serializable {
    // 搜索的关键词 (可以是用户账号、昵称等)
    private String keyword;
}
