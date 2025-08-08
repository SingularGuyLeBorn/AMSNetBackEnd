// FILE: src/main/java/com/scy/mytemplate/model/dto/folder/FolderCopyRequest.java
package com.scy.mytemplate.model.dto.folder;

import lombok.Data;
import java.io.Serializable;

/**
 * 复制文件夹的请求体。
 *
 * @author Bedrock
 */
@Data
public class FolderCopyRequest implements Serializable {

    /**
     * 要复制的源文件夹ID。
     */
    private String sourceFolderId;

    /**
     * 目标空间类型。
     * 必须是 'user_private' 或 'user_public'。
     */
    private String targetSpace;

    private static final long serialVersionUID = 1L;
}
// END OF FILE: src/main/java/com/scy/mytemplate/model/dto/folder/FolderCopyRequest.java