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

    /**
     * 上传单张图片。
     * @param folderId 目标文件夹ID
     * @param file 图片文件
     * @param currentUser 当前用户
     * @return 上传后的图片VO
     */
    ImageVO uploadImage(String folderId, MultipartFile file, User currentUser);

    /**
     * 批量上传图片及其关联的标注文件（JSON）。
     * @param folderId 目标文件夹ID
     * @param classMap (被忽略) 用于YOLO转换的类别映射
     * @param files 混合的文件列表 (images, jsons)
     * @param currentUser 当前操作用户
     * @return 成功上传后的图片视图对象列表
     */
    List<ImageVO> uploadImagesWithAnnotationsBatch(String folderId, Map<Integer, String> classMap, List<MultipartFile> files, User currentUser);

    /**
     * 删除单张图片及其关联数据。
     * @param imageId 图片ID
     * @param currentUser 当前用户
     */
    void deleteImage(String imageId, User currentUser);

    /**
     * 批量删除图片及其关联数据。
     * @param request 包含图片ID列表的请求
     * @param currentUser 当前用户
     */
    void deleteImagesBatch(ImageBatchDeleteRequest request, User currentUser);

    /**
     * 分页列出文件夹内的图片。
     * @param request 包含文件夹ID和分页信息的请求
     * @param currentUser 当前用户
     * @return 分页后的图片VO列表
     */
    Page<ImageVO> listImagesByPage(ImageListRequest request, User currentUser);

    /**
     * 根据ID获取单个图片VO。
     * @param imageId 图片ID
     * @param currentUser 当前用户
     * @return 图片VO
     */
    ImageVO getImageVOById(String imageId, User currentUser);

    /**
     * 更新图片元数据（如名称）。
     * @param request 包含图片ID和新元数据的请求
     * @param currentUser 当前用户
     * @return 更新后的图片VO
     */
    ImageVO updateImage(ImageUpdateRequest request, User currentUser);

    /**
     * 根据文件夹ID和文件名获取图片VO。
     * @param request 包含文件夹ID和文件名的请求
     * @param currentUser 当前用户
     * @return 图片VO
     */
    ImageVO getImageVOByFolderAndName(ImageGetByNameRequest request, User currentUser);

    /**
     * 下载图片文件。
     * @param imageId 图片ID
     * @param currentUser 当前用户
     * @return 图片文件资源
     */
    org.springframework.core.io.Resource downloadImage(String imageId, User currentUser);
}
// END OF FILE: src/main/java/com/scy/mytemplate/service/ImageService.java