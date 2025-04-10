# 数据库初始化

-- 创建库
create database if not exists db_oj;

-- 切换库
use db_oj;

-- 用户表
create table if not exists user
(
    id            bigint auto_increment comment 'id' primary key,
    user_account  varchar(256)                           not null comment '账号',
    user_password varchar(512)                           not null comment '密码',
    user_name     varchar(256)                           null comment '用户昵称',
    union_id      varchar(256)                           null comment '微信开放平台id',
    user_avatar   varchar(1024)                          null comment '用户头像',
    user_profile  varchar(512)                           null comment '用户简介',
    user_role     varchar(256) default 'user'            not null comment '用户角色：user/admin/ban',
    create_time   datetime     default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time   datetime     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    is_delete     tinyint      default 0                 not null comment '是否删除',
    index idx_union_id (union_id)
) collate = utf8mb4_unicode_ci;

-- 题目表
create table if not exists problem
(
    id            bigint auto_increment comment 'id' primary key,
    title         varchar(512)                       null comment '标题',
    content       text                               null comment '内容(md文本)',
    tags          varchar(1024)                      null comment '标签列表（字符串形式的json数组）',
    level         varchar(20)                        null comment '题目难度',
    solution      text                               null comment '题解',
    submitted_num int      default 0                 not null comment '提交次数',
    accepted_num  int      default 0                 not null comment '通过次数',
    judge_case    text                               null comment '测试用例 (json字符串存储)',
    judge_config  text                               null comment '判题配置, 内存, 时间限制 (json字符串存储)',
    user_id       bigint                             not null comment '创建用户 id',
    create_time   datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time   datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    is_delete     tinyint  default 0                 not null comment '是否删除',
    index idx_title (title)
) collate = utf8mb4_unicode_ci;

-- 提交表
create table if not exists submission
(
    id          bigint auto_increment comment 'id' primary key,
    user_id     bigint                             not null comment '创建用户 id',
    problem_id  bigint                             not null comment '关于哪个题目的提交',
    source_code text                               not null comment '提交的源码',
    judge_info  text                               not null comment '判题信息(json对象)',
    status      int      default 0 comment '判题状态',
    language    varchar(128)                       not null comment '使用的编程语言',
    title       varchar(512)                       null comment '标题',
    content     text                               null comment '内容(md文本)',
    create_time datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    is_delete   tinyint  default 0                 not null comment '是否删除',
    index idx_user (user_id),
    index idx_problem (problem_id)
) collate = utf8mb4_unicode_ci;

DROP TABLE IF EXISTS `contest_problem`;
CREATE TABLE `contest_problem`
(
    `id`            bigint(20) unsigned                    NOT NULL AUTO_INCREMENT COMMENT '主键',
    `display_id`    varchar(255)                           NOT NULL COMMENT '该题目在比赛中的顺序id',
    `cid`           bigint(20) unsigned                    NOT NULL COMMENT '比赛id',
    `pid`           bigint(20) unsigned                    NOT NULL COMMENT '题目id',
    `display_title` varchar(255)                           NOT NULL COMMENT '该题目在比赛中的标题，默认为原名字',
    `color`         varchar(255) DEFAULT NULL COMMENT '气球颜色',
    create_time     datetime     default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time     datetime     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    is_delete       tinyint      default 0                 not null comment '是否删除',
    PRIMARY KEY (`id`, `cid`, `pid`),
    UNIQUE KEY `display_id` (`display_id`, `cid`, `pid`),
    KEY `contest_problem_ibfk_1` (`cid`),
    KEY `contest_problem_ibfk_2` (`pid`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 1
  DEFAULT CHARSET = utf8;