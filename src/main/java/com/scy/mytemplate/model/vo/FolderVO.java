// FILE: src/main/java/com/scy/mytemplate/model/vo/FolderVO.java
package com.scy.mytemplate.model.vo;

import com.scy.mytemplate.model.entity.Folder;
import lombok.Data;
import java.io.Serializable;
import java.util.Date;

/**
 * 文件夹视图对象 (Folder View Object)。
 * 用于向前端返回文件夹的详细信息。
 *
 * @author Bedrock
 */
@Data
public class FolderVO implements Serializable {

    private Long id;
    private String name;
    private String space;
    private Long ownerUserId;
    private Long ownerOrganizationId;
    private Date createTime;
    private Date updateTime;

    /**
     * 将实体对象转换为视图对象的静态工厂方法。
     * @param folder 文件夹实体对象
     * @return 文件夹视图对象
     */
    public static FolderVO fromEntity(Folder folder) {
        if (folder == null) {
            return null;
        }
        FolderVO folderVO = new FolderVO();
        folderVO.setId(folder.getId());
        folderVO.setName(folder.getName());
        folderVO.setSpace(folder.getSpace());
        folderVO.setOwnerUserId(folder.getOwnerUserId());
        folderVO.setOwnerOrganizationId(folder.getOwnerOrganizationId());
        folderVO.setCreateTime(folder.getCreateTime());
        folderVO.setUpdateTime(folder.getUpdateTime());
        return folderVO;
    }

    private static final long serialVersionUID = 1L;
}
// END OF FILE: src/main/java/com/scy/mytemplate/model/vo/FolderVO.java