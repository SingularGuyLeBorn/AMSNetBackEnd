// FILE: src/main/java/com/scy/mytemplate/model/dto/relationship/RelationshipGetAllRequest.java
package com.scy.mytemplate.model.dto.relationship;

import lombok.Data;
import java.io.Serializable;

/**
 * 获取当前用户可见的所有关系的请求体。
 * 目前为空，为将来可能添加的筛选、分页等参数预留扩展空间。
 */
@Data
public class RelationshipGetAllRequest implements Serializable {

    // 例如: private String filterByType;

    private static final long serialVersionUID = 1L;
}
// END OF FILE: src/main/java/com/scy/mytemplate/model/dto/relationship/RelationshipGetAllRequest.java