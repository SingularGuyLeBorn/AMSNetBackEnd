package com.scy.mytemplate.service;

import com.scy.mytemplate.model.vo.NodeVO;
import com.scy.mytemplate.model.vo.RelationshipVO;

import java.util.List;
import java.util.Map;

public interface GraphService {
    /**
     * 创建节点（根据节点名称和属性）
     * @param name 节点名称
     * @param properties 节点属性
     * @return
     */
    String createNode(String name, Map<String, Object> properties);

    /**
     * 删除节点（根据节点名称）
     * @param name 节点名称
     * @return
     */
    String deleteNode(String name);

    /**
     * 更新节点（根据节点名称和新属性）
     * @param name 节点名称
     * @param newProperties 新属性
     * @return
     */
    String updateNode(String name, Map<String, Object> newProperties);

    /**
     * 查询节点（根据节点名称）
     * @param name
     * @return
     */
    NodeVO findNode(String name);

    /**
     * 创建关系（根据节点名称和关系属性）
     * @param name 关系名称
     * @param properties 关系属性
     * @return
     */
    String createRelationship(String name, Map<String, Object> properties);

    /**
     * 删除关系（根据关系名称）
     * @param name
     * @return
     */
    String deleteRelationship(String name);

    /**
     * 更新关系（根据关系名称和新属性）
     * @param name
     * @param newProperties
     * @return
     */
    String updateRelationship(String name, Map<String, Object> newProperties);

    /**
     * 查询关系（根据关系名称）
     * @param name
     * @return
     */
    RelationshipVO findRelationship(String name);

    /**
     * 获取全部节点
     * @param isIncludeProperties
     * @return
     */
    List<NodeVO> getAllNodes(boolean isIncludeProperties);

    /**
     *获取全部关系
     * @param isIncludeProperties
     * @return
     */
    List<RelationshipVO> getAllRelationships(boolean isIncludeProperties);
}