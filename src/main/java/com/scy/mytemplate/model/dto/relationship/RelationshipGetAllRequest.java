package com.scy.mytemplate.model.dto.relationship;

import lombok.Data;

/**
 * 用于封装节点创建请求的参数对象。
 * 该类提供了一种结构化的方式来传递节点创建所需的信息，包括节点名称和节点属性。
 */
@Data
public class RelationshipGetAllRequest {

    /**
     * 是否包含属性
     */
    boolean isIncludeProperties;
}
