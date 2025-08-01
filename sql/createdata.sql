-- 切换到您的数据库
USE my_db;

-- ===================================================================
-- Part 1: 创建所有新表
-- ===================================================================

-- 1. 组织表 (Organizations)
CREATE TABLE IF NOT EXISTS Organizations
(
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE COMMENT '组织名称，必须唯一',
    ownerId BIGINT NOT NULL COMMENT '组织的创建者/拥有者 ID',
    description VARCHAR(512) NULL COMMENT '组织简介',
    createTime DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updateTime DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL ON UPDATE CURRENT_TIMESTAMP,
    isDelete TINYINT DEFAULT 0 NOT NULL,
    FOREIGN KEY (ownerId) REFERENCES user(id) ON DELETE RESTRICT
) COMMENT '组织表' COLLATE = utf8mb4_unicode_ci;

-- 2. 组织成员表 (OrganizationMembers)
CREATE TABLE IF NOT EXISTS OrganizationMembers
(
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    organizationId BIGINT NOT NULL COMMENT '组织 ID',
    userId BIGINT NOT NULL COMMENT '用户 ID',
    roleInOrg VARCHAR(255) DEFAULT 'member' NOT NULL COMMENT '成员在组织内的角色',
    joinTime DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL,
    UNIQUE KEY uk_org_user (organizationId, userId),
    FOREIGN KEY (organizationId) REFERENCES Organizations(id) ON DELETE CASCADE,
    FOREIGN KEY (userId) REFERENCES user(id) ON DELETE CASCADE
) COMMENT '组织成员关系表' COLLATE = utf8mb4_unicode_ci;

-- 3. 文件夹表 (Folders)
CREATE TABLE IF NOT EXISTS Folders
(
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL COMMENT '文件夹名称',
    space ENUM('public', 'private', 'organization') NOT NULL COMMENT '空间类型',
    ownerUserId BIGINT NULL COMMENT '私有文件夹的拥有者 (当 space="private")',
    ownerOrganizationId BIGINT NULL COMMENT '组织文件夹的拥有者 (当 space="organization")',
    createTime DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updateTime DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL ON UPDATE CURRENT_TIMESTAMP,
    isDelete TINYINT DEFAULT 0 NOT NULL,
    FOREIGN KEY (ownerUserId) REFERENCES user(id) ON DELETE CASCADE,
    FOREIGN KEY (ownerOrganizationId) REFERENCES Organizations(id) ON DELETE CASCADE,
    CONSTRAINT chk_folder_owner CHECK (
        (space = 'private' AND ownerUserId IS NOT NULL AND ownerOrganizationId IS NULL) OR
        (space = 'organization' AND ownerOrganizationId IS NOT NULL AND ownerUserId IS NULL) OR
        (space = 'public' AND ownerUserId IS NULL AND ownerOrganizationId IS NULL)
        )
) COMMENT '文件夹表' COLLATE = utf8mb4_unicode_ci;

-- 4. 图片元数据表 (Images) - 已修正索引长度
CREATE TABLE IF NOT EXISTS Images
(
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    folderId BIGINT NOT NULL COMMENT '所属文件夹 ID',
    originalFilename VARCHAR(255) NOT NULL COMMENT '原始文件名',
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

-- 5. 标注表 (Annotations)
CREATE TABLE IF NOT EXISTS Annotations
(
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    imageId BIGINT NOT NULL UNIQUE COMMENT '对应的图片 ID (一对一关系)',
    jsonContent JSON NULL COMMENT '标注数据 (JSON 格式)',
    lastEditorId BIGINT NULL COMMENT '最后修改者 ID',
    updateTime DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (imageId) REFERENCES Images(id) ON DELETE CASCADE,
    FOREIGN KEY (lastEditorId) REFERENCES user(id) ON DELETE SET NULL
) COMMENT '标注数据表' COLLATE = utf8mb4_unicode_ci;


-- ===================================================================
-- Part 2: 插入示例数据
-- 如果需要重复执行此脚本，请取消以下 DELETE 语句的注释来清空旧数据
-- 注意：删除顺序与创建顺序相反，以避免外键约束问题
-- ===================================================================
-- DELETE FROM Annotations;
-- DELETE FROM Images;
-- DELETE FROM Folders;
-- DELETE FROM OrganizationMembers;
-- DELETE FROM Organizations;


-- 1. 插入组织 (Organizations)
INSERT INTO Organizations (id, name, ownerId, description)
VALUES (101, 'SJTU芯片设计实验室', 1902290828802396161, '专注于高性能芯片设计与验证的实验室')
ON DUPLICATE KEY UPDATE name=VALUES(name), description=VALUES(description); -- 如果组织已存在，则更新信息

-- 2. 插入组织成员 (OrganizationMembers)
INSERT INTO OrganizationMembers (organizationId, userId, roleInOrg)
VALUES
    (101, 1902290828802396161, 'admin'),
    (101, 1902041788016308226, 'member')
ON DUPLICATE KEY UPDATE roleInOrg=VALUES(roleInOrg); -- 如果成员已存在，则更新其角色

-- 3. 插入文件夹 (Folders)
INSERT INTO Folders (id, name, space, ownerUserId, ownerOrganizationId)
VALUES
    (1001, '公共教程-基础标注示例', 'public', NULL, NULL),
    (2001, '项目A-RX前端电路图', 'organization', NULL, 101),
    (3001, '我的私人草稿', 'private', 1902041788016308226, NULL),
    (4001, '个人测试数据集', 'private', 1902614151306416129, NULL)
ON DUPLICATE KEY UPDATE name=VALUES(name); -- 如果文件夹已存在，则更新其名称

-- 4. 插入图片元数据 (Images)
INSERT INTO Images (id, folderId, originalFilename, storagePath, width, height, fileSize, uploaderId)
VALUES
    (1, 1001, 'opamp_basic.png', 'public/opamp_basic_uuid.png', 1024, 768, 153286, 1902290828802396161),
    (2, 1001, 'resistor_array.png', 'public/resistor_array_uuid.png', 800, 600, 98765, 1902290828802396161),
    (3, 2001, 'mixer_schematic_v1.png', 'org/101/mixer_schematic_v1_uuid.png', 1280, 960, 245312, 1902290828802396161),
    (4, 2001, 'lna_layout_v2.png', 'org/101/lna_layout_v2_uuid.png', 1600, 1200, 312654, 1902041788016308226),
    (5, 3001, 'my_test_image_01.png', 'user/1902041788016308226/my_test_image_01_uuid.png', 640, 480, 75432, 1902041788016308226)
ON DUPLICATE KEY UPDATE originalFilename=VALUES(originalFilename); -- 如果图片已存在，则更新文件名

-- 5. 插入标注数据 (Annotations) - 已处理转义字符
INSERT INTO Annotations (imageId, jsonContent, lastEditorId)
VALUES
    (1, '{
      "cpnts": [
        { "type": "opamp", "l": 100, "t": 120, "r": 250, "b": 280, "name": "opamp_0" }
      ],
      "segments": [],
      "key_points": [],
      "netlist_scs": "simulator lang=spectre\\n\\nXOP0 (net1 net2 vdd vss) opamp_macro\\n\\nR0 (net2 0) resistor r=1k\\n",
      "netlist_cdl": ".SUBCKT opamp_macro in_p in_n vdd vss\\n* ... opamp cdl content ...\\n.ENDS"
    }', 1902290828802396161),
    (3, '{
      "cpnts": [
        { "type": "nmos", "l": 50, "t": 60, "r": 100, "b": 110, "name": "nmos_0" },
        { "type": "resistor", "l": 150, "t": 40, "r": 200, "b": 60, "name": "resistor_0" }
      ],
      "segments": [],
      "key_points": [],
      "netlist_scs": "simulator lang=spectre\\n\\nMN0 (out in gnd gnd) nmos w=1u l=0.18u\\n\\nR0 (vdd out) resistor r=2k\\n",
      "netlist_cdl": ".SUBCKT mixer_core ...\\n* ... mixer cdl content ...\\n.ENDS"
    }', 1902041788016308226),
    (5, '{
      "cpnts": [],
      "segments": [],
      "key_points": [],
      "netlist_scs": null,
      "netlist_cdl": null
    }', 1902041788016308226)
ON DUPLICATE KEY UPDATE jsonContent=VALUES(jsonContent), lastEditorId=VALUES(lastEditorId); -- 如果标注已存在，则更新