# type 1: 그룹 초대
INSERT IGNORE INTO notification_messages(id, type, title, message) VALUES
    (1, 1, '{groupName} 초대 도착', '{inviter}님이 {groupName}에 초대했어요');