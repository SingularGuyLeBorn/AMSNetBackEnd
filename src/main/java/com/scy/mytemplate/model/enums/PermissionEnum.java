// === 新增文件: src/main/java/com/scy/mytemplate/model/enums/PermissionEnum.java ===
package com.scy.mytemplate.model.enums;

/**
 * 权限枚举
 * 用于在权限检查中清晰地指定所需的操作权限。
 */
public enum PermissionEnum {
    /**
     * 读取权限。
     * 允许查看节点/关系信息。
     */
    READ,
    /**
     * 写入权限。
     * 允许创建、修改、删除节点/关系。
     */
    WRITE
}