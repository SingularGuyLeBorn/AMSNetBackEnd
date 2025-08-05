// FILE: src/main/java/com/scy/mytemplate/model/dto/folder/FolderCreateRequest.java
package com.scy.mytemplate.model.dto.folder;

import lombok.Data;
import java.io.Serializable;

/**
 * 创建新文件夹的请求体。
 *
 * @author Bedrock
 */
@Data
public class FolderCreateRequest implements Serializable {

    /**
     * 新文件夹的名称。
     */
    private String name;

    /**
     * 要创建的文件夹的空间类型。
     * 必须是 'private', 'organization', 'public' 中的一个。
     */
    private String space;

    /**
     * 当 space 为 'organization' 时，必须提供此字段，指定文件夹所属的组织ID。
     */
    private Long ownerOrganizationId;

    private static final long serialVersionUID = 1L;
}
// END OF FILE: src/main/java/com/scy/mytemplate/model/dto/folder/FolderCreateRequest.java