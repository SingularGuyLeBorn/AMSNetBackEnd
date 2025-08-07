// FILE: src/main/java/com/scy/mytemplate/service/ImageService.java
package com.scy.mytemplate.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.scy.mytemplate.model.dto.image.*;
import com.scy.mytemplate.model.entity.Image;
import com.scy.mytemplate.model.entity.User;
import com.scy.mytemplate.model.vo.ImageVO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public interface ImageService extends IService<Image> {

    ImageVO uploadImage(String folderId, MultipartFile file, User currentUser);

    List<ImageVO> uploadImagesBatch(String folderId, List<MultipartFile> files, User currentUser);

    /**
     * === 修正方法 ===
     * 批量上传图片及其关联的标注文件（JSON或TXT）。
     * @param folderId 目标文件夹ID
     * @param classMap 用于YOLO转换的类别映射
     * @param files 混合的文件列表 (images, jsons, txts)
     * @param currentUser 当前操作用户
     * @return 成功上传后的图片视图对象列表
     */
    List<ImageVO> uploadImagesWithAnnotationsBatch(String folderId, Map<Integer, String> classMap, List<MultipartFile> files, User currentUser);

    void deleteImage(String imageId, User currentUser);

    void deleteImagesBatch(ImageBatchDeleteRequest request, User currentUser);

    Page<ImageVO> listImagesByPage(ImageListRequest request, User currentUser);

    ImageVO getImageVOById(String imageId, User currentUser);

    ImageVO updateImage(ImageUpdateRequest request, User currentUser);

    ImageVO getImageVOByFolderAndName(ImageGetByNameRequest request, User currentUser);

    org.springframework.core.io.Resource downloadImage(String imageId, User currentUser);
}