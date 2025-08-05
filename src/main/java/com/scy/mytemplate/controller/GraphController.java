// FILE: src/main/java/com/scy/mytemplate/controller/GraphController.java
package com.scy.mytemplate.controller;

import com.scy.mytemplate.common.BaseResponse;
import com.scy.mytemplate.common.ErrorCode;
import com.scy.mytemplate.common.ResultUtils;
import com.scy.mytemplate.exception.BusinessException;
import com.scy.mytemplate.model.dto.node.*;
import com.scy.mytemplate.model.dto.relationship.*;
import com.scy.mytemplate.model.entity.User;
import com.scy.mytemplate.model.vo.NodeVO;
import com.scy.mytemplate.model.vo.RelationshipVO;
import com.scy.mytemplate.service.GraphService;
import com.scy.mytemplate.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 图谱操作统一接口
 * 提供了对知识图谱中节点和关系的增、删、改、查 (CRUD) 以及获取全部 (GetAll) 功能。
 * 所有操作都会进行严格的身份验证和权限校验。
 *
 * @author Bedrock
 */
@RestController
@RequestMapping("/graph")
@Slf4j
public class GraphController {

    @Resource
    private GraphService graphService;

    @Resource
    private UserService userService;

    // region 节点接口 (Node Endpoints)

    @PostMapping("/node/create")
    public BaseResponse<String> createNode(@RequestBody NodeCreateRequest createRequest, HttpServletRequest request) {
        if (createRequest == null) throw new BusinessException(ErrorCode.PARAMS_ERROR);
        User currentUser = userService.getLoginUser(request);
        String result = graphService.createNode(createRequest, currentUser);
        return ResultUtils.success(result);
    }

    @PostMapping("/node/delete")
    public BaseResponse<String> deleteNode(@RequestBody NodeDeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null) throw new BusinessException(ErrorCode.PARAMS_ERROR);
        User currentUser = userService.getLoginUser(request);
        String nodeName = graphService.deleteNode(deleteRequest, currentUser);
        return ResultUtils.success("节点 '" + nodeName + "' 已成功删除");
    }

    @PostMapping("/node/update")
    public BaseResponse<String> updateNode(@RequestBody NodeUpdateRequest updateRequest, HttpServletRequest request) {
        if (updateRequest == null) throw new BusinessException(ErrorCode.PARAMS_ERROR);
        User currentUser = userService.getLoginUser(request);
        String result = graphService.updateNode(updateRequest, currentUser);
        return ResultUtils.success(result);
    }

    @PostMapping("/node/find")
    public BaseResponse<NodeVO> findNode(@RequestBody NodeQueryRequest queryRequest, HttpServletRequest request) {
        if (queryRequest == null) throw new BusinessException(ErrorCode.PARAMS_ERROR);
        User currentUser = userService.getLoginUser(request);
        NodeVO nodeVO = graphService.findNode(queryRequest, currentUser);
        if (nodeVO == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "节点未找到或您没有权限访问");
        }
        return ResultUtils.success(nodeVO);
    }

    @PostMapping("/node/getAll")
    public BaseResponse<List<NodeVO>> getAllNodes(@RequestBody NodeGetAllRequest getAllRequest, HttpServletRequest request) {
        if (getAllRequest == null) throw new BusinessException(ErrorCode.PARAMS_ERROR);
        User currentUser = userService.getLoginUser(request);
        List<NodeVO> nodes = graphService.getAllNodesForCurrentUser(getAllRequest, currentUser);
        return ResultUtils.success(nodes);
    }

    // endregion

    // region 关系接口 (Relationship Endpoints)

    @PostMapping("/relationship/create")
    public BaseResponse<String> createRelationship(@RequestBody RelationshipCreateRequest createRequest, HttpServletRequest request) {
        if (createRequest == null) throw new BusinessException(ErrorCode.PARAMS_ERROR);
        User currentUser = userService.getLoginUser(request);
        String result = graphService.createRelationship(createRequest, currentUser);
        return ResultUtils.success(result);
    }

    @PostMapping("/relationship/delete")
    public BaseResponse<String> deleteRelationship(@RequestBody RelationshipDeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null) throw new BusinessException(ErrorCode.PARAMS_ERROR);
        User currentUser = userService.getLoginUser(request);
        String relName = graphService.deleteRelationship(deleteRequest, currentUser);
        return ResultUtils.success("关系 '" + relName + "' 已成功删除");
    }

    @PostMapping("/relationship/update")
    public BaseResponse<String> updateRelationship(@RequestBody RelationshipUpdateRequest updateRequest, HttpServletRequest request) {
        if (updateRequest == null) throw new BusinessException(ErrorCode.PARAMS_ERROR);
        User currentUser = userService.getLoginUser(request);
        String result = graphService.updateRelationship(updateRequest, currentUser);
        return ResultUtils.success(result);
    }

    @PostMapping("/relationship/find")
    public BaseResponse<RelationshipVO> findRelationship(@RequestBody RelationshipQueryRequest queryRequest, HttpServletRequest request) {
        if (queryRequest == null) throw new BusinessException(ErrorCode.PARAMS_ERROR);
        User currentUser = userService.getLoginUser(request);
        RelationshipVO vo = graphService.findRelationship(queryRequest, currentUser);
        if (vo == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "关系未找到或您没有权限访问");
        }
        return ResultUtils.success(vo);
    }

    @PostMapping("/relationship/getAll")
    public BaseResponse<List<RelationshipVO>> getAllRelationships(@RequestBody RelationshipGetAllRequest getAllRequest, HttpServletRequest request) {
        if (getAllRequest == null) throw new BusinessException(ErrorCode.PARAMS_ERROR);
        User currentUser = userService.getLoginUser(request);
        List<RelationshipVO> relationships = graphService.getAllRelationshipsForCurrentUser(getAllRequest, currentUser);
        return ResultUtils.success(relationships);
    }

    // endregion
}
// END OF FILE: src/main/java/com/scy/mytemplate/controller/GraphController.java