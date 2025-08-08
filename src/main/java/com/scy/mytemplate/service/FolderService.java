// FILE: src/main/java/com/scy/mytemplate/service/FolderService.java
package com.scy.mytemplate.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.scy.mytemplate.model.dto.folder.FolderCopyRequest;
import com.scy.mytemplate.model.dto.folder.FolderCreateRequest;
import com.scy.mytemplate.model.dto.folder.FolderMergeRequest;
import com.scy.mytemplate.model.dto.folder.FolderMergeResponse;
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
    void deleteFolder(String folderId, User currentUser);

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

    /**
     * 复制一个文件夹及其所有内容到当前用户的个人空间。
     *
     * @param request     包含源文件夹ID和目标空间类型
     * @param currentUser 当前操作的用户
     * @return 复制后新生成的文件夹视图对象
     */
    FolderVO copyFolder(FolderCopyRequest request, User currentUser);

    /**
     * 用户从个人公共空间发起一个合并文件夹到组织的请求。
     *
     * @param request     包含要合并的文件夹ID和目标组织ID
     * @param currentUser 当前操作的用户
     */
    void requestMerge(FolderMergeRequest request, User currentUser);

    /**
     * 组织管理员获取所有待审批的合并请求列表。
     *
     * @param organizationId 组织ID
     * @param currentUser    当前操作的管理员用户
     * @return 待合并请求的详细信息列表
     */
    List<FolderMergeResponse> listPendingMerges(String organizationId, User currentUser);

    /**
     * 组织管理员批准一个文件夹合并请求。
     *
     * @param folderId    待合并的文件夹ID
     * @param currentUser 当前操作的管理员用户
     */
    void approveMergeRequest(String folderId, User currentUser);

    /**
     * 组织管理员拒绝一个文件夹合并请求。
     *
     * @param folderId    待合并的文件夹ID
     * @param currentUser 当前操作的管理员用户
     */
    void rejectMergeRequest(String folderId, User currentUser);
}
// END OF FILE: src/main/java/com/scy/mytemplate/service/FolderService.java