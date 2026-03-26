-- Test members (local profile only)
INSERT INTO member (id, provider, provider_id, nickname, therapy_area, role, created_at, updated_at)
VALUES (100, 'KAKAO', 'test_pending', 'pending_user', 'SPEECH', 'PENDING', NOW(), NOW())
ON CONFLICT (id) DO NOTHING;

INSERT INTO member (id, provider, provider_id, nickname, therapy_area, role, created_at, updated_at)
VALUES (101, 'KAKAO', 'test_member', 'member_user', 'MUSIC', 'MEMBER', NOW(), NOW())
ON CONFLICT (id) DO NOTHING;

INSERT INTO member (id, provider, provider_id, nickname, therapy_area, role, created_at, updated_at)
VALUES (102, 'KAKAO', 'test_admin', 'admin_user', 'ART', 'ADMIN', NOW(), NOW())
ON CONFLICT (id) DO NOTHING;
