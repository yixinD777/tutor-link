-- V17: 批量生成测试家教数据 (300+条)
-- 使用存储过程生成随机但合理的 user + tutor_profile + tutor_subject

DELIMITER //

DROP PROCEDURE IF EXISTS seed_bulk_tutors //

CREATE PROCEDURE seed_bulk_tutors()
BEGIN
    DECLARE i INT DEFAULT 0;
    DECLARE v_user_id BIGINT;
    DECLARE v_profile_id BIGINT;
    DECLARE v_ts_id BIGINT;
    DECLARE v_surname VARCHAR(10);
    DECLARE v_given_name VARCHAR(10);
    DECLARE v_nickname VARCHAR(50);
    DECLARE v_university VARCHAR(100);
    DECLARE v_major VARCHAR(100);
    DECLARE v_enrollment_year INT;
    DECLARE v_edu_level TINYINT;
    DECLARE v_subject_count INT;
    DECLARE v_subject1 BIGINT;
    DECLARE v_subject2 BIGINT;
    DECLARE v_rate_min INT;
    DECLARE v_rate_max INT;
    DECLARE v_rating DECIMAL(2,1);
    DECLARE v_rating_count INT;
    DECLARE v_order_count INT;
    DECLARE v_intro VARCHAR(500);
    DECLARE v_teaching_style VARCHAR(300);
    DECLARE v_province VARCHAR(30);
    DECLARE v_city VARCHAR(30);
    DECLARE v_district VARCHAR(30);
    DECLARE v_lng DECIMAL(10,7);
    DECLARE v_lat DECIMAL(10,7);
    DECLARE v_gender TINYINT;
    DECLARE v_is_online TINYINT;

    -- 起始 ID (在现有 200014 基础上递增)
    SET v_user_id = 210001;
    SET v_profile_id = 310001;
    SET v_ts_id = 410001;

    WHILE i < 320 DO
        -- 随机姓名
        SET v_surname = ELT(1 + FLOOR(RAND() * 50),
            '赵','钱','孙','李','周','吴','郑','王','冯','陈',
            '褚','卫','蒋','沈','韩','杨','朱','秦','尤','许',
            '何','吕','施','张','孔','曹','严','华','金','魏',
            '陶','姜','戚','谢','邹','喻','柏','窦','章','苏',
            '潘','葛','范','彭','鲁','韦','昌','马','苗','方');
        SET v_given_name = ELT(1 + FLOOR(RAND() * 80),
            '伟','芳','娜','敏','静','丽','强','磊','洋','艳',
            '勇','军','杰','娟','涛','明','超','秀英','霞','平',
            '刚','桂英','文','云','建华','玲','建国','建军','佳','欣',
            '宇','辰','涵','博','翔','皓','然','睿','泽','逸',
            '萱','琳','婷','雪','颖','璐','瑶','菲','蕾','晴',
            '浩','昊','鹏','辉','斌','威','峰','旭','晨','阳',
            '思','雨','梦','诗','雅','怡','若','语','乐','瑞',
            '一诺','子涵','梓轩','紫萱','可欣','浩然','皓轩','诗涵','雨桐','欣怡');
        SET v_nickname = CONCAT(v_surname, v_given_name);

        -- 随机学校
        SET v_university = ELT(1 + FLOOR(RAND() * 35),
            '北京大学','清华大学','复旦大学','上海交通大学','浙江大学',
            '南京大学','中国科学技术大学','华中科技大学','武汉大学','中山大学',
            '西安交通大学','哈尔滨工业大学','北京师范大学','四川大学','同济大学',
            '东南大学','中国人民大学','北京航空航天大学','南开大学','天津大学',
            '厦门大学','吉林大学','山东大学','中南大学','大连理工大学',
            '华南理工大学','北京理工大学','重庆大学','电子科技大学','湖南大学',
            '华东师范大学','北京邮电大学','中央财经大学','中国政法大学','北京外国语大学');

        -- 随机专业
        SET v_major = ELT(1 + FLOOR(RAND() * 30),
            '数学与应用数学','物理学','化学','生物科学','计算机科学与技术',
            '软件工程','电子信息工程','英语','汉语言文学','历史学',
            '哲学','经济学','金融学','会计学','法学',
            '心理学','教育学','新闻传播学','建筑学','土木工程',
            '机械工程','电气工程','材料科学','环境科学','药学',
            '临床医学','口腔医学','护理学','统计学','人工智能');

        SET v_enrollment_year = 2020 + FLOOR(RAND() * 5); -- 2020-2024
        SET v_edu_level = ELT(1 + FLOOR(RAND() * 10),
            1,1,1,1,1,1,1,2,2,3); -- 本科70% 硕士20% 博士10%
        SET v_gender = ELT(1 + FLOOR(RAND() * 3), 1, 1, 2); -- 男2/3 女1/3

        -- 随机地区 (8个城市)
        CASE 1 + FLOOR(RAND() * 8)
            WHEN 1 THEN
                SET v_province='北京市'; SET v_city='北京市';
                SET v_district = ELT(1+FLOOR(RAND()*6),'海淀区','朝阳区','西城区','东城区','丰台区','昌平区');
                SET v_lng = 116.30 + RAND()*0.25; SET v_lat = 39.85 + RAND()*0.15;
            WHEN 2 THEN
                SET v_province='上海市'; SET v_city='上海市';
                SET v_district = ELT(1+FLOOR(RAND()*6),'浦东新区','徐汇区','杨浦区','虹口区','静安区','闵行区');
                SET v_lng = 121.40 + RAND()*0.20; SET v_lat = 31.15 + RAND()*0.12;
            WHEN 3 THEN
                SET v_province='广东省'; SET v_city='广州市';
                SET v_district = ELT(1+FLOOR(RAND()*4),'天河区','海珠区','越秀区','番禺区');
                SET v_lng = 113.25 + RAND()*0.15; SET v_lat = 23.00 + RAND()*0.10;
            WHEN 4 THEN
                SET v_province='广东省'; SET v_city='深圳市';
                SET v_district = ELT(1+FLOOR(RAND()*4),'南山区','福田区','罗湖区','宝安区');
                SET v_lng = 113.90 + RAND()*0.15; SET v_lat = 22.50 + RAND()*0.10;
            WHEN 5 THEN
                SET v_province='浙江省'; SET v_city='杭州市';
                SET v_district = ELT(1+FLOOR(RAND()*4),'西湖区','拱墅区','上城区','滨江区');
                SET v_lng = 120.05 + RAND()*0.15; SET v_lat = 30.20 + RAND()*0.10;
            WHEN 6 THEN
                SET v_province='江苏省'; SET v_city='南京市';
                SET v_district = ELT(1+FLOOR(RAND()*4),'鼓楼区','玄武区','栖霞区','江宁区');
                SET v_lng = 118.70 + RAND()*0.15; SET v_lat = 32.00 + RAND()*0.10;
            WHEN 7 THEN
                SET v_province='湖北省'; SET v_city='武汉市';
                SET v_district = ELT(1+FLOOR(RAND()*4),'洪山区','武昌区','江岸区','江汉区');
                SET v_lng = 114.25 + RAND()*0.15; SET v_lat = 30.45 + RAND()*0.10;
            WHEN 8 THEN
                SET v_province='四川省'; SET v_city='成都市';
                SET v_district = ELT(1+FLOOR(RAND()*4),'武侯区','锦江区','青羊区','高新区');
                SET v_lng = 104.00 + RAND()*0.15; SET v_lat = 30.55 + RAND()*0.10;
        END CASE;

        -- 随机时薪 (5000-20000 分 = ¥50-200)
        SET v_rate_min = 5000 + FLOOR(RAND() * 8000); -- 5000-13000
        SET v_rate_max = v_rate_min + 2000 + FLOOR(RAND() * 5000); -- +2000~7000
        IF v_rate_max > 20000 THEN SET v_rate_max = 20000; END IF;

        -- 加权评分分布 (4.0-5.0, 高分偏多)
        SET v_rating = ELT(1 + FLOOR(RAND() * 20),
            4.0,4.0,4.1,4.1,4.2,4.2,4.3,4.3,4.3,4.4,
            4.4,4.5,4.5,4.5,4.6,4.6,4.7,4.8,4.9,5.0);
        SET v_rating_count = FLOOR(RAND() * 50);
        SET v_order_count = FLOOR(RAND() * 40);

        -- 随机 intro
        SET v_intro = ELT(1 + FLOOR(RAND() * 10),
            CONCAT(v_university, '在读，擅长因材施教，帮助学生夯实基础提升成绩'),
            CONCAT('有丰富家教经验，教学耐心细致，善于激发学习兴趣'),
            CONCAT(v_university, '毕业，专注中高考辅导，帮助学生突破瓶颈'),
            '注重思维训练和方法总结，让学生举一反三',
            CONCAT('来自', v_university, '，认真负责，善于与学生沟通'),
            '教学风格活泼，善于用生活中的例子解释抽象概念',
            CONCAT(v_university, '学霸，竞赛获奖经验丰富，擅长拔高训练'),
            '辅导过多名学生提分显著，注重个性化教学方案',
            '讲课条理清晰，善于归纳考点，帮助学生高效备考',
            CONCAT(v_university, '优秀毕业生，善于培养学习习惯和自主学习能力'));

        SET v_teaching_style = ELT(1 + FLOOR(RAND() * 8),
            '循序渐进，注重基础', '启发式教学，培养思维',
            '情景教学，寓教于乐', '项目驱动，实战为主',
            '分层教学，因材施教', '错题归因，精准提升',
            '互动讨论，深度理解', '建模思维，举一反三');

        SET v_is_online = IF(RAND() > 0.4, 1, 0); -- 60%在线

        -- 插入 user
        INSERT INTO `user` (`id`, `nickname`, `gender`, `role`, `status`, `is_deleted`, `create_time`, `update_time`)
        VALUES (v_user_id, v_nickname, v_gender, 2, 1, 0, NOW(), NOW());

        -- 插入 tutor_profile
        INSERT INTO `tutor_profile` (
            `id`, `user_id`, `real_name`, `university`, `major`, `enrollment_year`, `education_level`,
            `certification_status`, `certification_time`, `intro`, `teaching_style`,
            `hourly_rate_min`, `hourly_rate_max`, `avg_rating`, `rating_count`, `order_count`,
            `province`, `city`, `district`, `longitude`, `latitude`, `is_online`,
            `is_deleted`, `create_time`, `update_time`
        ) VALUES (
            v_profile_id, v_user_id, v_nickname, v_university, v_major, v_enrollment_year, v_edu_level,
            2, NOW(), v_intro, v_teaching_style,
            v_rate_min, v_rate_max, v_rating, v_rating_count, v_order_count,
            v_province, v_city, v_district, v_lng, v_lat, v_is_online,
            0, NOW(), NOW()
        );

        -- 插入 1-2 个 tutor_subject
        SET v_subject_count = IF(RAND() > 0.5, 2, 1);
        SET v_subject1 = ELT(1 + FLOOR(RAND() * 6), 1, 2, 3, 4, 5, 6);

        INSERT INTO `tutor_subject` (`id`, `tutor_user_id`, `subject_id`, `grade_range`, `is_deleted`, `create_time`, `update_time`)
        VALUES (v_ts_id, v_user_id, v_subject1,
            ELT(1 + FLOOR(RAND() * 4), 'G1-G6', 'G7-G9', 'G10-G12', 'G7-G9,G10-G12'),
            0, NOW(), NOW());
        SET v_ts_id = v_ts_id + 1;

        IF v_subject_count = 2 THEN
            -- 第二个科目不能和第一个相同
            SET v_subject2 = v_subject1;
            WHILE v_subject2 = v_subject1 DO
                SET v_subject2 = ELT(1 + FLOOR(RAND() * 6), 1, 2, 3, 4, 5, 6);
            END WHILE;
            INSERT INTO `tutor_subject` (`id`, `tutor_user_id`, `subject_id`, `grade_range`, `is_deleted`, `create_time`, `update_time`)
            VALUES (v_ts_id, v_user_id, v_subject2,
                ELT(1 + FLOOR(RAND() * 4), 'G1-G6', 'G7-G9', 'G10-G12', 'G7-G9,G10-G12'),
                0, NOW(), NOW());
            SET v_ts_id = v_ts_id + 1;
        END IF;

        SET v_user_id = v_user_id + 1;
        SET v_profile_id = v_profile_id + 1;
        SET i = i + 1;
    END WHILE;
END //

DELIMITER ;

CALL seed_bulk_tutors();

DROP PROCEDURE IF EXISTS seed_bulk_tutors;
