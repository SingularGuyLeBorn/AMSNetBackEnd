-- ===================================================================
-- 脚本：从零开始创建所有表，ID 字段类型为 VARCHAR(36)
-- 特性：幂等设计 (IF NOT EXISTS)，可安全重复执行。
-- ===================================================================

-- 步骤 1: 创建并选择数据库 (如果尚不存在)
CREATE DATABASE IF NOT EXISTS `my_db` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `my_db`;

-- 步骤 2: 创建所有表

-- `user` 表
CREATE TABLE IF NOT EXISTS `user` (
                                      `id` VARCHAR(36) NOT NULL COMMENT '主键ID',
                                      `userAccount` VARCHAR(256) NOT NULL COMMENT '账号',
                                      `userPassword` VARCHAR(512) NOT NULL COMMENT '密码',
                                      `unionId` VARCHAR(256) DEFAULT NULL COMMENT '微信开放平台id',
                                      `mpOpenId` VARCHAR(256) DEFAULT NULL COMMENT '公众号openId',
                                      `userName` VARCHAR(256) DEFAULT NULL COMMENT '用户昵称',
                                      `userAvatar` VARCHAR(1024) DEFAULT NULL COMMENT '用户头像',
                                      `userProfile` VARCHAR(512) DEFAULT NULL COMMENT '用户简介',
                                      `userRole` VARCHAR(256) NOT NULL DEFAULT 'user' COMMENT '用户角色：user/admin/ban',
                                      `createTime` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                      `updateTime` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                      `isDelete` TINYINT NOT NULL DEFAULT '0' COMMENT '是否删除',
                                      PRIMARY KEY (`id`),
                                      UNIQUE KEY `uk_userAccount` (`userAccount`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- `Organizations` 表
CREATE TABLE IF NOT EXISTS `Organizations` (
                                               `id` VARCHAR(36) NOT NULL COMMENT '组织ID',
                                               `name` VARCHAR(256) NOT NULL COMMENT '组织名称',
                                               `ownerId` VARCHAR(36) NOT NULL COMMENT '组织创建者/拥有者ID',
                                               `description` VARCHAR(1024) DEFAULT NULL COMMENT '组织简介',
                                               `createTime` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                               `updateTime` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                               `isDelete` TINYINT NOT NULL DEFAULT '0' COMMENT '逻辑删除标志',
                                               PRIMARY KEY (`id`),
                                               UNIQUE KEY `uk_org_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='组织表';

-- `OrganizationMembers` 表
CREATE TABLE IF NOT EXISTS `OrganizationMembers` (
                                                     `id` VARCHAR(36) NOT NULL COMMENT '主键ID',
                                                     `organizationId` VARCHAR(36) NOT NULL COMMENT '组织ID',
                                                     `userId` VARCHAR(36) NOT NULL COMMENT '用户ID',
                                                     `roleInOrg` VARCHAR(50) NOT NULL COMMENT '成员在组织内的角色: ''admin'', ''member''',
                                                     `joinTime` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '加入时间',
                                                     PRIMARY KEY (`id`),
                                                     UNIQUE KEY `uk_orgId_userId` (`organizationId`,`userId`),
                                                     KEY `idx_userId` (`userId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='组织成员关系表';

-- `Folders` 表
CREATE TABLE IF NOT EXISTS `Folders` (
                                         `id` VARCHAR(36) NOT NULL COMMENT '主键ID',
                                         `name` VARCHAR(255) NOT NULL COMMENT '文件夹名称',
                                         `space` VARCHAR(50) NOT NULL COMMENT '空间类型: ''public'', ''private'', ''organization''',
                                         `ownerUserId` VARCHAR(36) DEFAULT NULL COMMENT '私有文件夹的拥有者用户ID',
                                         `ownerOrganizationId` VARCHAR(36) DEFAULT NULL COMMENT '组织文件夹的拥有者组织ID',
                                         `createTime` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                         `updateTime` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                         `isDelete` TINYINT NOT NULL DEFAULT '0' COMMENT '逻辑删除标志',
                                         PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文件夹/工作区表';

-- `Images` 表
CREATE TABLE IF NOT EXISTS `Images` (
                                        `id` VARCHAR(36) NOT NULL COMMENT '主键ID',
                                        `folderId` VARCHAR(36) NOT NULL COMMENT '所属文件夹ID',
                                        `originalFilename` VARCHAR(255) NOT NULL COMMENT '原始文件名',
                                        `storagePath` VARCHAR(512) NOT NULL COMMENT '文件在存储中的唯一路径/Key',
                                        `width` INT DEFAULT NULL COMMENT '图片宽度（像素）',
                                        `height` INT DEFAULT NULL COMMENT '图片高度（像素）',
                                        `fileSize` BIGINT DEFAULT NULL COMMENT '文件大小（字节）',
                                        `uploaderId` VARCHAR(36) NOT NULL COMMENT '上传者用户ID',
                                        `createTime` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                        `isDelete` TINYINT NOT NULL DEFAULT '0' COMMENT '逻辑删除标志',
                                        PRIMARY KEY (`id`),
                                        UNIQUE KEY `uk_storagePath` (`storagePath`),
                                        KEY `idx_folderId` (`folderId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='图片元数据表';

-- `Annotations` 表
CREATE TABLE IF NOT EXISTS `Annotations` (
                                             `id` VARCHAR(36) NOT NULL COMMENT '主键ID',
                                             `imageId` VARCHAR(36) NOT NULL COMMENT '对应的图片ID',
                                             `jsonContent` JSON COMMENT '标注数据 (JSON 格式)',
                                             `lastEditorId` VARCHAR(36) NOT NULL COMMENT '最后修改者ID',
                                             `updateTime` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                             PRIMARY KEY (`id`),
                                             UNIQUE KEY `uk_imageId` (`imageId`) COMMENT '确保一张图片只有一个标注记录'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='标注数据表';

-- `FileEntity` 表 (根据 Mapper XML 推断)
CREATE TABLE IF NOT EXISTS `FileEntity` (
                                            `id` VARCHAR(36) NOT NULL COMMENT '主键ID',
                                            `user_id` VARCHAR(36) DEFAULT NULL COMMENT '用户ID',
                                            `text` VARCHAR(255) DEFAULT NULL COMMENT '用户输入的文字',
                                            `color` VARCHAR(50) DEFAULT NULL COMMENT '用户选择的颜色',
                                            `timestamp` BIGINT DEFAULT NULL COMMENT '更改时间戳',
                                            `image_file` LONGBLOB COMMENT '图片文件二进制内容',
                                            PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文件实体表';

-- `post` 表
CREATE TABLE IF NOT EXISTS `post` (
                                      `id` VARCHAR(36) NOT NULL COMMENT '帖子主键ID',
                                      `title` VARCHAR(512) NOT NULL COMMENT '标题',
                                      `content` TEXT COMMENT '内容',
                                      `tags` VARCHAR(1024) COMMENT '标签列表 (JSON格式的字符串)',
                                      `thumbNum` INT NOT NULL DEFAULT '0' COMMENT '点赞数',
                                      `favourNum` INT NOT NULL DEFAULT '0' COMMENT '收藏数',
                                      `userId` VARCHAR(36) NOT NULL COMMENT '创建用户ID',
                                      `createTime` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                      `updateTime` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                      `isDelete` TINYINT NOT NULL DEFAULT '0' COMMENT '逻辑删除标志 (0-未删, 1-已删)',
                                      PRIMARY KEY (`id`),
                                      KEY `idx_userId` (`userId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='帖子表';

-- `post_favour` 表
CREATE TABLE IF NOT EXISTS `post_favour` (
                                             `id` VARCHAR(36) NOT NULL COMMENT '收藏记录主键ID',
                                             `postId` VARCHAR(36) NOT NULL COMMENT '帖子ID',
                                             `userId` VARCHAR(36) NOT NULL COMMENT '创建用户ID',
                                             `createTime` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                             `updateTime` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                             PRIMARY KEY (`id`),
                                             UNIQUE KEY `uk_postId_userId` (`postId`, `userId`),
                                             KEY `idx_userId_fav` (`userId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='帖子收藏表';

-- `post_thumb` 表
CREATE TABLE IF NOT EXISTS `post_thumb` (
                                            `id` VARCHAR(36) NOT NULL COMMENT '点赞记录主键ID',
                                            `postId` VARCHAR(36) NOT NULL COMMENT '帖子ID',
                                            `userId` VARCHAR(36) NOT NULL COMMENT '创建用户ID',
                                            `createTime` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                            `updateTime` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                            PRIMARY KEY (`id`),
                                            UNIQUE KEY `uk_postId_userId` (`postId`, `userId`),
                                            KEY `idx_userId_thumb` (`userId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='帖子点赞表';

-- 提示：数据库表已成功创建！