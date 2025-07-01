package com.scy.mytemplate.service.impl;

import com.scy.mytemplate.common.ErrorCode;
import com.scy.mytemplate.common.ResultUtils;
import com.scy.mytemplate.exception.BusinessException;
import com.scy.mytemplate.model.vo.NodeVO;
import com.scy.mytemplate.model.vo.RelationshipVO;
import com.scy.mytemplate.service.GraphService;
import lombok.extern.slf4j.Slf4j;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;
import org.neo4j.driver.Transaction;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Slf4j
public class GraphServiceImpl implements GraphService {

    private final Driver driver;
    public static final String NEO4J_SALT = "neo4jSalt";

    @Autowired
    public GraphServiceImpl(Driver driver) {
        this.driver = driver;
    }

    @Override
    public String createNode(String name, Map<String, Object> properties) {
        // 校验
        if (name == null || properties == null || name.isEmpty() || properties.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        // 检查节点是否已存在
        if (findNode(name) != null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "节点已存在");
        }
        synchronized (name.intern()) {
            try (Session session = driver.session(); Transaction tx = session.beginTransaction()) {
                // 构建属性字符串
                StringBuilder propertiesBuilder = new StringBuilder();
                for (Map.Entry<String, Object> entry : properties.entrySet()) {
                    // 转义属性名，避免特殊字符导致的问题
                    String key = entry.getKey().replace("\\", "\\\\").replace("'", "\\'");
                    // 根据属性值的类型来构建属性字符串
                    Object value = entry.getValue();
                    String valueString;
                    if (value instanceof String) {
                        valueString = "'" + value.toString().replace("\\", "\\\\").replace("'", "\\'") + "'";
                    } else {
                        valueString = value.toString();
                    }
                    propertiesBuilder.append(", ").append(key).append(": ").append(valueString);
                }
                String propertiesString = propertiesBuilder.length() > 0 ? propertiesBuilder.substring(2) : "";
                // 构建最终的Cypher查询
                String query = "CREATE (n:AMSNet {name: $name" + (propertiesString.isEmpty() ? "" : ", " + propertiesString) + "})";
                log.error("创建节点语句: {}", query);
                tx.run(query, Map.of("name", name));
                tx.commit(); // 确保事务被提交
                return name; // 创建节点后，可以直接返回节点名称
            } catch (Exception e) {
                log.error("创建节点失败", e);
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "创建节点失败，数据库错误");
            }
        }
    }


    @Override
    public String deleteNode(String name) {
        // 1. 校验
        if (name == null || name.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        // 2. 检查节点是否存在
        if (findNode(name) == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "节点不已存在");
        }
        // 3. 删除节点
        try (Session session = driver.session(); Transaction tx = session.beginTransaction()) {
            String query = "MATCH (n:AMSNet {name: $name}) DETACH DELETE n";
            tx.run(query, Map.of("name", name));
            tx.commit();
        } catch (Exception e) {
            log.error("删除节点失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "删除节点失败，数据库错误");
        }
        return name;
    }

    @Override
    public String updateNode(String name, Map<String, Object> newProperties) {
        // 1. 校验
        if (name == null || newProperties == null || name.isEmpty() || newProperties.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        // 2.  ·检查节点是否存在
        if (findNode(name) == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "节点不存在");
        }
        try (Session session = driver.session(); Transaction tx = session.beginTransaction()) {
            StringBuilder propertiesBuilder = new StringBuilder();
            for (Map.Entry<String, Object> entry : newProperties.entrySet()) {
                // 转义属性名和属性值，避免特殊字符导致的问题
                String key = entry.getKey().replace("\\", "\\\\").replace("'", "\\'");
                String value = entry.getValue().toString().replace("\\", "\\\\").replace("'", "\\'");
                propertiesBuilder.append(", ").append(key).append(": '").append(value).append("'");
            }
            String propertiesString = propertiesBuilder.toString();
            // 构建最终的Cypher查询
            String query = "MATCH (n:AMSNet {name: $name}) SET n += {name:$name" + propertiesString + "}";
            tx.run(query, Map.of("name", name));
            tx.commit();
        } catch (Exception e) {
            log.error("更新节点失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "更新节点失败，数据库错误");
        }
        return name;
    }

    @Override
    public NodeVO findNode(String name) {
        // 1. 校验
        if (name == null || name.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        try (Session session = driver.session(); Transaction tx = session.beginTransaction()) {
            String query = "MATCH (n:AMSNet {name: $name}) RETURN n";
            log.error("查询节点语句: {}", query);
            Result result = tx.run(query, Map.of("name", name));
            // 判断是否有查询结果
            if (result.hasNext()) {
                Map<String, Object> nodeQueryResultMap = result.single().get("n").asMap();// 是一个不可变视图，不能修改
                Map<String, Object> nodeMap = new HashMap<>(nodeQueryResultMap);
                NodeVO nodeVO = new NodeVO();
                nodeVO.setName((String) nodeMap.get("name"));
                // 移除name属性，避免重复设置
                nodeMap.remove("name");
                nodeVO.setProperties(nodeMap);
                return nodeVO;
            } else {
                // 未找到节点，返回null表示节点不存在
                log.error("查询节点失败，节点不存在");
                return null;
            }
        } catch (Exception e) {
            log.error("查询节点失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "查询节点失败，数据库错误");
        }
    }

    // 修改后的createRelationship方法
    @Override
    public String createRelationship(String name, Map<String, Object> properties) {
        // 1. 校验参数
        if (name == null || properties == null || name.isEmpty() || properties.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }

        String fromNode = (String) properties.get("fromNode");
        String toNode = (String) properties.get("toNode");

        if (fromNode == null || toNode == null || fromNode.isEmpty() || toNode.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "节点名称为空");
        }
        // 2. 校验是否from to 节点都存在
        if (findNode(fromNode) == null || findNode(toNode) == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "From OR To 节点不存在");
        }
        // 3. 创建关系
        try (Session session = driver.session(); Transaction tx = session.beginTransaction()) {
            StringBuilder propertiesBuilder = new StringBuilder();
            for (Map.Entry<String, Object> entry : properties.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue() instanceof String ? "'" + entry.getValue() + "'" : entry.getValue().toString();
                propertiesBuilder.append(", ").append(key).append(": ").append(value);
            }
            String propertiesString = propertiesBuilder.toString().replaceFirst(", ", "");

            String query = "MATCH (a:AMSNet {name: $fromNode}), (b:AMSNet {name:$toNode}) " +
                    "CREATE (a)-[r:" + name + " {" + propertiesString + "}]->(b)";
            tx.run(query, Map.of("fromNode", fromNode, "toNode", toNode));
            tx.commit();
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "创建关系失败，数据库错误");
        }
        return name;
    }


    @Override
    public String deleteRelationship(String name) {
        // 校验
        if (name == null || name.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "关系名称参数为空");
        }
        // 2. 校验关系是否存在
        if (findRelationship(name) == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "关系不存在");
        }
        try (Session session = driver.session(); Transaction tx = session.beginTransaction()) {
            String query = "MATCH ()-[r:" + name + "]-() DELETE r";
            tx.run(query);
            tx.commit();
        } catch (Exception e) {
            log.error("删除关系失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "删除关系失败，数据库错误");
        }
        return name;
    }

    // 由于更改neo4j的link改变node并不会更新link的连接，因此需要先删除关系再创建新关系
    @Override
    public String updateRelationship(String name, Map<String, Object> newProperties) {
        // 1. 校验参数
        if (name == null || newProperties == null || name.isEmpty() || newProperties.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        // 2. 校验关系是否存在
        if (findRelationship(name) == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "关系不存在");
        }
        // 3. 删除同名关系
        deleteRelationship(name);
        // 4. 创建新关系
        createRelationship(name, newProperties);
        return name;
    }


    @Override
    public RelationshipVO findRelationship(String name) {
        // 校验
        if (name == null || name.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "关系参数为空");
        }
        try (Session session = driver.session(); Transaction tx = session.beginTransaction()) {
            String query = "MATCH ()-[r:" + name + "]-() RETURN r LIMIT 1";
            Result result = tx.run(query);
            if (result.hasNext()) {
                Map<String, Object> relationshipQueryResultMap = result.single().get("r").asMap();// 是一个不可变视图，不能修改
                Map<String, Object> relationshipMap = new HashMap<>(relationshipQueryResultMap);
                RelationshipVO relationshipVO = new RelationshipVO();
                relationshipVO.setName((String) relationshipMap.get("name"));
                // 移除name属性，避免重复设置
                relationshipMap.remove("name");
                relationshipVO.setProperties(relationshipMap);
                return relationshipVO;
            } else {
                return null;
            }
        } catch (Exception e) {
            log.error("查询关系失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "查询关系失败，数据库错误");
        }
    }

    @Override
    public List<NodeVO> getAllNodes(boolean isIncludeProperties) {
        try (Session session = driver.session(); Transaction tx = session.beginTransaction()) {
            String query = "MATCH (n:AMSNet) RETURN n";
            Result result = tx.run(query);
            List<NodeVO> nodeVOs = new ArrayList<>();
            while (result.hasNext()) {
                Map<String, Object> nodeQueryResultMap = result.next().get("n").asMap();
                // 将不可变对象转为可变对象
                Map<String, Object> nodeMap = new HashMap<>(nodeQueryResultMap);
                NodeVO nodeVO = new NodeVO();
                nodeVO.setName((String) nodeMap.get("name"));
                // 根据是否包含属性来决定是否设置properties
                if (isIncludeProperties) {
                    // 移除name属性，避免重复设置
                    nodeMap.remove("name");
                    nodeVO.setProperties(nodeMap);
                }
                nodeVOs.add(nodeVO);
            }
            return nodeVOs;
        } catch (Exception e) {
            log.error("获取全部节点失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "获取全部节点失败，数据库错误");
        }
    }

    @Override
    public List<RelationshipVO> getAllRelationships(boolean isIncludeProperties) {
        try (Session session = driver.session(); Transaction tx = session.beginTransaction()) {
            // 修改后的查询语句，增加了返回关系的起始节点名称和结束节点名称
            String query = "MATCH ()-[r]->() RETURN type(r) AS name, r.fromNode AS fromNode, r.toNode AS toNode";
            Result result = tx.run(query);

            List<RelationshipVO> relationshipVOs = new ArrayList<>();
            while (result.hasNext()) {
                Map<String, Object> relationshipQueryResultMap = new HashMap<>(result.next().asMap());
                // 将不可变对象转为可变对象
                Map<String, Object> relationshipMap = new HashMap<>(relationshipQueryResultMap);
                RelationshipVO relationshipVO = new RelationshipVO();
                relationshipVO.setName((String) relationshipMap.get("name"));
                // 根据是否包含属性来决定是否设置properties
                if (isIncludeProperties) {
                    // 移除name属性，避免重复设置
                    relationshipMap.remove("name");
                    relationshipVO.setProperties(relationshipMap);
                }
                relationshipVOs.add(relationshipVO);
            }
            return relationshipVOs;
        } catch (Exception e) {
            log.error("获取全部关系失败");
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "获取全部关系失败，数据库错误");
        }
    }
}