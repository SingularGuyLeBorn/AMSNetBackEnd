// FILE: src/main/java/com/scy/mytemplate/service/GraphService.java
package com.scy.mytemplate.service;

import com.scy.mytemplate.model.dto.node.*;
import com.scy.mytemplate.model.dto.relationship.*;
import com.scy.mytemplate.model.entity.User;
import com.scy.mytemplate.model.vo.NodeVO;
import com.scy.mytemplate.model.vo.RelationshipVO;
import java.util.List;
import java.util.Map;

/**
 * 统一的图服务接口。
 * 定义了对知识图谱中节点和关系的所有操作，并集成了权限校验。
 *
 * @author Bedrock
 */
public interface GraphService {

    // region 节点操作 (Node Operations)

    String createNode(NodeCreateRequest request, User currentUser);
    String deleteNode(NodeDeleteRequest request, User currentUser);
    String updateNode(NodeUpdateRequest request, User currentUser);
    NodeVO findNode(NodeQueryRequest request, User currentUser);
    List<NodeVO> getAllNodesForCurrentUser(NodeGetAllRequest request, User currentUser);

    /**
     * 更新知识图谱节点的权限相关属性。
     *
     * @param nodeName 节点名称
     * @param newPermissionProperties 新的权限属性Map (e.g., space, ownerOrganizationId)
     */
    void updateNodePermissions(String nodeName, Map<String, Object> newPermissionProperties);

    // endregion

    // region 关系操作 (Relationship Operations)

    String createRelationship(RelationshipCreateRequest request, User currentUser);
    String deleteRelationship(RelationshipDeleteRequest request, User currentUser);
    String updateRelationship(RelationshipUpdateRequest request, User currentUser);
    RelationshipVO findRelationship(RelationshipQueryRequest request, User currentUser);
    List<RelationshipVO> getAllRelationshipsForCurrentUser(RelationshipGetAllRequest request, User currentUser);

    /**
     * 触发自动化关系创建的逻辑。
     * @param nodeName 刚刚被创建或更新的节点名称
     */
    void triggerAutoRelationshipCreation(String nodeName);

    // endregion
}
// END OF FILE: src/main/java/com/scy/mytemplate/service/GraphService.java