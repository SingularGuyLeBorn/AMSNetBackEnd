// FILE: src/main/java/com/scy/mytemplate/service/FolderService.java
package com.scy.mytemplate.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.scy.mytemplate.model.dto.folder.FolderCreateRequest;
import com.scy.mytemplate.model.dto.folder.FolderUpdateRequest;
import com.scy.mytemplate.model.entity.Folder;
import com.scy.mytemplate.model.entity.User;
import com.scy.mytemplate.model.vo.FolderVO;

import java.util.List;

/**
 * 文件夹服务接口。
 *
 * @author Bedrock
 */
public interface FolderService extends IService<Folder> {

    /**
     * 创建一个新的文件夹。
     *
     * @param request     包含文件夹名称、空间类型和组织ID的请求。
     * @param currentUser 当前操作的用户。
     * @return 创建成功后的文件夹视图对象。
     */
    FolderVO createFolder(FolderCreateRequest request, User currentUser);

    /**
     * (逻辑)删除一个文件夹。
     *
     * @param folderId    要删除的文件夹ID。
     * @param currentUser 当前操作的用户。
     */
    void deleteFolder(Long folderId, User currentUser);

    /**
     * 更新文件夹名称。
     *
     * @param request     包含文件夹ID和新名称的请求。
     * @param currentUser 当前操作的用户。
     * @return 更新后的文件夹视图对象。
     */
    FolderVO updateFolder(FolderUpdateRequest request, User currentUser);

    /**
     * 获取当前用户有权查看的所有文件夹列表。
     *
     * @param currentUser 当前操作的用户。
     * @return 文件夹视图对象列表。
     */
    List<FolderVO> listFoldersForCurrentUser(User currentUser);
}
// END OF FILE: src/main/java/com/scy/mytemplate/service/FolderService.java