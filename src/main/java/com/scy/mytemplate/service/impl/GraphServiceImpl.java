// FILE: src/main/java/com/scy/mytemplate/service/impl/GraphServiceImpl.java
package com.scy.mytemplate.service.impl;

import com.scy.mytemplate.common.ErrorCode;
import com.scy.mytemplate.exception.BusinessException;
import com.scy.mytemplate.mapper.OrganizationMemberMapper;
import com.scy.mytemplate.model.dto.node.*;
import com.scy.mytemplate.model.dto.relationship.*;
import com.scy.mytemplate.model.entity.User;
import com.scy.mytemplate.model.enums.PermissionEnum;
import com.scy.mytemplate.model.enums.UserRoleEnum;
import com.scy.mytemplate.model.vo.NodeVO;
import com.scy.mytemplate.model.vo.RelationshipVO;
import com.scy.mytemplate.service.GraphService;
import com.scy.mytemplate.service.PermissionService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;
import org.neo4j.driver.types.Node;
import org.neo4j.driver.types.Relationship;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 统一的图服务实现类 (最终完整版)。
 * 实现了对 Neo4j 数据库中节点和关系的 CRUD 和 GetAll 操作，并将权限校验委托给 PermissionService。
 *
 * @author Bedrock
 */
@Service
@Slf4j
public class GraphServiceImpl implements GraphService {

    private final Driver driver;

    @Resource
    private PermissionService permissionService;

    @Resource
    private OrganizationMemberMapper organizationMemberMapper;

    @Autowired
    public GraphServiceImpl(Driver driver) {
        this.driver = driver;
    }

    // region 节点操作实现
    @Override
    public String createNode(NodeCreateRequest request, User currentUser) {
        String name = request.getName();
        if (StringUtils.isBlank(name)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "节点名称不能为空");
        }
        // 在创建时，我们假设权限已在调用此方法的服务中（如ImageService）校验过
        // permissionService.checkNodePermission(name, currentUser, PermissionEnum.WRITE);
        try (Session session = driver.session()) {
            boolean exists = session.readTransaction(tx -> tx.run("MATCH (n:CircuitNode {name: $name}) RETURN n", Map.of("name", name)).hasNext());
            if (exists) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "节点 '" + name + "' 已存在");
            }
            return session.writeTransaction(tx -> {
                String query = "CREATE (n:CircuitNode) SET n.name = $name, n += $props RETURN n.name";
                Result result = tx.run(query, Map.of("name", name, "props", request.getProperties()));
                return result.single().get(0).asString();
            });
        } catch (Exception e) {
            log.error("直接创建节点失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "创建节点时发生数据库错误: " + e.getMessage());
        }
    }

    @Override
    public String deleteNode(NodeDeleteRequest request, User currentUser) {
        permissionService.checkNodePermission(request.getName(), currentUser, PermissionEnum.WRITE);
        try (Session session = driver.session()) {
            session.writeTransaction(tx -> {
                tx.run("MATCH (n:CircuitNode {name: $name}) DETACH DELETE n", Map.of("name", request.getName()));
                return null;
            });
            return request.getName();
        } catch (Exception e) {
            log.error("删除节点失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "删除节点时发生数据库错误: " + e.getMessage());
        }
    }

    @Override
    public String updateNode(NodeUpdateRequest request, User currentUser) {
        permissionService.checkNodePermission(request.getName(), currentUser, PermissionEnum.WRITE);
        if ((request.getPropertiesToSet() == null || request.getPropertiesToSet().isEmpty()) && (request.getPropertiesToRemove() == null || request.getPropertiesToRemove().isEmpty())) {
            return request.getName();
        }
        try (Session session = driver.session()) {
            String updatedNodeName = session.writeTransaction(tx -> {
                StringBuilder queryBuilder = new StringBuilder("MATCH (n:CircuitNode {name: $name})");
                Map<String, Object> parameters = new HashMap<>();
                parameters.put("name", request.getName());
                if (request.getPropertiesToSet() != null && !request.getPropertiesToSet().isEmpty()) {
                    queryBuilder.append(" SET n += $propsToSet");
                    parameters.put("propsToSet", request.getPropertiesToSet());
                }
                if (request.getPropertiesToRemove() != null && !request.getPropertiesToRemove().isEmpty()) {
                    for (String keyToRemove : request.getPropertiesToRemove()) {
                        if (StringUtils.isNotBlank(keyToRemove)) {
                            queryBuilder.append(" REMOVE n.`").append(keyToRemove.replace("`", "``")).append("`");
                        }
                    }
                }
                queryBuilder.append(" RETURN n.name");
                Result result = tx.run(queryBuilder.toString(), parameters);
                if (!result.hasNext()) {
                    throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "要更新的节点 '" + request.getName() + "' 不存在");
                }
                return result.single().get(0).asString();
            });
            // 异步触发关系自动创建
            triggerAutoRelationshipCreation(updatedNodeName);
            return updatedNodeName;
        } catch (Exception e) {
            log.error("更新节点失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "更新节点时发生数据库错误: " + e.getMessage());
        }
    }

    @Override
    public NodeVO findNode(NodeQueryRequest request, User currentUser) {
        permissionService.checkNodePermission(request.getName(), currentUser, PermissionEnum.READ);
        try (Session session = driver.session()) {
            return session.readTransaction(tx -> {
                Result result = tx.run("MATCH (n:CircuitNode {name: $name}) RETURN n", Map.of("name", request.getName()));
                return result.stream()
                        .findFirst()
                        .map(record -> nodeToVO(record.get("n").asNode()))
                        .orElse(null);
            });
        } catch (Exception e) {
            log.error("查询节点失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "查询节点时发生数据库错误: " + e.getMessage());
        }
    }

    @Override
    public List<NodeVO> getAllNodesForCurrentUser(NodeGetAllRequest request, User currentUser) {
        if (currentUser == null) throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        try (Session session = driver.session()) {
            return session.readTransaction(tx -> {
                Result result;
                if (UserRoleEnum.ADMIN.getValue().equals(currentUser.getUserRole())) {
                    result = tx.run("MATCH (n:CircuitNode) RETURN n");
                } else {
                    List<String> orgIds = organizationMemberMapper.findUserOrganizationIds(currentUser.getId());
                    String query = "MATCH (n:CircuitNode) WHERE n.space = 'platform_public' OR (n.space IN ['user_private', 'user_public'] AND n.ownerUserId = $userId) OR (n.space = 'organization_public' AND n.ownerOrganizationId IN $orgIds) RETURN n";
                    Map<String, Object> params = Map.of("userId", currentUser.getId(), "orgIds", orgIds);
                    result = tx.run(query, params);
                }
                return result.stream()
                        .map(record -> nodeToVO(record.get("n").asNode()))
                        .collect(Collectors.toList());
            });
        } catch (Exception e) {
            log.error("获取全部节点失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "获取全部节点时发生数据库错误: " + e.getMessage());
        }
    }

    @Override
    public void updateNodePermissions(String nodeName, Map<String, Object> newPermissionProperties) {
        if (StringUtils.isBlank(nodeName) || newPermissionProperties == null || newPermissionProperties.isEmpty()) {
            return;
        }
        try (Session session = driver.session()) {
            session.writeTransaction(tx -> {
                String query = "MATCH (n:CircuitNode {name: $name}) SET n += $props";
                tx.run(query, Map.of("name", nodeName, "props", newPermissionProperties));
                return null;
            });
            log.info("成功更新节点 '{}' 的权限属性。", nodeName);
        } catch (Exception e) {
            log.error("更新节点 '{}' 权限属性失败。", nodeName, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "更新知识图谱节点权限失败");
        }
    }

    // endregion

    // region 关系操作实现
    @Override
    public String createRelationship(RelationshipCreateRequest request, User currentUser) {
        permissionService.checkNodePermission(request.getFromNode(), currentUser, PermissionEnum.WRITE);
        permissionService.checkNodePermission(request.getToNode(), currentUser, PermissionEnum.WRITE);
        String fromNode = request.getFromNode();
        String toNode = request.getToNode();
        String name = request.getName();
        if (StringUtils.isAnyBlank(fromNode, toNode, name)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "关系的起止节点和类型名称不能为空");
        }
        try (Session session = driver.session()) {
            return session.writeTransaction(tx -> {
                validateRelationshipName(name);
                String query = String.format("MATCH (a:CircuitNode {name: $fromNode}), (b:CircuitNode {name: $toNode}) CREATE (a)-[r:%s]->(b) SET r += $props RETURN type(r)", name);
                Map<String, Object> params = new HashMap<>();
                params.put("fromNode", fromNode);
                params.put("toNode", toNode);
                params.put("props", request.getProperties() == null ? new HashMap<>() : request.getProperties());
                Result result = tx.run(query, params);
                if (!result.hasNext()) throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "创建关系失败，一个或两个节点不存在");
                return result.single().get(0).asString();
            });
        } catch (Exception e) {
            log.error("创建关系失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "创建关系时发生数据库错误: " + e.getMessage());
        }
    }

    @Override
    public String deleteRelationship(RelationshipDeleteRequest request, User currentUser) {
        permissionService.checkNodePermission(request.getFromNode(), currentUser, PermissionEnum.WRITE);
        permissionService.checkNodePermission(request.getToNode(), currentUser, PermissionEnum.WRITE);
        String fromNode = request.getFromNode();
        String toNode = request.getToNode();
        String name = request.getName();
        try (Session session = driver.session()) {
            session.writeTransaction(tx -> {
                validateRelationshipName(name);
                String query = String.format("MATCH (a:CircuitNode {name: $fromNode})-[r:%s]->(b:CircuitNode {name: $toNode}) DELETE r", name);
                tx.run(query, Map.of("fromNode", fromNode, "toNode", toNode));
                return null;
            });
            return name;
        } catch (Exception e) {
            log.error("删除关系失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "删除关系时发生数据库错误: " + e.getMessage());
        }
    }

    @Override
    public String updateRelationship(RelationshipUpdateRequest request, User currentUser) {
        permissionService.checkNodePermission(request.getFromNode(), currentUser, PermissionEnum.WRITE);
        permissionService.checkNodePermission(request.getToNode(), currentUser, PermissionEnum.WRITE);
        String fromNode = request.getFromNode();
        String toNode = request.getToNode();
        String name = request.getName();
        try (Session session = driver.session()) {
            return session.writeTransaction(tx -> {
                validateRelationshipName(name);
                StringBuilder queryBuilder = new StringBuilder(String.format("MATCH (:CircuitNode {name: $fromNode})-[r:%s]->(:CircuitNode {name: $toNode})", name));
                Map<String, Object> parameters = new HashMap<>();
                parameters.put("fromNode", fromNode);
                parameters.put("toNode", toNode);
                if (request.getPropertiesToSet() != null && !request.getPropertiesToSet().isEmpty()) {
                    queryBuilder.append(" SET r += $propsToSet");
                    parameters.put("propsToSet", request.getPropertiesToSet());
                }
                if (request.getPropertiesToRemove() != null && !request.getPropertiesToRemove().isEmpty()) {
                    for (String key : request.getPropertiesToRemove()) {
                        if(StringUtils.isNotBlank(key)) queryBuilder.append(" REMOVE r.`").append(key.replace("`", "``")).append("`");
                    }
                }
                queryBuilder.append(" RETURN type(r)");
                Result result = tx.run(queryBuilder.toString(), parameters);
                if (!result.hasNext()) throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "要更新的关系不存在");
                return result.single().get(0).asString();
            });
        } catch (Exception e) {
            log.error("更新关系失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "更新关系时发生数据库错误: " + e.getMessage());
        }
    }

    @Override
    public RelationshipVO findRelationship(RelationshipQueryRequest request, User currentUser) {
        permissionService.checkNodePermission(request.getFromNode(), currentUser, PermissionEnum.READ);
        permissionService.checkNodePermission(request.getToNode(), currentUser, PermissionEnum.READ);
        String fromNode = request.getFromNode();
        String toNode = request.getToNode();
        String name = request.getName();
        try (Session session = driver.session()) {
            return session.readTransaction(tx -> {
                validateRelationshipName(name);
                String query = String.format("MATCH (a:CircuitNode {name: $fromNode})-[r:%s]->(b:CircuitNode {name: $toNode}) RETURN r, a.name as fromNodeName, b.name as toNodeName", name);
                Result result = tx.run(query, Map.of("fromNode", fromNode, "toNode", toNode));
                return result.stream()
                        .findFirst()
                        .map(record -> relationshipToVO(
                                record.get("r").asRelationship(),
                                record.get("fromNodeName").asString(),
                                record.get("toNodeName").asString()
                        ))
                        .orElse(null);
            });
        } catch (Exception e) {
            log.error("查询关系失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "查询关系时发生数据库错误: " + e.getMessage());
        }
    }

    @Override
    public List<RelationshipVO> getAllRelationshipsForCurrentUser(RelationshipGetAllRequest request, User currentUser) {
        if (currentUser == null) throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        List<NodeVO> visibleNodes = getAllNodesForCurrentUser(new NodeGetAllRequest(), currentUser);
        if (visibleNodes.isEmpty()) {
            return new ArrayList<>();
        }
        List<String> visibleNodeNames = visibleNodes.stream().map(NodeVO::getName).collect(Collectors.toList());
        try (Session session = driver.session()) {
            return session.readTransaction(tx -> {
                String relQuery = "MATCH (a:CircuitNode)-[r]->(b:CircuitNode) WHERE a.name IN $nodeNames AND b.name IN $nodeNames RETURN r, a.name AS fromNode, b.name AS toNode";
                Result result = tx.run(relQuery, Map.of("nodeNames", visibleNodeNames));
                return result.stream()
                        .map(record -> relationshipToVO(
                                record.get("r").asRelationship(),
                                record.get("fromNode").asString(),
                                record.get("toNode").asString()
                        ))
                        .collect(Collectors.toList());
            });
        } catch (Exception e) {
            log.error("获取全部关系失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "获取全部关系时发生数据库错误: " + e.getMessage());
        }
    }

    @Override
    public void triggerAutoRelationshipCreation(String nodeName) {
        // 实际项目中，这里应该是 @Async 异步执行
        log.info("开始为节点 '{}' 触发自动关系创建...", nodeName);
        try (Session session = driver.session()) {
            session.writeTransaction(tx -> {
                // 这个查询非常强大:
                // 1. 匹配触发节点
                // 2. UNWIND 其所有属性，变成 key-value 行
                // 3. 过滤掉我们不关心的属性（如name, space等）
                // 4. 对于每个有效的属性，匹配所有其他具有相同 key-value 的节点
                // 5. MERGE (合并) 关系，如果关系不存在则创建，避免重复
                String query =
                        "MATCH (a:CircuitNode {name: $nodeName}) " +
                                "UNWIND keys(a) AS key " +
                                "WITH a, key WHERE NOT key IN ['name', 'space', 'ownerUserId', 'ownerOrganizationId'] " +
                                "MATCH (b:CircuitNode) WHERE a <> b AND a[key] = b[key] " +
                                "WITH a, b, key " +
                                "CALL apoc.merge.relationship(a, 'SHARED_' + apoc.text.upperCamelCase(key), {}, {}, b) YIELD rel " +
                                "RETURN count(rel) AS createdRels";

                Result result = tx.run(query, Map.of("nodeName", nodeName));
                if (result.hasNext()) {
                    long createdCount = result.single().get("createdRels").asLong();
                    log.info("为节点 '{}' 自动创建/合并了 {} 条关系。", nodeName, createdCount);
                }
                return null;
            });
        } catch (Exception e) {
            log.error("为节点 '{}' 自动创建关系时发生错误", nodeName, e);
            // 在异步场景中，不应向上抛出业务异常，而是记录错误
        }
    }

    // endregion

    // region 辅助方法 (Helpers)
    private void validateRelationshipName(String name) {
        if (!name.matches("^[a-zA-Z_][a-zA-Z0-9_]*$")) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "关系名称不合法，只能包含字母、数字和下划线，且不能以数字开头。");
        }
    }

    private NodeVO nodeToVO(Node node) {
        Map<String, Object> properties = new HashMap<>(node.asMap());
        NodeVO vo = new NodeVO();
        vo.setName((String) properties.remove("name"));
        vo.setProperties(properties);
        return vo;
    }

    private RelationshipVO relationshipToVO(Relationship rel, String fromNodeName, String toNodeName) {
        Map<String, Object> properties = new HashMap<>(rel.asMap());
        RelationshipVO vo = new RelationshipVO();
        vo.setName(rel.type());
        vo.setFromNode(fromNodeName);
        vo.setToNode(toNodeName);
        vo.setProperties(properties);
        return vo;
    }
    // endregion
}
// END OF FILE: src/main/java/com/scy/mytemplate/service/impl/GraphServiceImpl.java