-- FILE: V4_Reset_Workspace_Data.sql
-- DESCRIPTION: This script will reset all workspace-related data while preserving the user table.
-- It first DELETES data from tables in the correct order to respect foreign key constraints,
-- and then INSERTS the standard seed data back into them.

-- -------------------------------------------------------------------
-- 请确保在执行此脚本前，您的 `user` 表中存在 id 为 1, 2, 3 的用户。
-- 如果不存在，请先执行上一个脚本 (V3) 来创建这些示例用户。
-- -------------------------------------------------------------------

-- 切换到您的数据库
USE my_db;


-- ===================================================================
-- Part 1: 清空所有相关表的数据 (保留 user 表)
-- 删除顺序与外键依赖关系相反，从子表开始删除，最后删除父表。
-- ===================================================================

-- 1. 清空 Annotations 表 (依赖 Images)
DELETE FROM `Annotations`;

-- 2. 清空 Images 表 (依赖 Folders)
DELETE FROM `Images`;

-- 3. 清空 OrganizationMembers 表 (依赖 Organizations)
DELETE FROM `OrganizationMembers`;

-- 4. 清空 Folders 表 (依赖 Organizations)
DELETE FROM `Folders`;

-- 5. 清空 Organizations 表 (没有其他表依赖它了)
DELETE FROM `Organizations`;


-- ===================================================================
-- Part 2: 重新插入种子数据
-- 插入顺序与外键依赖关系一致，从父表开始插入。
-- ===================================================================

-- 1. 插入组织 (依赖 user.id=1)
INSERT INTO `Organizations` (`id`, `name`, `ownerId`, `description`)
VALUES (101, 'SJTU芯片设计实验室', 1, '专注于高性能芯片设计与验证的实验室')
ON DUPLICATE KEY UPDATE `name`=VALUES(`name`), `description`=VALUES(`description`);

-- 2. 插入组织成员 (依赖 Organizations.id=101 和 user.id=2, 3)
INSERT INTO `OrganizationMembers` (`organizationId`, `userId`, `roleInOrg`)
VALUES
    (101, 2, 'admin'),
    (101, 3, 'member')
ON DUPLICATE KEY UPDATE `roleInOrg`=VALUES(`roleInOrg`);

-- 3. 插入文件夹 (依赖 Organizations.id=101 和 user.id=1, 2)
INSERT INTO `Folders` (`id`, `name`, `space`, `ownerUserId`, `ownerOrganizationId`)
VALUES
    (1001, '公共教程-基础标注示例', 'public', NULL, NULL),
    (2001, '项目A-RX前端电路图', 'organization', NULL, 101),
    (3001, '张三的私人草稿', 'private', 2, NULL),
    (4001, '管理员的测试集', 'private', 1, NULL)
ON DUPLICATE KEY UPDATE `name`=VALUES(`name`);

-- 4. 插入图片元数据 (依赖 Folders.id 和 user.id)
INSERT INTO `Images` (`id`, `folderId`, `originalFilename`, `storagePath`, `uploaderId`)
VALUES
    (1, 1001, 'public.png', 'public/img_uuid_001.png', 1),
    (2, 2001, 'org_design_v1.png', 'org/101/img_uuid_002.png', 2),
    (3, 3001, 'my_draft.png', 'user/2/img_uuid_003.png', 2)
ON DUPLICATE KEY UPDATE `originalFilename`=VALUES(`originalFilename`);

-- 5. 插入标注数据 (依赖 Images.id 和 user.id)
INSERT INTO `Annotations` (`imageId`, `jsonContent`, `lastEditorId`)
VALUES
    (1, '{}', 1),
    (2, '{}', 2),
    (3, '{}', 2)
ON DUPLICATE KEY UPDATE `jsonContent`=VALUES(`jsonContent`), `lastEditorId`=VALUES(`lastEditorId`);


-- ===================================================================
-- 数据重置完成
-- ===================================================================