// FILE: src/main/java/com/scy/mytemplate/model/vo/AnnotationVO.java
package com.scy.mytemplate.model.vo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.scy.mytemplate.model.entity.Annotation;
import lombok.Data;
import lombok.SneakyThrows;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

/**
 * 标注数据视图对象。
 * 用于向前端返回格式化后的标注信息。
 *
 * @author Bedrock
 */
@Data
public class AnnotationVO implements Serializable {

    private Long id;
    private Long imageId;

    /**
     * 将数据库中的 JSON 字符串反序列化为 Map 对象，方便前端直接使用。
     */
    private Map<String, Object> jsonContent;
    private Long lastEditorId;
    private Date updateTime;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 将实体对象转换为视图对象的静态工厂方法。
     *
     * @param annotation 标注实体对象
     * @return 标注视图对象
     */
    @SneakyThrows // LOMBOK 注解，自动处理 json 解析的受查异常
    public static AnnotationVO fromEntity(Annotation annotation) {
        if (annotation == null) {
            return null;
        }
        AnnotationVO vo = new AnnotationVO();
        vo.setId(annotation.getId());
        vo.setImageId(annotation.getImageId());
        vo.setLastEditorId(annotation.getLastEditorId());
        vo.setUpdateTime(annotation.getUpdateTime());
        if (annotation.getJsonContent() != null && !annotation.getJsonContent().isEmpty()) {
            vo.setJsonContent(objectMapper.readValue(annotation.getJsonContent(), Map.class));
        }
        return vo;
    }

    private static final long serialVersionUID = 1L;
}
// END OF FILE: src/main/java/com/scy/mytemplate/model/vo/AnnotationVO.java