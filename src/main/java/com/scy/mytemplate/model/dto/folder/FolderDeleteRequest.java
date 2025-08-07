// FILE: src/main/java/com/scy/mytemplate/model/dto/folder/FolderDeleteRequest.java
package com.scy.mytemplate.model.dto.folder;

import lombok.Data;
import java.io.Serializable;

/**
 * 删除文件夹的请求体。
 *
 * @author Bedrock
 */
@Data
public class FolderDeleteRequest implements Serializable {

    /**
     * 要删除的文件夹的ID。
     */
    private String folderId;

    private static final long serialVersionUID = 1L;
}
// END OF FILE: src/main/java/com/scy/mytemplate/model/dto/folder/FolderDeleteRequest.java