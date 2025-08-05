// FILE: src/main/java/com/scy/mytemplate/mapper/ImageMapper.java
package com.scy.mytemplate.mapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.scy.mytemplate.model.entity.Image;
import org.apache.ibatis.annotations.Param;
/**
 Image 表的数据库操作接口。
 @author Bedrock
 */
public interface ImageMapper extends BaseMapper<Image> {
    /**
     根据文件存储路径查找图片元数据。
     */
    Image findByStoragePath(@Param("storagePath") String storagePath);
}
// END OF FILE: src/main/java/com/scy/mytemplate/mapper/ImageMapper.java