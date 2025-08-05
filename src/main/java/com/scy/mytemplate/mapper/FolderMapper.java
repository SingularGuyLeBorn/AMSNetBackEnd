// FILE: src/main/java/com/scy/mytemplate/mapper/FolderMapper.java
package com.scy.mytemplate.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.scy.mytemplate.model.entity.Folder;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Folder 表的数据库操作接口。
 *
 * @author Bedrock
 */
public interface FolderMapper extends BaseMapper<Folder> {

    /**
     * 查找对当前用户可见的所有文件夹。
     * 这包括：所有公共文件夹、用户自己的私有文件夹、以及用户所属组织的所有组织文件夹。
     *
     * @param userId 当前用户ID
     * @return 可见文件夹的实体列表
     */
    List<Folder> findVisibleFoldersForUser(@Param("userId") Long userId);

}
// END OF FILE: src/main/java/com/scy/mytemplate/mapper/FolderMapper.java