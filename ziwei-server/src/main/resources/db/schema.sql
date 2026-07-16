-- ============================================================
-- ZiWei (紫微斗数) Database Migration Script
-- Database: MySQL 8.0+
-- Engine: InnoDB
-- Charset: utf8mb4
-- Author: JTWORLD
-- ============================================================

-- Create database
CREATE DATABASE IF NOT EXISTS ziwei
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE ziwei;

-- ============================================================
-- 1. ziwei_user (用户表)
-- ============================================================
DROP TABLE IF EXISTS ziwei_user;
CREATE TABLE ziwei_user (
    id          BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键',
    open_id     VARCHAR(64) NOT NULL                COMMENT '微信 openid',
    nickname    VARCHAR(50) DEFAULT NULL            COMMENT '昵称',
    avatar      VARCHAR(500) DEFAULT NULL           COMMENT '头像URL',
    gender      TINYINT     DEFAULT NULL            COMMENT '性别: 0=女, 1=男',
    create_time DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted     TINYINT     NOT NULL DEFAULT 0      COMMENT '软删除: 0=正常, 1=已删除',
    PRIMARY KEY (id),
    UNIQUE KEY uk_openid (open_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- ============================================================
-- 2. ziwei_chart (命盘主表)
-- ============================================================
DROP TABLE IF EXISTS ziwei_chart;
CREATE TABLE ziwei_chart (
    id              BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键',
    user_id         BIGINT      DEFAULT NULL            COMMENT '用户ID',
    solar_year      INT         NOT NULL                COMMENT '公历出生年',
    solar_month     TINYINT     NOT NULL                COMMENT '公历出生月 (1-12)',
    solar_day       TINYINT     NOT NULL                COMMENT '公历出生日 (1-31)',
    solar_hour      TINYINT     NOT NULL                COMMENT '公历出生时 (0-23)',
    solar_minute    TINYINT     DEFAULT NULL            COMMENT '公历出生分 (0-59)',
    gender          TINYINT     NOT NULL                COMMENT '性别: 0=女, 1=男',
    birth_place     VARCHAR(100) DEFAULT NULL           COMMENT '出生地',
    is_dst          BIT         DEFAULT NULL            COMMENT '是否夏令时',
    lunar_year      INT         DEFAULT NULL            COMMENT '农历年',
    lunar_month     TINYINT     DEFAULT NULL            COMMENT '农历月 (1-12)',
    lunar_day       TINYINT     DEFAULT NULL            COMMENT '农历日 (1-30)',
    is_leap_month   BIT         DEFAULT NULL            COMMENT '是否闰月',
    year_pillar     VARCHAR(10) DEFAULT NULL            COMMENT '年柱 (如: 甲子)',
    month_pillar    VARCHAR(10) DEFAULT NULL            COMMENT '月柱 (如: 丙寅)',
    day_pillar      VARCHAR(10) DEFAULT NULL            COMMENT '日柱 (如: 戊辰)',
    hour_pillar     VARCHAR(10) DEFAULT NULL            COMMENT '时柱 (如: 庚申)',
    ming_gong_dizhi VARCHAR(5)  DEFAULT NULL            COMMENT '命宫地支',
    shen_gong_dizhi VARCHAR(5)  DEFAULT NULL            COMMENT '身宫地支',
    wuxing_ju       VARCHAR(10) DEFAULT NULL            COMMENT '五行局 (如: 水二局)',
    chart_json      LONGTEXT    DEFAULT NULL            COMMENT '命盘完整JSON备份',
    create_time     DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time     DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    creator         VARCHAR(50) DEFAULT NULL            COMMENT '创建者',
    updater         VARCHAR(50) DEFAULT NULL            COMMENT '更新者',
    deleted         BIT         DEFAULT 0               COMMENT '软删除: 0=正常, 1=已删除',
    PRIMARY KEY (id),
    KEY idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='命盘主表';

-- ============================================================
-- 3. ziwei_palace (宫位表)
-- ============================================================
DROP TABLE IF EXISTS ziwei_palace;
CREATE TABLE ziwei_palace (
    id                BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键',
    chart_id          BIGINT      NOT NULL                COMMENT '命盘ID',
    palace_type       TINYINT     NOT NULL                COMMENT '宫位类型: 1=命宫, 2=兄弟, 3=夫妻, 4=子女, 5=财帛, 6=疾厄, 7=迁移, 8=交友, 9=官禄, 10=田宅, 11=福德, 12=父母',
    dizhi             VARCHAR(5)  NOT NULL                COMMENT '宫位地支 (子丑寅卯辰巳午未申酉戌亥)',
    tian_gan          VARCHAR(5)  DEFAULT NULL            COMMENT '宫干 (甲乙丙丁戊己庚辛壬癸)',
    is_shen_gong      BIT         DEFAULT NULL            COMMENT '是否身宫',
    da_xian_start_age TINYINT     DEFAULT NULL            COMMENT '大限起始岁数',
    da_xian_end_age   TINYINT     DEFAULT NULL            COMMENT '大限结束岁数',
    create_time       DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time       DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    creator           VARCHAR(50) DEFAULT NULL            COMMENT '创建者',
    updater           VARCHAR(50) DEFAULT NULL            COMMENT '更新者',
    deleted           BIT         DEFAULT 0               COMMENT '软删除: 0=正常, 1=已删除',
    PRIMARY KEY (id),
    KEY idx_chart_id (chart_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='宫位表';

-- ============================================================
-- 4. ziwei_star_position (星曜落位表)
-- ============================================================
DROP TABLE IF EXISTS ziwei_star_position;
CREATE TABLE ziwei_star_position (
    id          BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键',
    chart_id    BIGINT      NOT NULL                COMMENT '命盘ID',
    palace_id   BIGINT      NOT NULL                COMMENT '宫位ID',
    star_code   VARCHAR(50) NOT NULL                COMMENT '星曜编码 (如: ziwei, tianji, taiyang)',
    star_type   TINYINT     NOT NULL                COMMENT '星曜类型: 1=主星, 2=辅星, 3=杂曜',
    brightness  TINYINT     DEFAULT NULL            COMMENT '亮度: 1=庙, 2=旺, 3=得, 4=利, 5=平, 6=陷',
    sihua_type  TINYINT     DEFAULT NULL            COMMENT '四化类型: 1=化禄, 2=化权, 3=化科, 4=化忌',
    sort_order  INT         DEFAULT NULL            COMMENT '同宫排序',
    create_time DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    creator     VARCHAR(50) DEFAULT NULL            COMMENT '创建者',
    updater     VARCHAR(50) DEFAULT NULL            COMMENT '更新者',
    deleted     BIT         DEFAULT 0               COMMENT '软删除: 0=正常, 1=已删除',
    PRIMARY KEY (id),
    KEY idx_chart_id (chart_id),
    KEY idx_palace_id (palace_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='星曜落位表';

-- ============================================================
-- 5. ziwei_sihua (四化表)
-- ============================================================
DROP TABLE IF EXISTS ziwei_sihua;
CREATE TABLE ziwei_sihua (
    id                 BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键',
    chart_id           BIGINT      NOT NULL                COMMENT '命盘ID',
    sihua_scope        TINYINT     NOT NULL                COMMENT '四化范围: 1=生年, 2=大限, 3=流年',
    ref_year           INT         DEFAULT NULL            COMMENT '参考年份 (流年四化时使用)',
    hua_lu_star_code   VARCHAR(50) DEFAULT NULL            COMMENT '化禄星曜编码',
    hua_quan_star_code VARCHAR(50) DEFAULT NULL            COMMENT '化权星曜编码',
    hua_ke_star_code   VARCHAR(50) DEFAULT NULL            COMMENT '化科星曜编码',
    hua_ji_star_code   VARCHAR(50) DEFAULT NULL            COMMENT '化忌星曜编码',
    create_time        DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time        DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    creator            VARCHAR(50) DEFAULT NULL            COMMENT '创建者',
    updater            VARCHAR(50) DEFAULT NULL            COMMENT '更新者',
    deleted            BIT         DEFAULT 0               COMMENT '软删除: 0=正常, 1=已删除',
    PRIMARY KEY (id),
    KEY idx_chart_id (chart_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='四化表';

-- ============================================================
-- 6. ziwei_daxian (大限表)
-- ============================================================
DROP TABLE IF EXISTS ziwei_daxian;
CREATE TABLE ziwei_daxian (
    id             BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键',
    chart_id       BIGINT      NOT NULL                COMMENT '命盘ID',
    sequence_order TINYINT     NOT NULL                COMMENT '序号 (1-12, 代表大限顺序)',
    age_start      TINYINT     NOT NULL                COMMENT '起始虚岁',
    age_end        TINYINT     NOT NULL                COMMENT '结束虚岁',
    cal_year_start INT         DEFAULT NULL            COMMENT '公历起始年份',
    cal_year_end   INT         DEFAULT NULL            COMMENT '公历结束年份',
    palace_type    TINYINT     NOT NULL                COMMENT '所在宫位类型 (1-12)',
    direction      BIT         NOT NULL DEFAULT 1      COMMENT '方向: 1=顺行, 0=逆行',
    create_time    DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time    DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    creator        VARCHAR(50) DEFAULT NULL            COMMENT '创建者',
    updater        VARCHAR(50) DEFAULT NULL            COMMENT '更新者',
    deleted        BIT         DEFAULT 0               COMMENT '软删除: 0=正常, 1=已删除',
    PRIMARY KEY (id),
    KEY idx_chart_id (chart_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='大限表';

-- ============================================================
-- 7. ziwei_rag_document (RAG文档表)
-- ============================================================
DROP TABLE IF EXISTS ziwei_rag_document;
CREATE TABLE ziwei_rag_document (
    id            BIGINT      NOT NULL AUTO_INCREMENT COMMENT '编号',
    book_title    VARCHAR(200) NOT NULL               COMMENT '书名',
    book_author   VARCHAR(100) DEFAULT NULL           COMMENT '作者/来源',
    file_name     VARCHAR(200) NOT NULL               COMMENT '原始文件名',
    file_url      VARCHAR(500) NOT NULL               COMMENT 'MinIO文件URL',
    file_size     BIGINT      DEFAULT NULL            COMMENT '文件大小 (bytes)',
    segment_count INT         DEFAULT 0               COMMENT '切分段数',
    status        TINYINT     NOT NULL DEFAULT 0      COMMENT '状态: 0=处理中, 1=已完成, 2=失败',
    error_message VARCHAR(500) DEFAULT NULL           COMMENT '失败原因 (status=2时记录)',
    creator       VARCHAR(50) DEFAULT NULL            COMMENT '创建者',
    create_time   DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updater       VARCHAR(50) DEFAULT NULL            COMMENT '更新者',
    update_time   DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted       TINYINT     DEFAULT 0               COMMENT '是否删除: 0=正常, 1=已删除',
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='RAG文档表';
