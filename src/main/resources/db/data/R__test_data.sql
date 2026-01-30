# gatling 부하 테스트 전용 데이터
# 테스트 서버가 아닐 경우 사용 x!!

# username1 ~ username6 계정 생성
# 비밀번호 password123!
INSERT IGNORE INTO users(id, username, password)
WITH RECURSIVE user_number AS (
    SELECT 1 AS n
    UNION ALL
    SELECT n + 1
    FROM user_number
    WHERE n < 6
)
SELECT n, CONCAT('user', n), '{bcrypt}$2a$10$PrHZVi1wqb.7NZmJCZYJ7.EI66rfqnDHyej.M1Z7ne5kf5HZp.xx.' FROM user_number;

# 임시 테이블
CREATE TEMPORARY TABLE IF NOT EXISTS tmp_id(
    id  BIGINT NOT NULL,
    PRIMARY KEY(id)
);

# 세션의 최대 재귀 깊이 설정
SET SESSION cte_max_recursion_depth = 300000;

# 10001 ~ 310000
INSERT IGNORE INTO tmp_id
WITH RECURSIVE user_number AS (
    SELECT 10001 AS n
    UNION ALL
    SELECT n + 1
    FROM user_number
    WHERE n < 310000
)
SELECT n FROM user_number;

# user10001 ~ user310000 계정 생성
# 비밀번호 password123!
INSERT IGNORE INTO users(id, username, password)
SELECT id, CONCAT('user', id), '{bcrypt}$2a$10$PrHZVi1wqb.7NZmJCZYJ7.EI66rfqnDHyej.M1Z7ne5kf5HZp.xx.'
FROM tmp_id;

# user10001 ~ user310000 소유의 그룹을 각각 1개씩 생성
INSERT IGNORE INTO `groups`(id, owner_id, name)
SELECT id, id, CONCAT('group', id)
FROM tmp_id;

# 생성된 그룹의 맴버로 등록
INSERT IGNORE INTO group_members(id, group_id, user_id, role_type)
SELECT id - 10000, id, id, 1
FROM tmp_id;

# 임시 테이블 제거
DROP TEMPORARY TABLE IF EXISTS tmp_id;