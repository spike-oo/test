-- =====================================================================
--  智能 AI 校园外卖平台 · 数据库建表脚本
--  数据库：MySQL 8.0（兼容 5.7+，字符集统一 utf8mb4）
--  依据：《第8组_智能AI校园外卖平台需求分析文档_按最初计划修订版_v3.docx》第 7 章 数据需求
--
--  约定：
--    1. 主键统一 VARCHAR(32)；所有表含 create_time / update_time；需逻辑删除的表含 deleted。
--    2. 金额统一 DECIMAL(10,2)；状态统一 TINYINT，取值范围写在字段注释中。
--    3. 表结构变更由「数据责任域」负责人提交，其他人不得直接改表（见 docs/02）。
-- =====================================================================

DROP DATABASE IF EXISTS campus_ai_delivery;
CREATE DATABASE campus_ai_delivery DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE campus_ai_delivery;

SET NAMES utf8mb4;

-- =====================================================================
-- 一、用户与画像域（主责：成员1，协助：成员4）
-- =====================================================================

-- 账号底座：登录、角色、状态。学生/商户/骑手/管理员各有一张档案表，通过 user_id 关联。
CREATE TABLE sys_user (
    id           VARCHAR(32)  NOT NULL COMMENT '用户ID',
    username     VARCHAR(50)  NOT NULL COMMENT '登录账号（学生用学号，商户/骑手用手机号，管理员自定义）',
    password     VARCHAR(100) NOT NULL COMMENT '密码（BCrypt 加盐哈希，禁止明文）',
    role         VARCHAR(20)  NOT NULL COMMENT '角色：STUDENT/MERCHANT/RIDER/ADMIN',
    nickname     VARCHAR(50)           DEFAULT NULL COMMENT '昵称',
    phone        VARCHAR(20)           DEFAULT NULL COMMENT '手机号（返回前端时脱敏）',
    avatar       VARCHAR(255)          DEFAULT NULL COMMENT '头像URL（对象存储）',
    status       TINYINT      NOT NULL DEFAULT 1 COMMENT '账号状态：0禁用 1正常',
    last_login_time DATETIME          DEFAULT NULL COMMENT '最近登录时间',
    create_time  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted      TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除 1已删除',
    PRIMARY KEY (id),
    UNIQUE KEY uk_username (username),
    KEY idx_role (role)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='账号表（四种角色共用登录底座）';

-- 学生用户表：学号与身份信息
CREATE TABLE student (
    id           VARCHAR(32) NOT NULL COMMENT '学生ID',
    user_id      VARCHAR(32) NOT NULL COMMENT '关联 sys_user.id',
    student_no   VARCHAR(20) NOT NULL COMMENT '学号（用于身份验证）',
    real_name    VARCHAR(50)          DEFAULT NULL COMMENT '姓名',
    college      VARCHAR(100)         DEFAULT NULL COMMENT '学院',
    grade        VARCHAR(20)          DEFAULT NULL COMMENT '年级',
    balance      DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '账户余额（模拟支付用，>=0.00）',
    create_time  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_id (user_id),
    UNIQUE KEY uk_student_no (student_no)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='学生用户表';

-- 用户饮食档案表：AI 推荐与饮食分析的核心输入
CREATE TABLE diet_profile (
    id              VARCHAR(32) NOT NULL COMMENT '档案ID',
    user_id         VARCHAR(32) NOT NULL COMMENT '关联 sys_user.id',
    diet_goal       TINYINT     NOT NULL DEFAULT 0 COMMENT '饮食目标：0普通 1减脂 2增肌',
    taste_preference VARCHAR(100)       DEFAULT NULL COMMENT '口味偏好，逗号分隔，如"清淡,微辣"',
    allergy_foods   TEXT                DEFAULT NULL COMMENT '忌口/过敏原食材列表（不超过500字），AI点餐时过滤',
    dislike_foods   TEXT                DEFAULT NULL COMMENT '不喜欢的食材',
    height_cm       INT                 DEFAULT NULL COMMENT '身高（cm），用于营养估算',
    weight_kg       DECIMAL(5,1)        DEFAULT NULL COMMENT '体重（kg），用于营养估算',
    update_time     DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_time     DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_id (user_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='用户饮食档案表';

-- 收货地址表
CREATE TABLE user_address (
    id           VARCHAR(32) NOT NULL COMMENT '地址ID',
    user_id      VARCHAR(32) NOT NULL COMMENT '关联 sys_user.id',
    receiver     VARCHAR(50) NOT NULL COMMENT '收货人',
    phone        VARCHAR(20) NOT NULL COMMENT '联系电话',
    campus_area  VARCHAR(100)         DEFAULT NULL COMMENT '校区/园区',
    building     VARCHAR(100)         DEFAULT NULL COMMENT '宿舍楼/教学楼',
    detail       VARCHAR(255)         DEFAULT NULL COMMENT '详细地址',
    is_default   TINYINT     NOT NULL DEFAULT 0 COMMENT '是否默认：0否 1是',
    create_time  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted      TINYINT     NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除 1已删除',
    PRIMARY KEY (id),
    KEY idx_user_id (user_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='用户收货地址表';

-- 收藏表（店铺与菜品共用）
CREATE TABLE user_favorite (
    id           VARCHAR(32) NOT NULL COMMENT '收藏ID',
    user_id      VARCHAR(32) NOT NULL COMMENT '关联 sys_user.id',
    target_type  TINYINT     NOT NULL COMMENT '收藏类型：1店铺 2菜品',
    target_id    VARCHAR(32) NOT NULL COMMENT '收藏对象ID',
    create_time  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_target (user_id, target_type, target_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='用户收藏表';

-- 商户表
CREATE TABLE merchant (
    id            VARCHAR(32) NOT NULL COMMENT '商户ID',
    user_id       VARCHAR(32) NOT NULL COMMENT '关联 sys_user.id',
    merchant_name VARCHAR(100) NOT NULL COMMENT '商户名称',
    contact_name  VARCHAR(50)          DEFAULT NULL COMMENT '联系人',
    contact_phone VARCHAR(20)          DEFAULT NULL COMMENT '联系电话',
    license_no    VARCHAR(64)          DEFAULT NULL COMMENT '营业执照/校园经营许可编号',
    audit_status  TINYINT     NOT NULL DEFAULT 0 COMMENT '审核状态：0待审核 1已通过 2已驳回',
    audit_remark  VARCHAR(255)         DEFAULT NULL COMMENT '审核意见/驳回原因',
    audit_time    DATETIME             DEFAULT NULL COMMENT '审核时间',
    audit_by      VARCHAR(32)          DEFAULT NULL COMMENT '审核人（管理员ID）',
    create_time   DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time   DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_id (user_id),
    KEY idx_audit_status (audit_status)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='商户表';

-- 骑手表（P2 可选加分功能）
CREATE TABLE rider (
    id                VARCHAR(32) NOT NULL COMMENT '骑手ID',
    user_id           VARCHAR(32) NOT NULL COMMENT '关联 sys_user.id',
    real_name         VARCHAR(50) NOT NULL COMMENT '真实姓名',
    id_card           VARCHAR(32)          DEFAULT NULL COMMENT '身份证号（加密存储，展示脱敏）',
    student_card_no   VARCHAR(32)          DEFAULT NULL COMMENT '学生证/工号',
    vehicle_type      VARCHAR(20)          DEFAULT NULL COMMENT '配送车辆：步行/自行车/电动车',
    work_status       TINYINT     NOT NULL DEFAULT 0 COMMENT '工作状态：0休息中 1接单中',
    delivering_count  INT         NOT NULL DEFAULT 0 COMMENT '当前配送中订单数（抢单成功+1，完成-1）',
    max_delivering    INT         NOT NULL DEFAULT 3 COMMENT '同时最大接单数（默认3单）',
    total_income      DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '累计收入',
    avg_delivery_time INT                  DEFAULT NULL COMMENT '历史平均配送时长（分钟）',
    audit_status      TINYINT     NOT NULL DEFAULT 0 COMMENT '审核状态：0待审核 1已通过 2已驳回',
    audit_remark      VARCHAR(255)         DEFAULT NULL COMMENT '审核意见',
    create_time       DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time       DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_id (user_id),
    KEY idx_work_status (work_status),
    KEY idx_audit_status (audit_status)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='骑手表';

-- 管理员表
CREATE TABLE admin (
    id           VARCHAR(32) NOT NULL COMMENT '管理员ID',
    user_id      VARCHAR(32) NOT NULL COMMENT '关联 sys_user.id',
    real_name    VARCHAR(50)          DEFAULT NULL COMMENT '姓名',
    admin_level  TINYINT     NOT NULL DEFAULT 1 COMMENT '管理员级别：1普通 2超级',
    create_time  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_id (user_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='管理员表';

-- 角色权限表（RBAC：角色-权限-数据范围）
CREATE TABLE sys_role_permission (
    id           VARCHAR(32) NOT NULL COMMENT '主键',
    role         VARCHAR(20) NOT NULL COMMENT '角色：STUDENT/MERCHANT/RIDER/ADMIN',
    permission   VARCHAR(64) NOT NULL COMMENT '权限标识，如 order:read / dish:write',
    data_scope   VARCHAR(32) NOT NULL DEFAULT 'SELF' COMMENT '数据范围：ALL全部 / SHOP本店 / SELF本人',
    create_time  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_role_permission (role, permission)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='角色权限表（权限数据缓存到 Redis）';

-- =====================================================================
-- 二、商品与经营域（主责：成员2）
-- =====================================================================

-- 店铺分类表（早餐、正餐、奶茶、夜宵等）
CREATE TABLE shop_category (
    id           VARCHAR(32) NOT NULL COMMENT '分类ID',
    name         VARCHAR(50) NOT NULL COMMENT '分类名称',
    icon         VARCHAR(255)         DEFAULT NULL COMMENT '分类图标URL',
    sort         INT         NOT NULL DEFAULT 0 COMMENT '排序值，越小越靠前',
    status       TINYINT     NOT NULL DEFAULT 1 COMMENT '状态：0停用 1启用',
    create_time  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='店铺分类表';

-- 店铺表
CREATE TABLE shop (
    id               VARCHAR(32) NOT NULL COMMENT '店铺ID',
    merchant_id      VARCHAR(32) NOT NULL COMMENT '所属商户ID（数据行级隔离依据）',
    category_id      VARCHAR(32)          DEFAULT NULL COMMENT '店铺分类ID',
    shop_name        VARCHAR(100) NOT NULL COMMENT '店铺名称',
    logo             VARCHAR(255)         DEFAULT NULL COMMENT '店铺Logo URL',
    description      VARCHAR(500)         DEFAULT NULL COMMENT '店铺介绍',
    address          VARCHAR(255)         DEFAULT NULL COMMENT '店铺地址（校园内）',
    phone            VARCHAR(20)          DEFAULT NULL COMMENT '联系电话',
    notice           VARCHAR(500)         DEFAULT NULL COMMENT '店铺公告',
    open_time        VARCHAR(50)          DEFAULT NULL COMMENT '营业时间，如"07:00-21:00"',
    business_status  TINYINT     NOT NULL DEFAULT 1 COMMENT '营业状态：0休息中 1营业中',
    ban_status       TINYINT     NOT NULL DEFAULT 0 COMMENT '封禁状态：0正常 1已封禁',
    min_price        DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '起送价',
    delivery_fee     DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '配送费',
    delivery_time    INT                  DEFAULT NULL COMMENT '预计配送时长（分钟）',
    score            DECIMAL(3,2) NOT NULL DEFAULT 5.00 COMMENT '综合评分（0.00-5.00）',
    monthly_sales    INT         NOT NULL DEFAULT 0 COMMENT '月销量',
    create_time      DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time      DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted          TINYINT     NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除 1已删除',
    PRIMARY KEY (id),
    KEY idx_merchant_id (merchant_id),
    KEY idx_category_id (category_id),
    KEY idx_business_status (business_status)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='店铺表';

-- 菜品分类表（店铺内分类：热销、主食、小吃、饮品等）
CREATE TABLE dish_category (
    id           VARCHAR(32) NOT NULL COMMENT '分类ID',
    shop_id      VARCHAR(32) NOT NULL COMMENT '所属店铺ID',
    name         VARCHAR(50) NOT NULL COMMENT '分类名称',
    sort         INT         NOT NULL DEFAULT 0 COMMENT '排序值',
    status       TINYINT     NOT NULL DEFAULT 1 COMMENT '状态：0停用 1启用',
    create_time  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_shop_id (shop_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='菜品分类表';

-- 菜品表
CREATE TABLE dish (
    id             VARCHAR(32) NOT NULL COMMENT '菜品ID',
    shop_id        VARCHAR(32) NOT NULL COMMENT '所属店铺ID',
    category_id    VARCHAR(32)          DEFAULT NULL COMMENT '菜品分类ID',
    dish_name      VARCHAR(50) NOT NULL COMMENT '菜品名称（1-50字符）',
    price          DECIMAL(10,2) NOT NULL COMMENT '菜品单价（0.01-9999.99）',
    image          VARCHAR(255)         DEFAULT NULL COMMENT '主图URL（对象存储）',
    description    VARCHAR(500)         DEFAULT NULL COMMENT '菜品描述（AI 语义匹配的依据）',
    tags           VARCHAR(200)         DEFAULT NULL COMMENT '菜品标签，逗号分隔，如"清淡,低脂,不辣"',
    ingredients    VARCHAR(255)         DEFAULT NULL COMMENT '主要食材，逗号分隔（过敏原过滤依据）',
    stock          INT         NOT NULL DEFAULT 0 COMMENT '库存数量（>=0，不可为负）',
    status         TINYINT     NOT NULL DEFAULT 1 COMMENT '上下架状态：0下架 1上架',
    monthly_sales  INT         NOT NULL DEFAULT 0 COMMENT '月销量（近30天）',
    score          DECIMAL(3,2) NOT NULL DEFAULT 5.00 COMMENT '菜品评分（0.00-5.00）',
    create_time    DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time    DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted        TINYINT     NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除 1已删除',
    PRIMARY KEY (id),
    KEY idx_shop_id (shop_id),
    KEY idx_category_id (category_id),
    KEY idx_status (status)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='菜品表';

-- 菜品规格表（大小份、辣度等）
CREATE TABLE dish_spec (
    id           VARCHAR(32) NOT NULL COMMENT '规格ID',
    dish_id      VARCHAR(32) NOT NULL COMMENT '所属菜品ID',
    spec_name    VARCHAR(50) NOT NULL COMMENT '规格名称，如"大份""微辣"',
    price_diff   DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '相对基准价的差价，可为负',
    stock        INT                  DEFAULT NULL COMMENT '该规格库存，NULL 表示共用菜品库存',
    sort         INT         NOT NULL DEFAULT 0 COMMENT '排序值',
    create_time  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_dish_id (dish_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='菜品规格表';

-- 菜品图片表（详情图）
CREATE TABLE dish_image (
    id           VARCHAR(32) NOT NULL COMMENT '图片ID',
    dish_id      VARCHAR(32) NOT NULL COMMENT '所属菜品ID',
    url          VARCHAR(255) NOT NULL COMMENT '图片URL（对象存储）',
    sort         INT         NOT NULL DEFAULT 0 COMMENT '排序值',
    create_time  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_dish_id (dish_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='菜品图片表';

-- 评价表（AI 评价分析的数据源）
CREATE TABLE review (
    id              VARCHAR(32) NOT NULL COMMENT '评价ID',
    order_id        VARCHAR(32) NOT NULL COMMENT '关联订单ID',
    user_id         VARCHAR(32) NOT NULL COMMENT '评价人（学生）',
    shop_id         VARCHAR(32) NOT NULL COMMENT '被评价店铺ID',
    rider_id        VARCHAR(32)          DEFAULT NULL COMMENT '被评价骑手ID（配送评价）',
    taste_score     TINYINT     NOT NULL COMMENT '口味评分：1-5',
    service_score   TINYINT              DEFAULT NULL COMMENT '商家服务评分：1-5',
    delivery_score  TINYINT              DEFAULT NULL COMMENT '配送评分：1-5',
    content         TEXT                 DEFAULT NULL COMMENT '评价内容（不超过500字）',
    is_anonymous    TINYINT     NOT NULL DEFAULT 0 COMMENT '是否匿名：0否 1是',
    ai_tags         VARCHAR(200)         DEFAULT NULL COMMENT 'AI提取的评价标签，逗号分隔',
    sentiment       TINYINT              DEFAULT NULL COMMENT '情感倾向：0差评 1中评 2好评（AI生成）',
    ai_model_version VARCHAR(50)         DEFAULT NULL COMMENT '生成标签的模型版本',
    ai_analyze_time DATETIME             DEFAULT NULL COMMENT 'AI分析时间',
    merchant_reply  VARCHAR(500)         DEFAULT NULL COMMENT '商家回复',
    reply_time      DATETIME             DEFAULT NULL COMMENT '回复时间',
    create_time     DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time     DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_order_id (order_id),
    KEY idx_shop_id (shop_id),
    KEY idx_user_id (user_id),
    KEY idx_sentiment (sentiment)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='评价表';

-- 评价图片表
CREATE TABLE review_image (
    id           VARCHAR(32) NOT NULL COMMENT '图片ID',
    review_id    VARCHAR(32) NOT NULL COMMENT '所属评价ID',
    url          VARCHAR(255) NOT NULL COMMENT '图片URL（对象存储）',
    sort         INT         NOT NULL DEFAULT 0 COMMENT '排序值',
    create_time  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_review_id (review_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='评价图片表';

-- 评价标签表（AI 提取结果的明细，用于标签云与高频统计）
CREATE TABLE review_tag (
    id           VARCHAR(32) NOT NULL COMMENT '标签ID',
    review_id    VARCHAR(32) NOT NULL COMMENT '所属评价ID',
    shop_id      VARCHAR(32) NOT NULL COMMENT '冗余店铺ID，便于按店铺聚合',
    tag_name     VARCHAR(50) NOT NULL COMMENT '标签名，如"口味好""出餐慢"',
    tag_type     VARCHAR(20) NOT NULL COMMENT '维度：TASTE口味 / PORTION分量 / SPEED速度 / HYGIENE卫生 / SERVICE服务 / PRICE价格',
    sentiment    TINYINT     NOT NULL COMMENT '情感：0负面 1中性 2正面',
    create_time  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_shop_tag (shop_id, tag_name),
    KEY idx_review_id (review_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='评价标签表（AI 标签聚合的数据源）';

-- =====================================================================
-- 三、交易与配送域（主责：成员4，协同：成员2）
-- =====================================================================

-- 购物车表
CREATE TABLE cart_item (
    id           VARCHAR(32) NOT NULL COMMENT '购物车项ID',
    user_id      VARCHAR(32) NOT NULL COMMENT '所属学生ID',
    shop_id      VARCHAR(32) NOT NULL COMMENT '所属店铺ID（同一店铺才可一起结算）',
    dish_id      VARCHAR(32) NOT NULL COMMENT '菜品ID',
    spec_id      VARCHAR(32)          DEFAULT NULL COMMENT '规格ID',
    quantity     INT         NOT NULL DEFAULT 1 COMMENT '数量',
    selected     TINYINT     NOT NULL DEFAULT 1 COMMENT '是否勾选结算：0否 1是',
    create_time  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_user_shop (user_id, shop_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='购物车表';

-- 订单主表（系统最核心的业务数据）
CREATE TABLE orders (
    id             VARCHAR(32) NOT NULL COMMENT '订单ID',
    order_no       VARCHAR(32) NOT NULL COMMENT '订单编号（对外展示，全局唯一）',
    user_id        VARCHAR(32) NOT NULL COMMENT '下单学生ID',
    shop_id        VARCHAR(32) NOT NULL COMMENT '店铺ID',
    rider_id       VARCHAR(32)          DEFAULT NULL COMMENT '配送骑手ID（抢单成功后写入）',
    goods_amount   DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '商品金额',
    delivery_fee   DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '配送费',
    total_amount   DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '订单实付总金额',
    pay_type       TINYINT              DEFAULT NULL COMMENT '支付方式：1余额 2模拟第三方',
    pay_status     TINYINT     NOT NULL DEFAULT 0 COMMENT '支付状态：0未支付 1已支付 2已退款',
    pay_time       DATETIME             DEFAULT NULL COMMENT '支付时间',
    order_status   TINYINT     NOT NULL DEFAULT 0 COMMENT '订单状态：0待接单 1备餐中 2待取餐 3配送中 4已完成 5已取消 6申诉中',
    delivery_type  TINYINT     NOT NULL DEFAULT 1 COMMENT '配送方式：1骑手配送 2宿舍楼下自取',
    receiver       VARCHAR(50)          DEFAULT NULL COMMENT '收货人',
    receiver_phone VARCHAR(20)          DEFAULT NULL COMMENT '收货电话',
    address        VARCHAR(255)         DEFAULT NULL COMMENT '收货地址',
    remark         VARCHAR(255)         DEFAULT NULL COMMENT '学生备注（忌口等）',
    pickup_code    VARCHAR(6)           DEFAULT NULL COMMENT '取餐码（商户出餐时生成，000000-999999）',
    cancel_reason  VARCHAR(255)         DEFAULT NULL COMMENT '取消/拒单原因',
    expect_time    DATETIME             DEFAULT NULL COMMENT '预计送达时间（AI 估算）',
    finish_time    DATETIME             DEFAULT NULL COMMENT '订单完成时间',
    version        INT         NOT NULL DEFAULT 0 COMMENT '乐观锁版本号（防止非法状态变更）',
    create_time    DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '下单时间',
    update_time    DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_order_no (order_no),
    KEY idx_user_status (user_id, order_status),
    KEY idx_shop_status (shop_id, order_status),
    KEY idx_rider_status (rider_id, order_status),
    KEY idx_create_time (create_time)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='订单主表';

-- 订单明细表
CREATE TABLE order_item (
    id           VARCHAR(32) NOT NULL COMMENT '明细ID',
    order_id     VARCHAR(32) NOT NULL COMMENT '所属订单ID',
    dish_id      VARCHAR(32) NOT NULL COMMENT '菜品ID',
    dish_name    VARCHAR(50) NOT NULL COMMENT '菜品名称（下单时快照，防止菜品改名影响历史订单）',
    dish_image   VARCHAR(255)         DEFAULT NULL COMMENT '菜品图片快照',
    spec_name    VARCHAR(50)          DEFAULT NULL COMMENT '规格名称快照',
    price        DECIMAL(10,2) NOT NULL COMMENT '成交单价快照',
    quantity     INT         NOT NULL COMMENT '数量',
    amount       DECIMAL(10,2) NOT NULL COMMENT '小计金额 = price * quantity',
    create_time  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_order_id (order_id),
    KEY idx_dish_id (dish_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='订单明细表';

-- 订单状态流转记录表（全程可追溯）
CREATE TABLE order_status_log (
    id            VARCHAR(32) NOT NULL COMMENT '记录ID',
    order_id      VARCHAR(32) NOT NULL COMMENT '订单ID',
    from_status   TINYINT              DEFAULT NULL COMMENT '变更前状态',
    to_status     TINYINT     NOT NULL COMMENT '变更后状态',
    operator_id   VARCHAR(32)          DEFAULT NULL COMMENT '操作人ID',
    operator_role VARCHAR(20)          DEFAULT NULL COMMENT '操作人角色',
    reason        VARCHAR(255)         DEFAULT NULL COMMENT '变更原因',
    create_time   DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '变更时间',
    PRIMARY KEY (id),
    KEY idx_order_id (order_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='订单状态流转记录表';

-- 配送记录表（P2）
CREATE TABLE delivery_record (
    id            VARCHAR(32) NOT NULL COMMENT '配送记录ID',
    order_id      VARCHAR(32) NOT NULL COMMENT '订单ID',
    rider_id      VARCHAR(32) NOT NULL COMMENT '骑手ID',
    grab_time     DATETIME             DEFAULT NULL COMMENT '抢单时间',
    pickup_time   DATETIME             DEFAULT NULL COMMENT '取餐时间',
    finish_time   DATETIME             DEFAULT NULL COMMENT '送达时间',
    duration_min  INT                  DEFAULT NULL COMMENT '配送耗时（分钟）',
    delivery_fee  DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '本单配送费（骑手收入）',
    status        TINYINT     NOT NULL DEFAULT 0 COMMENT '配送状态：0待取餐 1配送中 2已送达 3异常',
    abnormal_type VARCHAR(50)          DEFAULT NULL COMMENT '异常类型：地址错误/学生拒收/餐品损坏等',
    abnormal_desc VARCHAR(255)         DEFAULT NULL COMMENT '异常说明',
    create_time   DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time   DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_order_id (order_id),
    KEY idx_rider_id (rider_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='配送记录表';

-- 抢单日志表（并发抢单的审计依据）
CREATE TABLE grab_log (
    id          VARCHAR(32) NOT NULL COMMENT '日志ID',
    order_id    VARCHAR(32) NOT NULL COMMENT '订单ID',
    rider_id    VARCHAR(32) NOT NULL COMMENT '骑手ID',
    result      TINYINT     NOT NULL COMMENT '结果：1成功 0失败',
    fail_reason VARCHAR(100)         DEFAULT NULL COMMENT '失败原因：已被抢走/超出接单上限/工作状态非接单中',
    create_time DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_order_id (order_id),
    KEY idx_rider_id (rider_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='抢单日志表';

-- 模拟支付记录表
CREATE TABLE payment_record (
    id            VARCHAR(32) NOT NULL COMMENT '支付记录ID',
    order_id      VARCHAR(32) NOT NULL COMMENT '订单ID',
    user_id       VARCHAR(32) NOT NULL COMMENT '支付人',
    amount        DECIMAL(10,2) NOT NULL COMMENT '支付金额',
    pay_type      TINYINT     NOT NULL COMMENT '支付方式：1余额 2模拟第三方',
    status        TINYINT     NOT NULL DEFAULT 0 COMMENT '状态：0待支付 1成功 2失败 3已退款',
    trade_no      VARCHAR(64)          DEFAULT NULL COMMENT '模拟交易号',
    create_time   DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time   DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_order_id (order_id),
    KEY idx_user_id (user_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='模拟支付记录表';

-- =====================================================================
-- 四、AI 与运营域（主责：成员3，成员1/2 提供业务数据）
-- =====================================================================

-- 大模型配置表
CREATE TABLE ai_config (
    id           VARCHAR(32) NOT NULL COMMENT '配置ID',
    provider     VARCHAR(50) NOT NULL COMMENT '服务商，如 openai-compatible / doubao / qwen',
    api_url      VARCHAR(255) NOT NULL COMMENT 'API 地址',
    model_name   VARCHAR(100) NOT NULL COMMENT '模型名称',
    api_key      VARCHAR(255) NOT NULL COMMENT 'API Key（加密存储，禁止返回前端）',
    temperature  DECIMAL(3,2) NOT NULL DEFAULT 0.70 COMMENT '温度参数',
    max_tokens   INT         NOT NULL DEFAULT 2048 COMMENT '最大输出 Token',
    timeout_ms   INT         NOT NULL DEFAULT 15000 COMMENT '超时时间（毫秒）',
    is_default   TINYINT     NOT NULL DEFAULT 0 COMMENT '是否默认配置：0否 1是',
    status       TINYINT     NOT NULL DEFAULT 1 COMMENT '状态：0停用 1启用',
    create_time  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='大模型配置表';

-- AI 对话会话表
CREATE TABLE ai_chat_session (
    id           VARCHAR(32) NOT NULL COMMENT '会话ID',
    user_id      VARCHAR(32) NOT NULL COMMENT '所属用户ID',
    session_type VARCHAR(20) NOT NULL COMMENT '会话类型：ORDER点餐 / SEARCH搜索 / SERVICE客服',
    title        VARCHAR(100)         DEFAULT NULL COMMENT '会话标题（取首句）',
    message_count INT        NOT NULL DEFAULT 0 COMMENT '消息数',
    create_time  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_user_type (user_id, session_type)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='AI 对话会话表';

-- AI 对话消息表（多轮上下文，采用滑动窗口读取最近 N 轮）
CREATE TABLE ai_chat_message (
    id           VARCHAR(32) NOT NULL COMMENT '消息ID',
    session_id   VARCHAR(32) NOT NULL COMMENT '所属会话ID',
    role         VARCHAR(20) NOT NULL COMMENT '角色：user / assistant / system',
    content      TEXT                 DEFAULT NULL COMMENT '消息内容',
    tokens       INT                  DEFAULT NULL COMMENT 'Token 消耗',
    model_version VARCHAR(50)         DEFAULT NULL COMMENT '模型版本',
    create_time  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_session_id (session_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='AI 对话消息表';

-- 客服知识库表
CREATE TABLE knowledge_base (
    id           VARCHAR(32) NOT NULL COMMENT '知识ID',
    category     VARCHAR(50)          DEFAULT NULL COMMENT '分类：订单/退款/配送/账号等',
    question     VARCHAR(255) NOT NULL COMMENT '标准问题',
    answer       TEXT         NOT NULL COMMENT '标准答案',
    keywords     VARCHAR(255)         DEFAULT NULL COMMENT '关键词，逗号分隔，用于检索召回',
    hit_count    INT         NOT NULL DEFAULT 0 COMMENT '命中次数',
    status       TINYINT     NOT NULL DEFAULT 1 COMMENT '状态：0停用 1启用',
    create_time  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_category (category)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='客服知识库表';

-- 客服工单表（AI 无法回答时转人工）
CREATE TABLE ticket (
    id           VARCHAR(32) NOT NULL COMMENT '工单ID',
    ticket_no    VARCHAR(32) NOT NULL COMMENT '工单编号',
    user_id      VARCHAR(32) NOT NULL COMMENT '提交人',
    session_id   VARCHAR(32)          DEFAULT NULL COMMENT '关联对话会话ID',
    order_id     VARCHAR(32)          DEFAULT NULL COMMENT '关联订单ID',
    category     VARCHAR(50)          DEFAULT NULL COMMENT '问题分类（AI自动分类）',
    question     TEXT         NOT NULL COMMENT '问题描述',
    status       TINYINT     NOT NULL DEFAULT 0 COMMENT '状态：0待处理 1处理中 2已关闭',
    handler_id   VARCHAR(32)          DEFAULT NULL COMMENT '处理人（管理员ID）',
    reply        TEXT                 DEFAULT NULL COMMENT '处理回复',
    create_time  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    close_time   DATETIME             DEFAULT NULL COMMENT '关闭时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_ticket_no (ticket_no),
    KEY idx_status (status),
    KEY idx_user_id (user_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='客服工单表';

-- 推荐记录表（AI 结果可追溯：来源、模型版本、生成时间）
CREATE TABLE recommend_record (
    id            VARCHAR(32) NOT NULL COMMENT '记录ID',
    user_id       VARCHAR(32) NOT NULL COMMENT '用户ID',
    scene         VARCHAR(30) NOT NULL COMMENT '场景：HOME首页 / AI_ORDER点餐 / SEARCH搜索',
    input_text    VARCHAR(500)         DEFAULT NULL COMMENT '用户自然语言输入（点餐/搜索场景）',
    parsed_condition VARCHAR(500)      DEFAULT NULL COMMENT '解析出的结构化条件（JSON）',
    dish_ids      VARCHAR(500)         DEFAULT NULL COMMENT '推荐菜品ID列表，逗号分隔',
    reason        TEXT                 DEFAULT NULL COMMENT 'AI 推荐理由',
    model_version VARCHAR(50)          DEFAULT NULL COMMENT '模型版本',
    verify_status TINYINT     NOT NULL DEFAULT 0 COMMENT '校验状态：0未校验 1通过 2降级',
    create_time   DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '生成时间',
    PRIMARY KEY (id),
    KEY idx_user_scene (user_id, scene)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='AI 推荐记录表';

-- 饮食分析报告表
CREATE TABLE diet_report (
    id            VARCHAR(32) NOT NULL COMMENT '报告ID',
    user_id       VARCHAR(32) NOT NULL COMMENT '用户ID',
    period_type   TINYINT     NOT NULL COMMENT '周期：7近7天 30近30天',
    start_date    DATE        NOT NULL COMMENT '统计开始日期',
    end_date      DATE        NOT NULL COMMENT '统计结束日期',
    order_count   INT         NOT NULL DEFAULT 0 COMMENT '统计期内订单数',
    structure_data TEXT                DEFAULT NULL COMMENT '饮食结构数据（JSON：高油/高盐/荤素比例等）',
    nutrition_data TEXT                DEFAULT NULL COMMENT '营养估算数据（JSON：热量/蛋白质等）',
    suggestion    TEXT                 DEFAULT NULL COMMENT 'AI 健康建议',
    allergy_risk  TEXT                 DEFAULT NULL COMMENT '过敏原风险提醒',
    model_version VARCHAR(50)          DEFAULT NULL COMMENT '模型版本',
    create_time   DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '生成时间',
    PRIMARY KEY (id),
    KEY idx_user_period (user_id, period_type)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='饮食分析报告表';

-- =====================================================================
-- 五、日志与审计
-- =====================================================================

-- 操作日志表（管理员关键操作、AI 调用、抢单等审计追溯）
CREATE TABLE operation_log (
    id           VARCHAR(32) NOT NULL COMMENT '日志ID',
    user_id      VARCHAR(32)          DEFAULT NULL COMMENT '操作人ID',
    role         VARCHAR(20)          DEFAULT NULL COMMENT '操作人角色',
    module       VARCHAR(50)          DEFAULT NULL COMMENT '模块',
    action       VARCHAR(100)         DEFAULT NULL COMMENT '操作描述',
    target_id    VARCHAR(32)          DEFAULT NULL COMMENT '操作对象ID',
    detail       TEXT                 DEFAULT NULL COMMENT '操作明细（JSON）',
    ip           VARCHAR(64)          DEFAULT NULL COMMENT '来源IP',
    cost_ms      INT                  DEFAULT NULL COMMENT '耗时（毫秒）',
    create_time  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    PRIMARY KEY (id),
    KEY idx_user_id (user_id),
    KEY idx_module (module),
    KEY idx_create_time (create_time)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='操作日志表';

-- =====================================================================
-- 六、系统配置
-- =====================================================================

CREATE TABLE sys_config (
    id           VARCHAR(32) NOT NULL COMMENT '配置ID',
    config_key   VARCHAR(64) NOT NULL COMMENT '配置键',
    config_value VARCHAR(500)         DEFAULT NULL COMMENT '配置值',
    remark       VARCHAR(255)         DEFAULT NULL COMMENT '说明',
    create_time  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_config_key (config_key)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='系统配置表';

-- 首页轮播图表
CREATE TABLE banner (
    id           VARCHAR(32) NOT NULL COMMENT '轮播图ID',
    title        VARCHAR(100)         DEFAULT NULL COMMENT '标题',
    image_url    VARCHAR(255) NOT NULL COMMENT '图片URL',
    link_url     VARCHAR(255)         DEFAULT NULL COMMENT '跳转链接',
    sort         INT         NOT NULL DEFAULT 0 COMMENT '排序值',
    status       TINYINT     NOT NULL DEFAULT 1 COMMENT '状态：0停用 1启用',
    create_time  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='首页轮播图表';

-- =====================================================================
-- 建表完成：共 35 张表
-- =====================================================================
