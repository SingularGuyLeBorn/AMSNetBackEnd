// FILE: src/main/java/com/scy/mytemplate/model/dto/folder/FolderMergeRequest.java
package com.scy.mytemplate.model.dto.folder;

import lombok.Data;
import java.io.Serializable;

/**
 * 请求合并文件夹到组织的请求体。
 *
 * @author Bedrock
 */
@Data
public class FolderMergeRequest implements Serializable {

    /**
     * 用户个人公共空间中要发起合并请求的文件夹ID。
     */
    private String folderId;

    /**
     * 目标组织的ID。
     */
    private String targetOrganizationId;

    private static final long serialVersionUID = 1L;
}
// END OF FILE: src/main/java/com/scy/mytemplate/model/dto/folder/FolderMergeRequest.java