// FILE: src/main/java/com/scy/mytemplate/service/ImageService.java
package com.scy.mytemplate.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.scy.mytemplate.model.entity.Image;
import com.scy.mytemplate.model.entity.User;
import org.springframework.web.multipart.MultipartFile;

/**
 * 图片服务接口。
 *
 * @author Bedrock
 */
public interface ImageService extends IService<Image> {

    /**
     * 处理图片上传。
     *
     * @param folderId    目标文件夹的ID。
     * @param file        上传的图片文件。
     * @param currentUser 当前操作的用户。
     * @return 成功保存后的 Image 实体对象。
     */
    Image uploadImage(Long folderId, MultipartFile file, User currentUser);

    /**
     * (逻辑)删除一张图片。
     *
     * @param imageId     要删除的图片ID。
     * @param currentUser 当前操作的用户。
     */
    void deleteImage(Long imageId, User currentUser);
}
// END OF FILE: src/main/java/com/scy/mytemplate/service/ImageService.java