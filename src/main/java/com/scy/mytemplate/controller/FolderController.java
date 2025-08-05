// FILE: src/main/java/com/scy/mytemplate/controller/FolderController.java
package com.scy.mytemplate.controller;

import com.scy.mytemplate.common.BaseResponse;
import com.scy.mytemplate.common.ErrorCode;
import com.scy.mytemplate.common.ResultUtils;
import com.scy.mytemplate.exception.BusinessException;
import com.scy.mytemplate.model.dto.folder.FolderCreateRequest;
import com.scy.mytemplate.model.dto.folder.FolderDeleteRequest;
import com.scy.mytemplate.model.dto.folder.FolderUpdateRequest;
import com.scy.mytemplate.model.entity.User;
import com.scy.mytemplate.model.vo.FolderVO;
import com.scy.mytemplate.service.FolderService;
import com.scy.mytemplate.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 文件夹/工作区管理接口。
 *
 * @author Bedrock
 */
@RestController
@RequestMapping("/folder")
@Api(tags = "folderController")
@Slf4j
public class FolderController {

    @Resource
    private FolderService folderService;

    @Resource
    private UserService userService;

    /**
     * 创建新文件夹
     */
    @PostMapping("/create")
    @ApiOperation(value = "创建新文件夹")
    public BaseResponse<FolderVO> createFolder(@RequestBody FolderCreateRequest createRequest, HttpServletRequest request) {
        User currentUser = userService.getLoginUser(request);
        FolderVO folderVO = folderService.createFolder(createRequest, currentUser);
        return ResultUtils.success(folderVO);
    }

    /**
     * 删除文件夹
     */
    @PostMapping("/delete")
    @ApiOperation(value = "删除文件夹（逻辑删除）")
    public BaseResponse<Boolean> deleteFolder(@RequestBody FolderDeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getFolderId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User currentUser = userService.getLoginUser(request);
        folderService.deleteFolder(deleteRequest.getFolderId(), currentUser);
        return ResultUtils.success(true);
    }

    /**
     * 更新文件夹
     */
    @PostMapping("/update")
    @ApiOperation(value = "更新文件夹名称")
    public BaseResponse<FolderVO> updateFolder(@RequestBody FolderUpdateRequest updateRequest, HttpServletRequest request) {
        if (updateRequest == null || updateRequest.getFolderId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User currentUser = userService.getLoginUser(request);
        FolderVO folderVO = folderService.updateFolder(updateRequest, currentUser);
        return ResultUtils.success(folderVO);
    }

    /**
     * 获取当前用户可见的文件夹列表
     */
    @GetMapping("/list/my")
    @ApiOperation(value = "获取我的文件夹列表")
    public BaseResponse<List<FolderVO>> listMyFolders(HttpServletRequest request) {
        User currentUser = userService.getLoginUser(request);
        List<FolderVO> folderList = folderService.listFoldersForCurrentUser(currentUser);
        return ResultUtils.success(folderList);
    }
}
// END OF FILE: src/main/java/com/scy/mytemplate/controller/FolderController.java