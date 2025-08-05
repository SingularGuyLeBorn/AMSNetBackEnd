-- FILE: V6_Generate_Comprehensive_Seed_Data.sql
-- DESCRIPTION: A complete script to wipe and re-seed all necessary tables.
-- It creates 10 sample users with diverse roles and relationships to thoroughly test the application.

-- -------------------------------------------------------------------
-- 警告：此脚本会清空以下所有表的数据，包括 `user` 表，
-- 以确保 ID 和外键关系的一致性。请仅在开发或测试环境中使用。
-- -------------------------------------------------------------------

USE my_db;

-- ===================================================================
-- Part 1: 安全清空所有相关表的数据
-- ===================================================================
SET FOREIGN_KEY_CHECKS = 0; -- 临时禁用外键检查以便清空
TRUNCATE TABLE `Annotations`;
TRUNCATE TABLE `Images`;
TRUNCATE TABLE `Folders`;
TRUNCATE TABLE `OrganizationMembers`;
TRUNCATE TABLE `Organizations`;
TRUNCATE TABLE `user`;
SET FOREIGN_KEY_CHECKS = 1; -- 重新启用外键检查


-- ===================================================================
-- Part 2: 创建 10 个全新的示例用户
-- ===================================================================
INSERT INTO `user` (`id`, `userAccount`, `userPassword`, `userName`, `userRole`) VALUES
                                                                                     (1, 'platform_admin', 'd4986d7c40eb25899f85bb23020e233a', '平台管理员', 'admin'),
                                                                                     (2, 'alice_org_admin', 'd4986d7c40eb25899f85bb23020e233a', 'Alice (SJTU组织管理员)', 'user'),
                                                                                     (3, 'bob_member', 'd4986d7c40eb25899f85bb23020e233a', 'Bob (SJTU组织成员)', 'user'),
                                                                                     (4, 'charlie_lead', 'd4986d7c40eb25899f85bb23020e233a', 'Charlie (Quantum组织管理员)', 'user'),
                                                                                     (5, 'diana_engineer', 'd4986d7c40eb25899f85bb23020e233a', 'Diana (Quantum组织成员)', 'user'),
                                                                                     (6, 'eve_consultant', 'd4986d7c40eb25899f85bb23020e233a', 'Eve (跨组织顾问)', 'user'),
                                                                                     (7, 'frank_freelancer', 'd4986d7c40eb25899f85bb23020e233a', 'Frank (无组织用户)', 'user'),
                                                                                     (8, 'grace_banned', 'd4986d7c40eb25899f85bb23020e233a', 'Grace (被禁用)', 'ban'),
                                                                                     (9, 'henry_intern', 'd4986d7c40eb25899f85bb23020e233a', 'Henry (SJTU实习生)', 'user'),
                                                                                     (10, 'ivy_analyst', 'd4986d7c40eb25899f85bb23020e233a', 'Ivy (Quantum分析师)', 'user');


-- ===================================================================
-- Part 3: 创建组织
-- ===================================================================
INSERT INTO `Organizations` (`id`, `name`, `ownerId`, `description`) VALUES
                                                                         (101, 'SJTU芯片设计实验室', 1, '专注于高性能芯片设计与验证的实验室'),
                                                                         (102, 'Quantum Circuits Inc.', 4, '专注于量子计算电路的前沿研究公司');


-- ===================================================================
-- Part 4: 分配组织成员和角色
-- ===================================================================
INSERT INTO `OrganizationMembers` (`organizationId`, `userId`, `roleInOrg`) VALUES
-- SJTU 实验室成员
(101, 2, 'admin'),     -- Alice 是 SJTU 的管理员
(101, 3, 'member'),    -- Bob 是 SJTU 的成员
(101, 9, 'member'),    -- Henry 是 SJTU 的成员
-- Quantum Circuits 成员
(102, 4, 'admin'),     -- Charlie 是 Quantum 的管理员
(102, 5, 'member'),    -- Diana 是 Quantum 的成员
(102, 10, 'member'),   -- Ivy 是 Quantum 的成员
-- 跨组织成员 Eve
(101, 6, 'admin'),     -- Eve 在 SJTU 是管理员
(102, 6, 'member');    -- Eve 在 Quantum 是普通成员


-- ===================================================================
-- Part 5: 创建文件夹/工作区
-- ===================================================================
INSERT INTO `Folders` (`id`, `name`, `space`, `ownerUserId`, `ownerOrganizationId`) VALUES
-- 公共空间
(1001, '公共教程-基础标注示例', 'public', NULL, NULL),
-- 组织空间
(2001, 'SJTU-项目A-RX前端电路图', 'organization', NULL, 101),
(2002, 'Quantum-项目Q-核心模块', 'organization', NULL, 102),
-- 私人空间
(3001, 'Alice的私人项目', 'private', 2, NULL),
(3002, 'Bob的个人笔记', 'private', 3, NULL),
(3003, 'Frank的独立研究', 'private', 7, NULL);


-- ===================================================================
-- Part 6: 创建图片元数据
-- ===================================================================
INSERT INTO `Images` (`id`, `folderId`, `originalFilename`, `storagePath`, `uploaderId`) VALUES
                                                                                             (1, 1001, 'public_opamp.png', 'public/public_opamp_uuid.png', 1),
                                                                                             (2, 2001, 'sjtu_mixer_v1.png', 'org/101/sjtu_mixer_v1_uuid.png', 2),
                                                                                             (3, 2002, 'quantum_qubit_v3.png', 'org/102/quantum_qubit_v3_uuid.png', 4),
                                                                                             (4, 3001, 'alice_draft_01.png', 'user/2/alice_draft_01_uuid.png', 2),
                                                                                             (5, 3003, 'frank_idea.png', 'user/7/frank_idea_uuid.png', 7);


-- ===================================================================
-- Part 7: 创建标注数据 (包含真实感JSON)
-- ===================================================================
INSERT INTO `Annotations` (`id`, `imageId`, `jsonContent`, `lastEditorId`) VALUES
                                                                               (1, 1, '{
                                                                                 "cpnts": [
                                                                                   { "type": "opamp", "l": 100, "t": 120, "r": 250, "b": 280, "name": "opamp_0" }
                                                                                 ],
                                                                                 "segments": [],
                                                                                 "key_points": [],
                                                                                 "netlist_scs": "simulator lang=spectre\\nXOP0 (net1 net2 vdd vss) opamp_macro",
                                                                                 "netlist_cdl": ".SUBCKT opamp_macro in_p in_n vdd vss\\n.ENDS"
                                                                               }', 1),
                                                                               (2, 2, '{
                                                                                 "cpnts": [
                                                                                   { "type": "nmos", "l": 50, "t": 60, "r": 100, "b": 110, "name": "nmos_0" },
                                                                                   { "type": "resistor", "l": 150, "t": 40, "r": 200, "b": 60, "name": "resistor_0" }
                                                                                 ],
                                                                                 "segments": [],
                                                                                 "key_points": [],
                                                                                 "netlist_scs": "simulator lang=spectre\\nMN0 (out in gnd gnd) nmos w=1u l=0.18u",
                                                                                 "netlist_cdl": ".SUBCKT mixer_core ...\\n.ENDS"
                                                                               }', 3),
                                                                               (3, 3, '{ "cpnts": [], "segments": [], "key_points": [] }', 5),
                                                                               (4, 4, '{
                                                                                 "cpnts": [
                                                                                   { "type": "capacitor", "l": 20, "t": 30, "r": 80, "b": 90, "name": "capacitor_0" }
                                                                                 ],
                                                                                 "segments": [
                                                                                   { "src_key_point_id": 1, "dst_key_point_id": 2 }
                                                                                 ],
                                                                                 "key_points": [
                                                                                   { "id": 1, "net": "vdd", "type": "end", "x": 150.5, "y": 180.2, "port_id": 1 },
                                                                                   { "id": 2, "net": "vdd", "type": "end", "x": 250.0, "y": 180.2, "port_id": 2 }
                                                                                 ]
                                                                               }', 6),
                                                                               (5, 5, '{"cpnts": [], "segments": [], "key_points": []}', 7);

-- ===================================================================
-- 示例数据生成完毕
-- ===================================================================