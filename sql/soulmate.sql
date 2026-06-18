--用户表
DROP TABLE IF EXISTS app_user;
CREATE TABLE app_user (
      id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
      username VARCHAR(50) NOT NULL UNIQUE COMMENT '登录账号（spring security账号唯一）',
      password VARCHAR(100) NOT NULL COMMENT '密码（BCrypt密文）',
      enable TINYINT DEFAULT 1 COMMENT '启用状态（1启用0禁用）',
      create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
      update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
--      INDEX idx_uname(`username`)
);


-- 自定义用户：密码123456
INSERT INTO app_user(username,password,enable)
VALUES('admin','$2a$10$boIZnz7vsAivEEsDOzgKqOchyBVizYkgbQsIJmtuWBTDM7vTNK/EK',1);

-- 伴侣配置表
CREATE TABLE user_soulmate (
   id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '伴侣主键ID',
   user_id BIGINT NOT NULL COMMENT '所属用户id',
   gf_name VARCHAR(32) NOT NULL DEFAULT '' COMMENT '伴侣名字',
   sex VARCHAR(4) NOT NULL DEFAULT '女' COMMENT '性别：男/女',
   age TINYINT NOT NULL DEFAULT 18 COMMENT '年龄',
   birth VARCHAR(20) DEFAULT NULL COMMENT '生日 yyyy-MM-dd',
   hobby VARCHAR(255) NOT NULL DEFAULT '' COMMENT '兴趣爱好',
   character_tag VARCHAR(128) NOT NULL DEFAULT '' COMMENT '性格标签',
   detail_prompt TEXT NOT NULL DEFAULT '' COMMENT '人设系统提示词',

   create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
   update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
--      INDEX idx_uid(`user_id`),
--      UNIQUE KEY uk_uid_name(`user_id`,`gf_name`) COMMENT '同一个用户不能重名伴侣'
);

-- 伴侣头像表
CREATE TABLE soulmate_avatar (
   id BIGINT AUTO_INCREMENT PRIMARY KEY,
   soulmate_id BIGINT NOT NULL COMMENT '关联伴侣id',
   avatar_url VARCHAR(500) NOT NULL COMMENT '头像地址',
   create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
   update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
--    UNIQUE KEY uk_soulmate_id(`soulmate_id`) COMMENT '一个伴侣仅1个在用头像'
--    INDEX idx_smid(`soulmate_id`)
);


-- 用户头像配置表
CREATE TABLE user_avatar (
     id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
     user_id BIGINT NOT NULL COMMENT '关联用户id',
     avatar_url VARCHAR(500) NOT NULL COMMENT '头像url',
     create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
     update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
--    INDEX idx_uid(`user_id`)
);

-- spring_security实现rememberme库表
create table persistent_logins (
   username varchar(64) not null,
   series varchar(64) primary key,
   token varchar(64) not null,
   last_used timestamp not null
);

-- AI聊天记忆表
create table ai_soulmate_chat_memory (
     id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '自增主键',
     msg_uuid VARCHAR(64) NOT NULL COMMENT '单条消息唯一UUID',
     conversation_id VARCHAR(128) NOT NULL COMMENT '会话唯一ID',
     msg_type TINYINT NOT NULL COMMENT '消息类型 0系统 1用户 2助手 3工具响应',
     content TEXT COMMENT '消息文本内容',
     metadata TEXT COMMENT '元数据JSON字符串',
     media TEXT COMMENT '用户媒体资源列表JSON',
     tool_calls TEXT COMMENT '助手工具调用集合JSON',
     tool_responses TEXT COMMENT '工具返回响应集合JSON',
     create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '消息入库时间'
--      UNIQUE KEY uk_conv_msg_uuid (conversation_id, msg_uuid),
--      INDEX idx_conversation_id (conversation_id)
);

-- 备忘录表
CREATE TABLE memo (
      id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
      doc_id VARCHAR(64) NOT NULL COMMENT '全局唯一UUID，关联向量库主键',
      user_id BIGINT NOT NULL COMMENT '所属用户ID',
      soulmate_id BIGINT NOT NULL COMMENT '虚拟伴侣ID',
      content TEXT NOT NULL COMMENT '备忘录正文',
      create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
      update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
-- CREATE UNIQUE INDEX uk_doc_id ON memo(doc_id);
-- CREATE INDEX idx_user_id ON memo(user_id);

