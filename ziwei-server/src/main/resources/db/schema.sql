-- ============================================================
-- ZiWei (紫微斗数) Database Migration Script
-- Database: MySQL 5.7+
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
-- 1. ziwei_user (User Table)
-- ============================================================
DROP TABLE IF EXISTS ziwei_user;
CREATE TABLE ziwei_user (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
    open_id     VARCHAR(64)  NOT NULL                COMMENT 'WeChat openid',
    nickname    VARCHAR(50)  DEFAULT NULL            COMMENT 'Nickname',
    avatar      VARCHAR(500) DEFAULT NULL            COMMENT 'Avatar URL',
    gender      INT          DEFAULT NULL            COMMENT 'Gender: 0=female, 1=male',
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Create time',
    update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update time',
    deleted     TINYINT      NOT NULL DEFAULT 0      COMMENT 'Soft delete flag: 0=normal, 1=deleted',
    PRIMARY KEY (id),
    UNIQUE KEY uk_openid (open_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='ZiWei User Table';

-- ============================================================
-- 2. ziwei_chart (Chart / Natal Chart Master Table)
-- ============================================================
DROP TABLE IF EXISTS ziwei_chart;
CREATE TABLE ziwei_chart (
    -- Primary key
    id              BIGINT       NOT NULL AUTO_INCREMENT COMMENT 'Primary key',

    -- User
    user_id         BIGINT       NOT NULL                COMMENT 'User ID',

    -- Birth info (solar / Gregorian calendar)
    solar_year      INT          NOT NULL                COMMENT 'Solar birth year',
    solar_month     INT          NOT NULL                COMMENT 'Solar birth month (1-12)',
    solar_day       INT          NOT NULL                COMMENT 'Solar birth day (1-31)',
    solar_hour      INT          NOT NULL                COMMENT 'Solar birth hour (0-23)',
    solar_minute    INT          NOT NULL                COMMENT 'Solar birth minute (0-59)',
    gender          INT          NOT NULL                COMMENT 'Gender: 0=female, 1=male',
    birth_place     VARCHAR(100) DEFAULT NULL            COMMENT 'Birth place',
    is_dst          TINYINT      NOT NULL DEFAULT 0      COMMENT 'Is daylight saving time: 0=no, 1=yes',

    -- Lunar calendar
    lunar_year      INT          DEFAULT NULL            COMMENT 'Lunar year',
    lunar_month     INT          DEFAULT NULL            COMMENT 'Lunar month (1-12)',
    lunar_day       INT          DEFAULT NULL            COMMENT 'Lunar day (1-30)',
    is_leap_month   TINYINT      NOT NULL DEFAULT 0      COMMENT 'Is leap month: 0=no, 1=yes',

    -- Bazi (Four Pillars of Destiny)
    year_pillar     VARCHAR(10)  DEFAULT NULL            COMMENT 'Year pillar (e.g., 甲子)',
    month_pillar    VARCHAR(10)  DEFAULT NULL            COMMENT 'Month pillar (e.g., 丙寅)',
    day_pillar      VARCHAR(10)  DEFAULT NULL            COMMENT 'Day pillar (e.g., 戊辰)',
    hour_pillar     VARCHAR(10)  DEFAULT NULL            COMMENT 'Hour pillar (e.g., 庚申)',

    -- Chart core
    ming_gong_dizhi VARCHAR(5)   DEFAULT NULL            COMMENT 'Ming Palace earthly branch (命宫地支)',
    shen_gong_dizhi VARCHAR(5)   DEFAULT NULL            COMMENT 'Shen Palace earthly branch (身宫地支)',
    wuxing_ju       VARCHAR(10)  DEFAULT NULL            COMMENT 'Five Elements Bureau (五行局)',

    -- JSON backup
    chart_json      TEXT         DEFAULT NULL            COMMENT 'Full chart JSON backup',

    -- Timestamps
    create_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Create time',
    update_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update time',

    -- Soft delete
    deleted         TINYINT      NOT NULL DEFAULT 0      COMMENT 'Soft delete flag: 0=normal, 1=deleted',

    PRIMARY KEY (id),
    KEY idx_user_id (user_id),
    KEY idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='ZiWei Natal Chart Master Table (命盘主表)';

-- ============================================================
-- 3. ziwei_palace (Palace Table)
-- ============================================================
DROP TABLE IF EXISTS ziwei_palace;
CREATE TABLE ziwei_palace (
    id               BIGINT      NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
    chart_id         BIGINT      NOT NULL                COMMENT 'Chart ID (references ziwei_chart.id)',
    palace_type      INT         NOT NULL                COMMENT 'Palace type: 1=命宫, 2=兄弟, 3=夫妻, 4=子女, 5=财帛, 6=疾厄, 7=迁移, 8=交友, 9=官禄, 10=田宅, 11=福德, 12=父母',
    dizhi            VARCHAR(5)  NOT NULL                COMMENT 'Earthly branch of this palace (地支: 子丑寅卯辰巳午未申酉戌亥)',
    tian_gan         VARCHAR(5)  NOT NULL                COMMENT 'Heavenly stem of this palace (天干: 甲乙丙丁戊己庚辛壬癸)',
    is_shen_gong     TINYINT     NOT NULL DEFAULT 0      COMMENT 'Is this the Shen Palace (身宫): 0=no, 1=yes',
    da_xian_start_age INT        DEFAULT NULL            COMMENT 'Da Xian start age (大限起始岁数)',
    da_xian_end_age  INT        DEFAULT NULL            COMMENT 'Da Xian end age (大限结束岁数)',
    create_time      DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Create time',

    PRIMARY KEY (id),
    KEY idx_chart_id (chart_id),
    KEY idx_palace_type (chart_id, palace_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='ZiWei Palace Table (宫位表)';

-- ============================================================
-- 4. ziwei_star_position (Star Position Table)
-- ============================================================
DROP TABLE IF EXISTS ziwei_star_position;
CREATE TABLE ziwei_star_position (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
    chart_id    BIGINT       NOT NULL                COMMENT 'Chart ID (references ziwei_chart.id)',
    palace_id   BIGINT       NOT NULL                COMMENT 'Palace ID (references ziwei_palace.id)',
    star_code   VARCHAR(50)  NOT NULL                COMMENT 'Star code (星曜编码, e.g., ziwei, tianji, taiyang)',
    star_type   INT          NOT NULL                COMMENT 'Star type: 1=main star (主星), 2=auxiliary star (辅星), 3=miscellaneous star (杂曜), 4=transformation star (化星)',
    brightness  INT          DEFAULT NULL            COMMENT 'Brightness level: 1=庙, 2=旺, 3=得, 4=利, 5=平, 6=陷, 7=不',
    sihua_type  INT          DEFAULT NULL            COMMENT 'Si Hua type (if applicable): 1=化禄, 2=化权, 3=化科, 4=化忌',
    sort_order  INT          NOT NULL DEFAULT 0      COMMENT 'Sort order within the palace',
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Create time',

    PRIMARY KEY (id),
    KEY idx_chart_id (chart_id),
    KEY idx_palace_id (palace_id),
    KEY idx_star_code (chart_id, star_code),
    KEY idx_sihua_type (chart_id, sihua_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='ZiWei Star Position Table (星曜落位表)';

-- ============================================================
-- 5. ziwei_sihua (Four Transformations / Si Hua Table)
-- ============================================================
DROP TABLE IF EXISTS ziwei_sihua;
CREATE TABLE ziwei_sihua (
    id               BIGINT       NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
    chart_id         BIGINT       NOT NULL                COMMENT 'Chart ID (references ziwei_chart.id)',
    sihua_scope      INT          NOT NULL                COMMENT 'Si Hua scope: 1=ben ming (本命), 2=da xian (大限), 3=liu nian (流年)',
    ref_year         INT          DEFAULT NULL            COMMENT 'Reference year (引用年份), used when sihua_scope is liu nian',
    hua_lu_star_code VARCHAR(50)  DEFAULT NULL            COMMENT 'Hua Lu star code (化禄星曜编码)',
    hua_quan_star_code VARCHAR(50) DEFAULT NULL           COMMENT 'Hua Quan star code (化权星曜编码)',
    hua_ke_star_code VARCHAR(50)  DEFAULT NULL            COMMENT 'Hua Ke star code (化科星曜编码)',
    hua_ji_star_code VARCHAR(50)  DEFAULT NULL            COMMENT 'Hua Ji star code (化忌星曜编码)',
    create_time      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Create time',

    PRIMARY KEY (id),
    KEY idx_chart_id (chart_id),
    KEY idx_chart_scope (chart_id, sihua_scope),
    KEY idx_ref_year (chart_id, sihua_scope, ref_year)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='ZiWei Four Transformations Table (四化表)';

-- ============================================================
-- 6. ziwei_daxian (Da Xian / Major Limit Table)
-- ============================================================
DROP TABLE IF EXISTS ziwei_daxian;
CREATE TABLE ziwei_daxian (
    id             BIGINT      NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
    chart_id       BIGINT      NOT NULL                COMMENT 'Chart ID (references ziwei_chart.id)',
    sequence_order INT         NOT NULL                COMMENT 'Sequence order (1-12, representing the lifecycle order)',
    age_start      INT         NOT NULL                COMMENT 'Starting nominal age (起始虚岁)',
    age_end        INT         NOT NULL                COMMENT 'Ending nominal age (结束虚岁)',
    cal_year_start INT         DEFAULT NULL            COMMENT 'Calendar year start (公历起始年份)',
    cal_year_end   INT         DEFAULT NULL            COMMENT 'Calendar year end (公历结束年份)',
    palace_type    INT         NOT NULL                COMMENT 'Palace type where this Da Xian resides (所在宫位类型)',
    direction      TINYINT     NOT NULL DEFAULT 1      COMMENT 'Direction: 1=forward (顺行), 0=reverse (逆行)',
    create_time    DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Create time',

    PRIMARY KEY (id),
    KEY idx_chart_id (chart_id),
    KEY idx_chart_order (chart_id, sequence_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='ZiWei Da Xian / Major Limit Table (大限表)';
