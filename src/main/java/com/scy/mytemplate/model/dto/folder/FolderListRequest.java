// FILE: src/main/java/com/scy/mytemplate/model/dto/folder/FolderListRequest.java
package com.scy.mytemplate.model.dto.folder;

import lombok.Data;
import java.io.Serializable;

/**
 * 查询当前用户可见文件夹列表的请求体。
 * 为未来的筛选和分页功能预留。
 *
 * @author Bedrock
 */
@Data
public class FolderListRequest implements Serializable {

    // 例如: private String filterByName;

    private static final long serialVersionUID = 1L;
}
// END OF FILE: src/main/java/com/scy/mytemplate/model/dto/folder/FolderListRequest.java