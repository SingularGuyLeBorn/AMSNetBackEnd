// FILE: src/main/java/com/scy/mytemplate/controller/OrganizationAdminController.java
package com.scy.mytemplate.controller;

import com.scy.mytemplate.annotation.AuthCheck;
import com.scy.mytemplate.common.BaseResponse;
import com.scy.mytemplate.common.ResultUtils;
import com.scy.mytemplate.model.dto.folder.FolderMergeResponse;
import com.scy.mytemplate.model.entity.User;
import com.scy.mytemplate.service.FolderService;
import com.scy.mytemplate.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 组织管理员专属接口
 *
 * @author Bedrock
 */
@RestController
@RequestMapping("/admin/organization")
@Api(tags = "OrganizationAdminController")
public class OrganizationAdminController {

    @Resource
    private FolderService folderService;

    @Resource
    private UserService userService;

    @GetMapping("/merge/list")
    @ApiOperation("获取待合并的文件夹列表")
    public BaseResponse<List<FolderMergeResponse>> listPendingMerges(@RequestParam String organizationId, HttpServletRequest request) {
        User currentUser = userService.getLoginUser(request);
        List<FolderMergeResponse> pendingList = folderService.listPendingMerges(organizationId, currentUser);
        return ResultUtils.success(pendingList);
    }

    @PostMapping("/merge/approve")
    @ApiOperation("批准文件夹合并请求")
    public BaseResponse<Boolean> approveMergeRequest(@RequestParam String folderId, HttpServletRequest request) {
        User currentUser = userService.getLoginUser(request);
        folderService.approveMergeRequest(folderId, currentUser);
        return ResultUtils.success(true);
    }

    @PostMapping("/merge/reject")
    @ApiOperation("拒绝文件夹合并请求")
    public BaseResponse<Boolean> rejectMergeRequest(@RequestParam String folderId, HttpServletRequest request) {
        User currentUser = userService.getLoginUser(request);
        folderService.rejectMergeRequest(folderId, currentUser);
        return ResultUtils.success(true);
    }
}
// END OF FILE: src/main/java/com/scy/mytemplate/controller/OrganizationAdminController.java