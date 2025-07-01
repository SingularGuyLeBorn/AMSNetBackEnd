package com.scy.mytemplate.controller;

import com.scy.mytemplate.common.BaseResponse;
import com.scy.mytemplate.common.ErrorCode;
import com.scy.mytemplate.common.ResultUtils;
import com.scy.mytemplate.exception.BusinessException;
import com.scy.mytemplate.model.dto.node.*;
import com.scy.mytemplate.model.dto.relationship.*;
import com.scy.mytemplate.model.entity.Node;
import com.scy.mytemplate.model.vo.NodeVO;
import com.scy.mytemplate.model.vo.RelationshipVO;
import com.scy.mytemplate.service.GraphService;
import lombok.extern.slf4j.Slf4j;
import javax.annotation.Resource;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.BeanUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/graph")
@Slf4j
public class GraphController {

    @Resource
    private GraphService graphService;

    // 添加节点
    @PostMapping("/createNode")
    public BaseResponse<String> createNode(@RequestBody NodeCreateRequest nodeCreateRequest) {
        String name = nodeCreateRequest.getName();
        Map<String, Object> properties = nodeCreateRequest.getProperties();
        if (nodeCreateRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        String result = graphService.createNode(name, properties);
        return ResultUtils.success(result);
    }

    // 删除节点
    @DeleteMapping("/deleteNode")
    public BaseResponse<String> deleteNode(@RequestBody NodeDeleteRequest nodeDeleteRequest) {
        String name = nodeDeleteRequest.getName();
        // 先查询节点是否存在
        NodeVO existingNode = graphService.findNode(name);
        if (existingNode == null) {
            log.error("节点不存在，无法删除，name: " + name);
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "节点不存在，无法删除");
        }
        String nodeToDelete = graphService.deleteNode(name);
        return ResultUtils.success("Node:" + nodeToDelete + " deleted successfully");
    }

    // 更新节点
    @PutMapping("/updateNode")
    public BaseResponse<String> updateNode(@RequestBody NodeUpdateRequest nodeUpdateRequest) {
        if (nodeUpdateRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        String name = nodeUpdateRequest.getName();
        Map<String, Object> properties = nodeUpdateRequest.getProperties();
        String result = graphService.updateNode(name, properties);
        return ResultUtils.success(result);
    }

    // 查询节点
    @PostMapping("/findNode")
    public BaseResponse<NodeVO> findNode(@RequestBody NodeQueryRequest nodeQueryRequest) {
        if (nodeQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        String name = nodeQueryRequest.getName();
        NodeVO existingNode = graphService.findNode(name);
        if (existingNode == null) {
            log.error("节点不存在，name: " + name);
            return null;
        }
        return ResultUtils.success(existingNode);
    }

    // 创建关系
    @PostMapping("/createRelationship")
    public BaseResponse<String> createRelationship(@RequestBody RelationshipCreateRequest relationshipCreateRequest) {
        String name = relationshipCreateRequest.getName();
        Map<String, Object> properties = relationshipCreateRequest.getProperties();

        String fromNode = (String) properties.get("fromNode");
        String toNode = (String) properties.get("toNode");

        // 先查询起始节点是否存在
        NodeVO startNode = graphService.findNode(fromNode);
        if (startNode == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "起始节点不存在，无法创建关系");
        }

        // 先查询结束节点是否存在
        NodeVO endNode = graphService.findNode(toNode);
        if (endNode == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "结束节点不存在，无法创建关系");
        }

        // 再查询关系是否已存在（可根据业务需求决定是否添加此步）
        RelationshipVO existingRelationship = graphService.findRelationship(name);
        Map<String, Object> existingRelationshipProperties = existingRelationship.getProperties();
        String existingFromNode = (String) existingRelationshipProperties.get("fromNode");
        String existingToNode = (String) existingRelationshipProperties.get("toNode");
        if (existingRelationship != null &&  existingFromNode.equals(fromNode) && existingToNode.equals(toNode)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "关系已存在，无法创建");
        }
        String result = graphService.createRelationship(name, properties);
        return ResultUtils.success(result);
    }

    // 删除关系
    @DeleteMapping("/deleteRelationship")
    public BaseResponse<String> deleteRelationship(@RequestBody RelationshipDeleteRequest relationshipDeleteRequest) {
        String name = relationshipDeleteRequest.getName();

        // 先查询关系是否存在
        RelationshipVO existingRelationship = graphService.findRelationship(name);
        if (existingRelationship == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "关系不存在，无法删除");
        }
        String relationshipToDelete = graphService.deleteRelationship(name);
        return ResultUtils.success("Relationship:" + relationshipToDelete + " deleted successfully");
    }

    // 更新关系
    @PutMapping("/updateRelationship")
    public BaseResponse<String> updateRelationship(@RequestBody RelationshipUpdateRequest relationshipUpdateRequest) {
        if (relationshipUpdateRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        String name = relationshipUpdateRequest.getName();
        Map<String, Object> newProperties = relationshipUpdateRequest.getProperties();
        // 先查询关系是否存在
        RelationshipVO existingRelationship = graphService.findRelationship(name);
        if (existingRelationship == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "关系不存在，无法更新");
        }
        String result = graphService.updateRelationship(name, newProperties);
        return ResultUtils.success(result);
    }

    // 查询关系
    @PostMapping("/findRelationship")
    public BaseResponse<RelationshipVO> findRelationship(@RequestBody RelationshipQueryRequest relationshipQueryRequest) {
        if (relationshipQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        String name = relationshipQueryRequest.getName();
        // 先查询关系是否存在
        RelationshipVO existingRelationship = graphService.findRelationship(name);
        if (existingRelationship == null) {
            log.error("关系不存在，name: " + name);
            return null;
        }
        return ResultUtils.success(existingRelationship);
    }

    // 获取全部节点
    @PostMapping("/getAllNodes")
    public BaseResponse<List<NodeVO>> getAllNodes(@RequestBody NodeGetAllRequest nodeGetAllRequest) {
        boolean isIncludeProperties = nodeGetAllRequest.isIncludeProperties();
        List<NodeVO> nodes = graphService.getAllNodes(isIncludeProperties);
        log.error("Nodes: " + nodes);
        return ResultUtils.success(nodes);
    }

    // 获取全部关系
    @PostMapping("/getAllRelationships")
    public BaseResponse<List<RelationshipVO>> getAllRelationships(@RequestBody RelationshipGetAllRequest relationshipGetAllRequest) {
        boolean isIncludeProperties = relationshipGetAllRequest.isIncludeProperties();
        List<RelationshipVO> relationships = graphService.getAllRelationships(isIncludeProperties);
        log.error("Relationships: " + relationships);
        return ResultUtils.success(relationships);
    }
}