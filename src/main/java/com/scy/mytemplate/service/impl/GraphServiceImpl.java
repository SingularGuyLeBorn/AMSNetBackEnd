// FILE: src/main/java/com/scy/mytemplate/service/impl/GraphServiceImpl.java
package com.scy.mytemplate.service.impl;

import com.scy.mytemplate.common.ErrorCode;
import com.scy.mytemplate.exception.BusinessException;
import com.scy.mytemplate.mapper.FolderMapper;
import com.scy.mytemplate.mapper.ImageMapper;
import com.scy.mytemplate.mapper.OrganizationMemberMapper;
import com.scy.mytemplate.model.dto.node.*;
import com.scy.mytemplate.model.dto.relationship.*;
import com.scy.mytemplate.model.entity.Folder;
import com.scy.mytemplate.model.entity.Image;
import com.scy.mytemplate.model.entity.User;
import com.scy.mytemplate.model.enums.PermissionEnum;
import com.scy.mytemplate.model.enums.UserRoleEnum;
import com.scy.mytemplate.model.vo.NodeVO;
import com.scy.mytemplate.model.vo.RelationshipVO;
import com.scy.mytemplate.service.GraphService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;
import org.neo4j.driver.exceptions.NoSuchRecordException;
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
 * 统一的图服务实现类。
 * 实现了对 Neo4j 数据库中节点和关系的 CRUD 和 GetAll 操作，并内置了严格的权限校验逻辑。
 *
 * @author Bedrock
 */
@Service
@Slf4j
public class GraphServiceImpl implements GraphService {

    private final Driver driver;

    @Resource
    private ImageMapper imageMapper;
    @Resource
    private FolderMapper folderMapper;
    @Resource
    private OrganizationMemberMapper memberMapper;

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
        Image image = imageMapper.findByStoragePath(name);
        if (image == null) throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "创建节点失败：关联的图片资源不存在");
        Folder folder = folderMapper.selectById(image.getFolderId());
        if (folder == null) throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "创建节点失败：图片所属的文件夹不存在");
        checkPermissionByFolder(folder, currentUser, PermissionEnum.WRITE);

        try (Session session = driver.session()) {
            boolean exists = session.readTransaction(tx -> tx.run("MATCH (n:CircuitNode {name: $name}) RETURN n", Map.of("name", name)).hasNext());
            if (exists) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "节点 '" + name + "' 已存在，无法重复创建");
            }

            return session.writeTransaction(tx -> {
                Map<String, Object> finalProperties = new HashMap<>();
                if (request.getProperties() != null) {
                    finalProperties.putAll(request.getProperties());
                }
                finalProperties.put("space", folder.getSpace());
                finalProperties.put("ownerUserId", folder.getOwnerUserId());
                finalProperties.put("ownerOrganizationId", folder.getOwnerOrganizationId());

                String query = "CREATE (n:CircuitNode) SET n.name = $name, n += $props RETURN n.name";
                Result result = tx.run(query, Map.of("name", name, "props", finalProperties));
                return result.single().get(0).asString();
            });
        } catch (Exception e) {
            log.error("创建节点失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "创建节点时发生数据库错误: " + e.getMessage());
        }
    }

    @Override
    public String deleteNode(NodeDeleteRequest request, User currentUser) {
        String name = request.getName();
        checkPermission(name, currentUser, PermissionEnum.WRITE);
        try (Session session = driver.session()) {
            session.writeTransaction(tx -> {
                tx.run("MATCH (n:CircuitNode {name: $name}) DETACH DELETE n", Map.of("name", name));
                return null;
            });
            return name;
        } catch (Exception e) {
            log.error("删除节点失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "删除节点时发生数据库错误: " + e.getMessage());
        }
    }

    @Override
    public String updateNode(NodeUpdateRequest request, User currentUser) {
        String name = request.getName();
        checkPermission(name, currentUser, PermissionEnum.WRITE);
        if ((request.getPropertiesToSet() == null || request.getPropertiesToSet().isEmpty()) && (request.getPropertiesToRemove() == null || request.getPropertiesToRemove().isEmpty())) {
            return name;
        }
        try (Session session = driver.session()) {
            return session.writeTransaction(tx -> {
                StringBuilder queryBuilder = new StringBuilder("MATCH (n:CircuitNode {name: $name})");
                Map<String, Object> parameters = new HashMap<>();
                parameters.put("name", name);
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
                    throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "要更新的节点 '" + name + "' 不存在");
                }
                return result.single().get(0).asString();
            });
        } catch (Exception e) {
            log.error("更新节点失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "更新节点时发生数据库错误: " + e.getMessage());
        }
    }

    @Override
    public NodeVO findNode(NodeQueryRequest request, User currentUser) {
        String name = request.getName();
        checkPermission(name, currentUser, PermissionEnum.READ);
        try (Session session = driver.session()) {
            return session.readTransaction(tx -> {
                Result result = tx.run("MATCH (n:CircuitNode {name: $name}) RETURN n", Map.of("name", name));
                // 使用函数式流处理，避免直接操作 Record 对象
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
                    List<Long> orgIds = memberMapper.findUserOrganizationIds(currentUser.getId());
                    String query = "MATCH (n:CircuitNode) WHERE n.space = 'public' OR (n.space = 'private' AND n.ownerUserId = $userId) OR (n.space = 'organization' AND n.ownerOrganizationId IN $orgIds) RETURN n";
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
    // endregion

    // region 关系操作实现
    @Override
    public String createRelationship(RelationshipCreateRequest request, User currentUser) {
        String fromNode = request.getFromNode();
        String toNode = request.getToNode();
        String name = request.getName();
        if (StringUtils.isAnyBlank(fromNode, toNode, name)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "关系的起止节点和类型名称不能为空");
        }
        checkPermission(fromNode, currentUser, PermissionEnum.WRITE);
        checkPermission(toNode, currentUser, PermissionEnum.WRITE);
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
        String fromNode = request.getFromNode();
        String toNode = request.getToNode();
        String name = request.getName();
        checkPermission(fromNode, currentUser, PermissionEnum.WRITE);
        checkPermission(toNode, currentUser, PermissionEnum.WRITE);
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
        String fromNode = request.getFromNode();
        String toNode = request.getToNode();
        String name = request.getName();
        checkPermission(fromNode, currentUser, PermissionEnum.WRITE);
        checkPermission(toNode, currentUser, PermissionEnum.WRITE);
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
        String fromNode = request.getFromNode();
        String toNode = request.getToNode();
        String name = request.getName();
        checkPermission(fromNode, currentUser, PermissionEnum.READ);
        checkPermission(toNode, currentUser, PermissionEnum.READ);
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
    // endregion

    // region 辅助方法 (Helpers)
    private void checkPermission(String nodeName, User user, PermissionEnum requiredPermission) {
        if (user == null) throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        if (UserRoleEnum.ADMIN.getValue().equals(user.getUserRole())) return;
        Image image = imageMapper.findByStoragePath(nodeName);
        if (image == null) throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "资源不存在或已被删除: " + nodeName);
        Folder folder = folderMapper.selectById(image.getFolderId());
        if (folder == null) throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "资源所属的文件夹不存在");
        checkPermissionByFolder(folder, user, requiredPermission);
    }

    private void checkPermissionByFolder(Folder folder, User user, PermissionEnum requiredPermission) {
        switch (folder.getSpace()) {
            case "public":
                if (requiredPermission == PermissionEnum.WRITE) {
                    throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权修改公共空间内容");
                }
                break;
            case "organization":
                Long orgId = folder.getOwnerOrganizationId();
                String roleInOrg = memberMapper.findUserRoleInOrg(user.getId(), orgId);
                if (roleInOrg == null) {
                    throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "您不属于该组织，无权访问");
                }
                if (requiredPermission == PermissionEnum.WRITE && !"admin".equals(roleInOrg)) {
                    throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "您不是组织管理员，无权修改此内容");
                }
                break;
            case "private":
                if (!folder.getOwnerUserId().equals(user.getId())) {
                    throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权访问他人的私人空间");
                }
                break;
            default:
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "未知的空间类型，权限检查失败");
        }
    }

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