DROP DATABASE IF EXISTS lightcloud;
CREATE DATABASE lightcloud;
USE lightcloud;

# 创建用户表
DROP TABLE IF EXISTS `user`;
CREATE TABLE user(
    uid bigint NOT NULL PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
    nick_name varchar(20) DEFAULT NULL COMMENT '昵称',
    email varchar(120) DEFAULT NULL COMMENT '邮箱',
    qq_open_id varchar(35) DEFAULT NULL COMMENT 'qq登陆注册时使用的id',
    qq_avatar varchar(150) DEFAULT NULL COMMENT 'qq的头像',
    `password` varchar(32) DEFAULT NULL COMMENT '密码',
    `avatar_name` varchar(50) DEFAULT NULL COMMENT '用户的头像名',
    create_time datetime COMMENT '创建时间',
    last_login_time datetime COMMENT '最后的登陆时间',
    `status` tinyint COMMENT '用户状态： 0表示禁用，1表示启用',
    user_space bigint COMMENT '用户已占用空间',
    total_space bigint COMMENT '用户可用总空间'
) COMMENT = '用户信息';

# 创建用户表索引
CREATE UNIQUE INDEX idx_email ON user(email);
CREATE UNIQUE INDEX idx_qq_open_id ON user(qq_open_id);
CREATE INDEX idx_nick_name ON user(nick_name);

DROP TABLE IF EXISTS `file`;
#创建文件表
CREATE TABLE file(
    fid varchar(40) PRIMARY KEY NOT NULL COMMENT '文件的id',
    `name` varchar(35) NOT NULL COMMENT '文件的名字',
    file_path varchar(80) COMMENT '文件的相对路径',
    user_id bigint NOT NULL COMMENT '文件的用户id',
    file_md5 varchar(32) COMMENT '文件的md5值',
    file_parent varchar(40) COMMENT '文件的父级id,如果为0表示无父级目录',
    file_size bigint COMMENT '文件的大小',
    file_cover varchar(60) COMMENT '文件的封面',
    folder_type tinyint(2) COMMENT '文件夹的类型(0.file 1.dir)',
    file_category tinyint(2) default 0 COMMENT '文件的分类(1.video 2.image 3.music 4.document 5.zipFile 6.other)',
    `status` tinyint(2) COMMENT '文件的状态(0.删除 1.转码中 2.转码失败 3.转码成功 4.回收站 5.正常)',
    create_time datetime COMMENT '创建时间',
    last_update_time datetime COMMENT '最后更新时间',
    recovery_time datetime COMMENT '被放入回收站的时间',
    index idx_name(`name`),
    index idx_md5(file_md5),
    index idx_uid(user_id),
    index idx_parent(file_parent),
    index idx_update(last_update_time),
    index idx_status(`status`),
    index idx_recovery(recovery_time)
);


# 创建文件分享表
DROP TABLE IF EXISTS `share`;
CREATE TABLE share(
                     share_id varchar(30) NOT NULL PRIMARY KEY COMMENT '分享ID',
                     fid varchar(40) NOT NULL COMMENT '文件的id',
                     uid bigint DEFAULT NULL COMMENT '用户id',
                     `validity` tinyint COMMENT '分享时间（day）： 0表示永不过期',
                     create_time datetime COMMENT '创建时间',
                     expired_time datetime COMMENT '过期时间',
                     `code` varchar(8) COMMENT '提取码',
                     `view` INT DEFAULT 0 COMMENT '内容访问量',
                     index idx_share_id(`share_id`),
                     index idx_fid(`fid`),
                     index idx_uid(`uid`)
) COMMENT = '文件分享';