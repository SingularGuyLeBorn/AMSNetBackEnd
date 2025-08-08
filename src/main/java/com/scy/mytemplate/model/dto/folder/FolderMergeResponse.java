// FILE: src/main/java/com/scy/mytemplate/model/dto/folder/FolderMergeResponse.java
package com.scy.mytemplate.model.dto.folder;

import com.scy.mytemplate.model.vo.FolderVO;
import com.scy.mytemplate.model.vo.UserVO;
import lombok.Data;
import java.io.Serializable;

/**
 * 组织管理员获取待合并文件夹列表的响应体。
 *
 * @author Bedrock
 */
@Data
public class FolderMergeResponse implements Serializable {

    /**
     * 文件夹信息
     */
    private FolderVO folder;

    /**
     * 发起请求的用户信息
     */
    private UserVO requestUser;

    private static final long serialVersionUID = 1L;
}
// END OF FILE: src/main/java/com/scy/mytemplate/model/dto/folder/FolderMergeResponse.java