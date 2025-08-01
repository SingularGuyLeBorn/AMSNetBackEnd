-- 切换到您的数据库
USE my_db;

-- ===================================================================
-- 1. 组织表 (Organizations)
-- 定义每个组织的基本信息。
-- ===================================================================
CREATE TABLE IF NOT EXISTS Organizations
(
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE COMMENT '组织名称，必须唯一',
    ownerId BIGINT NOT NULL COMMENT '组织的创建者/拥有者 ID',
    description VARCHAR(512) NULL COMMENT '组织简介',
    createTime DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updateTime DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL ON UPDATE CURRENT_TIMESTAMP,
    isDelete TINYINT DEFAULT 0 NOT NULL,
    FOREIGN KEY (ownerId) REFERENCES user(id) ON DELETE RESTRICT -- 组织创建者不能轻易删除
) COMMENT '组织表' COLLATE = utf8mb4_unicode_ci;


-- ===================================================================
-- 2. 组织成员表 (OrganizationMembers)
-- 这是一个连接表，用于管理用户和组织的多对多关系。
-- ===================================================================
CREATE TABLE IF NOT EXISTS OrganizationMembers
(
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    organizationId BIGINT NOT NULL COMMENT '组织 ID',
    userId BIGINT NOT NULL COMMENT '用户 ID',
    -- 可以在组织内定义角色，如 'admin', 'member'，实现更细的权限
    roleInOrg VARCHAR(255) DEFAULT 'member' NOT NULL COMMENT '成员在组织内的角色',
    joinTime DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL,
    UNIQUE KEY uk_org_user (organizationId, userId), -- 确保一个用户在一个组织里只有一条记录
    FOREIGN KEY (organizationId) REFERENCES Organizations(id) ON DELETE CASCADE,
    FOREIGN KEY (userId) REFERENCES user(id) ON DELETE CASCADE
) COMMENT '组织成员关系表' COLLATE = utf8mb4_unicode_ci;


-- ===================================================================
-- 3. 文件夹表 (Folders)
-- 这是新架构的核心，取代了您原有的“工作区”概念。
-- ===================================================================
CREATE TABLE IF NOT EXISTS Folders
(
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL COMMENT '文件夹名称',
    -- 'space' 字段清晰地定义了文件夹的类型
    space ENUM('public', 'private', 'organization') NOT NULL COMMENT '空间类型',
    -- 这两个外键是 nullable 的，根据 space 类型的不同，只有一个会生效
    ownerUserId BIGINT NULL COMMENT '私有文件夹的拥有者 (当 space="private")',
    ownerOrganizationId BIGINT NULL COMMENT '组织文件夹的拥有者 (当 space="organization")',
    createTime DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updateTime DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL ON UPDATE CURRENT_TIMESTAMP,
    isDelete TINYINT DEFAULT 0 NOT NULL,
    FOREIGN KEY (ownerUserId) REFERENCES user(id) ON DELETE CASCADE,
    FOREIGN KEY (ownerOrganizationId) REFERENCES Organizations(id) ON DELETE CASCADE,
    -- 添加一个约束，确保私有文件夹必须有用户，组织文件夹必须有组织
    CONSTRAINT chk_folder_owner CHECK (
        (space = 'private' AND ownerUserId IS NOT NULL AND ownerOrganizationId IS NULL) OR
        (space = 'organization' AND ownerOrganizationId IS NOT NULL AND ownerUserId IS NULL) OR
        (space = 'public' AND ownerUserId IS NULL AND ownerOrganizationId IS NULL)
        )
) COMMENT '文件夹表' COLLATE = utf8mb4_unicode_ci;


-- ===================================================================
-- 4. 图片元数据表 (Images)
-- 用于存储每个图片文件的信息，不再使用 BLOB。
-- ===================================================================
CREATE TABLE IF NOT EXISTS Images
(
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    folderId BIGINT NOT NULL COMMENT '所属文件夹 ID',
    originalFilename VARCHAR(255) NOT NULL COMMENT '原始文件名',
    -- 修正: 将 VARCHAR(1024) 缩短为 VARCHAR(512) 以解决索引长度问题
    storagePath VARCHAR(512) NOT NULL UNIQUE COMMENT '文件存储的唯一路径/Key',
    width INT NULL COMMENT '图片宽度',
    height INT NULL COMMENT '图片高度',
    fileSize BIGINT NULL COMMENT '文件大小 (bytes)',
    uploaderId BIGINT NOT NULL COMMENT '上传者ID',
    createTime DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL,
    isDelete TINYINT DEFAULT 0 NOT NULL,
    FOREIGN KEY (folderId) REFERENCES Folders(id) ON DELETE CASCADE,
    FOREIGN KEY (uploaderId) REFERENCES user(id) ON DELETE RESTRICT
) COMMENT '图片元数据表' COLLATE = utf8mb4_unicode_ci;


-- ===================================================================
-- 5. 标注表 (Annotations)
-- 用于存储每张图片对应的标注数据。
-- ===================================================================
CREATE TABLE IF NOT EXISTS Annotations
(
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    imageId BIGINT NOT NULL UNIQUE COMMENT '对应的图片 ID (一对一关系)',
    -- 使用 JSON 类型可以更高效地查询和操作标注内容
    jsonContent JSON NULL COMMENT '标注数据 (JSON 格式)',
    lastEditorId BIGINT NULL COMMENT '最后修改者 ID',
    updateTime DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (imageId) REFERENCES Images(id) ON DELETE CASCADE,
    FOREIGN KEY (lastEditorId) REFERENCES user(id) ON DELETE SET NULL
) COMMENT '标注数据表' COLLATE = utf8mb4_unicode_ci;