-- FILE: V8_Recreate_Post_Related_Tables.sql
-- DESCRIPTION: Recreates the post, post_favour, and post_thumb tables with UUID-compatible schemas.
-- This script should be run if these tables were accidentally dropped.

USE my_db; -- 确保在正确的数据库中执行

-- ===================================================================
-- 1. 创建 帖子表 (post)
-- ===================================================================
CREATE TABLE `post` (
                        `id` VARCHAR(36) NOT NULL COMMENT '帖子主键ID (UUID)',
                        `title` VARCHAR(512) NOT NULL COMMENT '标题',
                        `content` TEXT COMMENT '内容',
                        `tags` VARCHAR(1024) COMMENT '标签列表 (JSON格式的字符串)',
                        `thumbNum` INT NOT NULL DEFAULT '0' COMMENT '点赞数',
                        `favourNum` INT NOT NULL DEFAULT '0' COMMENT '收藏数',
                        `userId` VARCHAR(36) NOT NULL COMMENT '创建用户ID (外键, 指向user.id)',
                        `createTime` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                        `updateTime` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                        `isDelete` TINYINT NOT NULL DEFAULT '0' COMMENT '逻辑删除标志 (0-未删, 1-已删)',
                        PRIMARY KEY (`id`),
                        KEY `idx_userId` (`userId`) COMMENT '创建者索引，方便查询用户帖子'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='帖子表';


-- ===================================================================
-- 2. 创建 帖子收藏表 (post_favour)
-- ===================================================================
CREATE TABLE `post_favour` (
                               `id` VARCHAR(36) NOT NULL COMMENT '收藏记录主键ID (UUID)',
                               `postId` VARCHAR(36) NOT NULL COMMENT '帖子ID (外键, 指向post.id)',
                               `userId` VARCHAR(36) NOT NULL COMMENT '创建用户ID (外键, 指向user.id)',
                               `createTime` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                               `updateTime` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                               PRIMARY KEY (`id`),
                               UNIQUE KEY `uk_postId_userId` (`postId`, `userId`) COMMENT '联合唯一索引，防止用户重复收藏同一个帖子',
                               KEY `idx_userId` (`userId`) COMMENT '用户索引，方便查询用户的收藏列表'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='帖子收藏表';


-- ===================================================================
-- 3. 创建 帖子点赞表 (post_thumb)
-- ===================================================================
CREATE TABLE `post_thumb` (
                              `id` VARCHAR(36) NOT NULL COMMENT '点赞记录主键ID (UUID)',
                              `postId` VARCHAR(36) NOT NULL COMMENT '帖子ID (外键, 指向post.id)',
                              `userId` VARCHAR(36) NOT NULL COMMENT '创建用户ID (外键, 指向user.id)',
                              `createTime` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                              `updateTime` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                              PRIMARY KEY (`id`),
                              UNIQUE KEY `uk_postId_userId` (`postId`, `userId`) COMMENT '联合唯一索引，防止用户重复点赞同一个帖子',
                              KEY `idx_userId` (`userId`) COMMENT '用户索引，方便查询用户的点赞列表'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='帖子点赞表';

-- 提示：三张表已成功创建！