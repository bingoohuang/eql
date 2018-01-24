-- [createTables]
drop table if exists tt_f_course;
CREATE TABLE tt_f_course (
  COURSE_ID bigint(20) NOT NULL COMMENT '课ID',
  COURSE_NAME varchar(100) NOT NULL COMMENT '课名称',
  SHORT_NAME varchar(45) DEFAULT NULL COMMENT '课的简称',
  COURSE_PIC_URL varchar(100) NOT NULL COMMENT '课的图片',
  DEFAULT_NUM int(11) DEFAULT NULL COMMENT '默认上课人数',
  DEFAULT_TIMESPAN int(11) DEFAULT NULL COMMENT '默认一节课时间，单位：分钟',
  SCORE decimal(3, 2) DEFAULT NULL COMMENT '评价分数',
  BRIEF varchar(1000) DEFAULT NULL COMMENT '课程简介',
  CREATE_TIME datetime NOT NULL COMMENT '创建时间',
  UPDATE_TIME datetime NOT NULL COMMENT '修改时间',
  STATE char(1) DEFAULT NULL COMMENT '0:删除的  1：有效的 2：有效不可见（目前就是私教）',
  COURSE_TRAIT varchar(50) DEFAULT NULL COMMENT '课程特点',
  APPROPRIATE_CROWD varchar(50) DEFAULT NULL COMMENT '适合人群',
  NOTES varchar(50) DEFAULT NULL COMMENT '特征',
  COURSE_EFFICACY varchar(200) DEFAULT NULL COMMENT '课程功效',
  TABOO varchar(50) DEFAULT NULL COMMENT '禁忌',
  ACTION_FEATURE varchar(100) DEFAULT NULL COMMENT '动作特点\n',
  PRIMARY KEY (COURSE_ID)
);

drop table if exists tt_d_course_type;
CREATE TABLE tt_d_course_type (
  COURSE_TYPE_ID bigint(20) NOT NULL COMMENT '种类ID',
  COURSE_TYPE_NAME varchar(20) NOT NULL COMMENT '种类名称',
  SHOW_ORDER int(11) NOT NULL COMMENT '显示顺序',
  SUBSCRIBE_TYPE tinyint(4) NOT NULL COMMENT '1：排的课；2：约的课',
  MIN_SUBSCRIPTIONS int(10) DEFAULT '0',
  STATE tinyint(4) NOT NULL COMMENT '课分类使用状态：1 — 正常在用；0 — 停用；',
  CREATE_TIME datetime NOT NULL COMMENT '创建时间',
  UPDATE_TIME datetime NOT NULL COMMENT '修改时间',
  PRIMARY KEY (COURSE_TYPE_ID)
);

drop table if exists tt_f_coach;
CREATE TABLE tt_f_coach (
  COACH_ID bigint(20) NOT NULL COMMENT '教练ID',
  USER_ID bigint(20) NOT NULL DEFAULT '0' COMMENT '用户ID',
  STAFF_ID bigint(20) NOT NULL COMMENT '员工ID',
  COACH_NAME varchar(45) DEFAULT NULL COMMENT '该字段已经弃用',
  MOBILE bigint(20) DEFAULT NULL COMMENT '该字段已经弃用',
  MOBILE_VISIBLE char(1) NOT NULL DEFAULT '0' COMMENT '手机号码对会员是否可见：0-不可见；1-可见',
  CREATE_TIME datetime NOT NULL COMMENT '创建时间',
  UPDATE_TIME datetime NOT NULL COMMENT '更新时间',
  SEX char(1) DEFAULT NULL COMMENT '该字段已经弃用',
  WX_NO varchar(45) DEFAULT NULL COMMENT '微信号',
  SKILLS varchar(45) DEFAULT NULL COMMENT '擅长',
  ACHIEVEMENTS varchar(45) DEFAULT NULL COMMENT '教练成就',
  ICON varchar(100) DEFAULT NULL COMMENT '该字段已经弃用',
  STATE char(1) NOT NULL COMMENT '在职状态',
  SCORE decimal(3, 2) DEFAULT NULL COMMENT '评价分数',
  BRIEF text COMMENT '个人简介',
  ABSTRACT text COMMENT '摘要信息',
  SUB_SELF char(1) DEFAULT NULL COMMENT '该字段已经弃用、暂定',
  NO tinyint(4) DEFAULT '100' COMMENT '该字段已经弃用',
  CARD_DEDU_TIMES tinyint(4) DEFAULT '5' COMMENT '该字段已经弃用',
  PRIZE varchar(1000) DEFAULT NULL,
  SPECIALITY varchar(1000) DEFAULT NULL COMMENT '擅长',
  COACH_YEAR varchar(1000) DEFAULT NULL COMMENT '执教年数',
  PRIMARY KEY (USER_ID, CREATE_TIME)
);

drop table if exists tt_f_schedule;
CREATE TABLE tt_f_schedule (
  SCHEDULE_ID bigint(20) NOT NULL COMMENT '排期ID',
  SCHEDULE_NAME varchar(45) DEFAULT NULL COMMENT '课程名称',
  COURSE_TYPE_ID bigint(20) NOT NULL DEFAULT '1001' COMMENT '课种类ID',
  COURSE_ID bigint(20) NOT NULL COMMENT '课ID',
  COACH_ID bigint(20) NOT NULL COMMENT '教练ID',
  COACH_NAME varchar(45) DEFAULT NULL COMMENT '教练名称',
  ROOM_ID bigint(20) DEFAULT NULL COMMENT '教室ID',
  ROOM_NAME varchar(45) DEFAULT NULL,
  DAY tinyint(4) NOT NULL COMMENT '星期几',
  START_TIME datetime NOT NULL COMMENT '授课日期，格式：YYYY-MM-DD HH24:MI:SS',
  END_TIME datetime NOT NULL COMMENT '结束时间',
  EXPECT_NUM smallint(6) NOT NULL DEFAULT '0' COMMENT '可预订的总人数',
  RESERVED_NUM smallint(6) DEFAULT '0' COMMENT '教练预留位置',
  REAL_NUM smallint(6) DEFAULT '0' COMMENT '已预订的人数',
  OFF_NUM tinyint(4) NOT NULL DEFAULT '0' COMMENT '免费课的标志：0为免费课，其它为非免费课',
  CREATE_TIME datetime DEFAULT NULL COMMENT '创建时间',
  UPDATE_TIME datetime DEFAULT NULL COMMENT '修改时间',
  STATE char(1) DEFAULT NULL COMMENT '排期状态  0:失效 1:有效',
  QUEUING_NUM int(11) NOT NULL DEFAULT '0' COMMENT '排队人数',
  PRIMARY KEY (SCHEDULE_ID)
);

drop table if exists tt_f_schedule_queuing;

CREATE TABLE tt_f_schedule_queuing (
  QUEUING_ID bigint(20) NOT NULL COMMENT '排队ID',
  SCHEDULE_ID bigint(20) NOT NULL COMMENT '排期ID',
  USER_ID bigint(20) NOT NULL COMMENT 'USER_ID',
  COACH_ID bigint(20) NOT NULL COMMENT '教练ID，用于校验教练是否变更',
  START_TIME datetime NOT NULL COMMENT '授课开始时间，用于校验时间是否调整',
  END_TIME datetime NOT NULL COMMENT '授课结束时间，用于校验时间是否调整',
  QUEUING_SEQ tinyint(4) NOT NULL COMMENT '排队号，注意前面排队的人取消/预订/失败时，修正序号后面的排队号',
  STATE varchar(20) NOT NULL COMMENT '状态：排队中，已取消，预订中（过渡状态），已预订，已失败',
  FAIL_CAUSE varchar(100) DEFAULT NULL COMMENT '排队失败原因',
  SUBSCRIBE_ID bigint(20) DEFAULT NULL COMMENT '已预订时，关联的订单ID',
  PAY_TYPE varchar(20) NOT NULL COMMENT '支付方式：体验券，会员卡，储值卡',
  PAY_ID bigint(20) NOT NULL COMMENT '支付ID：体验券ID，会员卡ID或者储值卡ID',
  PAY_VALUE decimal(11, 2) NOT NULL DEFAULT '0.00' COMMENT '支付金额，次数或者钱，用于我的预定展示参考',
  PRIMARY KEY (QUEUING_ID)
);
  
-- [benchmark]
   SELECT t1.SCHEDULE_ID
         ,t1.SCHEDULE_NAME
         ,t1.COURSE_ID
         ,t1.COACH_ID
         ,t1.SCHEDULE_NAME
         ,t3.COURSE_TYPE_NAME
         ,t3.SUBSCRIBE_TYPE
         ,t1.COACH_NAME coachName
         ,t1.START_TIME
         ,t1.END_TIME
         ,t1.EXPECT_NUM -  t1.REAL_NUM remainingNum
         ,t1.REAL_NUM scheduleTotalBooked
         ,t1.EXPECT_NUM scheduleTotalMaxNum
         ,t1.RESERVED_NUM reservedNum
         ,t1.OFF_NUM
         ,t1.REAL_NUM
         ,t2.COURSE_PIC_URL courseImg
         ,IFNULL(SQ.QUEUING_SEQ,'0') queuingSeq
         ,IFNULL(t1.QUEUING_NUM,'0') queuingNum
     FROM tt_f_course t2
         ,tt_d_course_type t3
         ,tt_f_coach t4
         ,tt_f_schedule t1
    LEFT JOIN TT_F_SCHEDULE_QUEUING SQ ON SQ.SCHEDULE_ID = t1.SCHEDULE_ID
     AND SQ.USER_ID = '#userId#'
     AND SQ.STATE = '排队中'
   WHERE t1.STATE = '1'
     AND t2.STATE != '0'
     AND t4.STATE = '1'
     -- isNotNull(courseTypeId)
     AND t1.COURSE_TYPE_ID = '#courseTypeId#'
     -- end
     -- isNotNull(coachId)
     AND t1.COACH_ID = '#coachId#'
     -- end
     AND t1.START_TIME > '#startDate#'
     AND t1.START_TIME < '#endDate#'
     AND t1.COURSE_ID = t2.COURSE_ID
     AND t1.COACH_ID = t4.COACH_ID
     AND t1.COURSE_TYPE_ID = t3.COURSE_TYPE_ID
ORDER BY t1.START_TIME