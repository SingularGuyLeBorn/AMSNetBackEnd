// FILE: src/main/java/com/scy/mytemplate/model/dto/folder/FolderUpdateRequest.java
package com.scy.mytemplate.model.dto.folder;

import lombok.Data;
import java.io.Serializable;

/**
 * 更新文件夹信息的请求体。
 *
 * @author Bedrock
 */
@Data
public class FolderUpdateRequest implements Serializable {

    /**
     * 要更新的文件夹的ID。
     */
    private Long folderId;

    /**
     * 文件夹的新名称。
     */
    private String newName;

    private static final long serialVersionUID = 1L;
}
// END OF FILE: src/main/java/com/scy/mytemplate/model/dto/folder/FolderUpdateRequest.java