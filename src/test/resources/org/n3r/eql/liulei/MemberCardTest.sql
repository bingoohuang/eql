-- [beforeClass]
DROP TABLE IF EXISTS member_card_week_times;
CREATE TABLE member_card_week_times (
  MBR_CARD_ID bigint(20) NOT NULL COMMENT '会员卡ID',
  START_TIME datetime NOT NULL COMMENT '开始时间',
  END_TIME datetime NOT NULL COMMENT '结束时间',
  TIMES smallint(6) NOT NULL COMMENT '初始次数',
  AVAIL_TIMES smallint(6) NOT NULL COMMENT '剩余次数',
  CREATE_TIME datetime NOT NULL COMMENT '创建时间',
  UPDATE_TIME datetime NOT NULL COMMENT '修改时间',
  KEY IDX_member_card_week_times_1 (MBR_CARD_ID,START_TIME)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=COMPACT
  COMMENT='将计时卡按照一定周期转换成多张计次卡，使得计时卡的使用逻辑和计次卡一样';

-- [beforeEachTest]
truncate table member_card_week_times;

-- insert into member_card_week_times ( MBR_CARD_ID, START_TIME, END_TIME, TIMES, UPDATE_TIME, AVAIL_TIMES, CREATE_TIME)
-- values ( '174268439299366912', '2016-08-12 00:00:00', '2016-08-15 00:00:00', '-1', NOW(), '-1', NOW());
-- 变化字段:START_TIME 每次增加7天,END_TIME每次增加7天
-- 一年5200条记录

-- [testIterateAddRecords iterate]
insert into member_card_week_times (MBR_CARD_ID, START_TIME, END_TIME, TIMES, UPDATE_TIME, AVAIL_TIMES, CREATE_TIME)
values ('#mbrCardId#', '#startTime#', '#endTime#', -1, NOW(), -1, NOW());

-- [testAddRecords]
insert into member_card_week_times (MBR_CARD_ID, START_TIME, END_TIME, TIMES, UPDATE_TIME, AVAIL_TIMES, CREATE_TIME)
values ('#mbrCardId#', '#startTime#', '#endTime#', -1, NOW(), -1, NOW());

-- [testInsertMultipleRows]
insert into member_card_week_times (MBR_CARD_ID, START_TIME, END_TIME, TIMES, UPDATE_TIME, AVAIL_TIMES, CREATE_TIME)
values
-- for item=card index=index collection=_1 separator=,
('#card.mbrCardId#', '#card.startTime#', '#card.endTime#', -1, NOW(), -1, NOW())
-- end
;

-- [countRecords returnType=int]
select count(*) from member_card_week_times where MBR_CARD_ID = '##';
