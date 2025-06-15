INSERT INTO board (
    title, content, board_attachment_enabled,
    created_person, created_date,
    modified_person, modified_date,
    category, is_deleted, pinned, writer
)
VALUES
-- 1. 자유게시판 이용 안내
('자유게시판 이용 안내',
 '자유게시판은 회원 여러분이 자유롭게 소통하고 일상, 관심사, 유머 등 다양한 주제로 이야기를 나누는 공간입니다.\n\n✅ 이용 수칙\n- 비방, 욕설 금지\n- 광고/도배 금지\n- 정치/종교 주제 자제\n\n즐거운 커뮤니티 문화 조성에 동참해주세요.',
 FALSE, 'matchon2025@gmail.com', NOW(), 'matchon2025@gmail.com', NOW(),
 'FREEBOARD', FALSE, TRUE, 11),

-- 2. 자유게시판 운영 방침 안내
('자유게시판 운영 방침 안내',
 '안녕하세요, 커뮤니티 운영팀입니다.\n\n자유게시판은 유저 간 자유로운 의견 교류를 위한 공간이지만, 건전한 커뮤니티 유지를 위해 몇 가지 운영 원칙을 안내드립니다.\n\n📌 주요 방침\n- 반복 신고 접수 시 운영팀 개입 및 글/댓글 삭제 조치 가능\n- 타인 명예 훼손/비하/허위사실 유포 시 게시물 삭제 및 제재\n- 논쟁성 글, 분쟁 유도 행위 반복 시 이용 제한 경고\n\n자유에는 책임이 따릅니다. 원활한 커뮤니티 운영을 위해 회원 여러분의 협조를 부탁드립니다.\n\n감사합니다.',
 FALSE, 'matchon2025@gmail.com', NOW(), 'matchon2025@gmail.com', NOW(),
 'FREEBOARD', FALSE, TRUE, 11),

-- 3. 공지사항 안내
('공지사항 게시판 안내',
 '이곳은 사이트 운영에 대한 중요한 공지사항을 전달하는 공식 공간입니다.\n\n📌 주요 안내\n- 서비스 점검\n- 정책 변경\n- 이벤트 공지 등\n\n일반 회원은 작성이 불가능하며, 운영팀 공지 전용 게시판입니다.',
 FALSE, 'matchon2025@gmail.com', NOW(), 'matchon2025@gmail.com', NOW(),
 'ANNOUNCEMENT', FALSE, TRUE, 11),

-- 4. 정보게시판 안내
('정보게시판 이용 안내',
 '정보게시판은 유용한 자료나 팁을 공유하는 공간입니다.\n\n✅ 공유 예시\n- 기술 정보\n- 학습 자료\n- 생활 팁 등\n\n🚫 금지\n- 무단 복제\n- 허위 정보\n- 광고/홍보 게시물\n\n신뢰 있는 정보 공유에 협조 부탁드립니다.',
 FALSE, 'matchon2025@gmail.com', NOW(), 'matchon2025@gmail.com', NOW(),
 'INFORMATION', FALSE, TRUE, 11),

-- 5. 국내/해외 축구 게시판 안내
('국내/해외 축구 게시판 이용 안내',
 'K리그, 대표팀, 프리미어리그 등 다양한 축구 이야기를 나누는 공간입니다.\n\n⚽ 게시물 예시\n- 경기 리뷰\n- 선수 이적 소식\n- 팬 토크/직관 후기 등\n\n📌 유의 사항\n- 팀/선수 비방 금지\n- 과열 논쟁/인신공격 금지\n- 불법 스트리밍 링크 금지\n\n건강한 축구 커뮤니티가 되도록 함께해 주세요.',
 FALSE, 'matchon2025@gmail.com', NOW(), 'matchon2025@gmail.com', NOW(),
 'FOOTBALL_TALK', FALSE, TRUE, 11);
