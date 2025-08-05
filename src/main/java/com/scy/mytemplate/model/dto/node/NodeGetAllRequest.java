// FILE: src/main/java/com/scy/mytemplate/model/dto/node/NodeGetAllRequest.java
package com.scy.mytemplate.model.dto.node;

import lombok.Data;
import java.io.Serializable;

/**
 * 获取当前用户可见的所有节点的请求体。
 * 目前为空，为将来可能添加的筛选、分页等参数预留扩展空间。
 */
@Data
public class NodeGetAllRequest implements Serializable {

    // 例如: private String filterByType;
    // 例如: private int pageNum = 1;
    // 例如: private int pageSize = 200;

    private static final long serialVersionUID = 1L;
}
// END OF FILE: src/main/java/com/scy/mytemplate/model/dto/node/NodeGetAllRequest.java