-- Reset and seed deterministic chat load-test data.
--
-- Run this before each branch/scenario run if the scenario writes chat messages.
-- Defaults:
--   admin@load.test / password
--   user1@load.test ... user100@load.test / password
--
-- SQL*Plus or SQLcl overrides:
--   DEFINE K6_USER_COUNT = 200
--   DEFINE K6_MESSAGES_PER_ROOM = 1000
--   @database/chat-load-test-reset.sql

SET DEFINE ON
SET SERVEROUTPUT ON

DEFINE K6_USER_COUNT = 100
DEFINE K6_MESSAGES_PER_ROOM = 500
DEFINE K6_DOMAIN = load.test
DEFINE K6_PASSWORD_HASH = $2a$10$BL2NX6dVxHmawTiiGXz1SePDCkEiiQkYqS01aw2NLfVOHzZuwqTDe

PROMPT Cleaning existing chat data...

DECLARE
  v_old_rooms NUMBER;
  v_old_messages NUMBER;
  v_old_presences NUMBER;
  v_deleted_rooms NUMBER;
  v_deleted_messages NUMBER;
  v_deleted_presences NUMBER;
BEGIN
  SELECT COUNT(*) INTO v_old_messages FROM chat_messages;
  SELECT COUNT(*) INTO v_old_presences FROM chat_admin_presences;
  SELECT COUNT(*) INTO v_old_rooms FROM chat_rooms;

  DELETE FROM chat_messages;
  v_deleted_messages := SQL%ROWCOUNT;

  DELETE FROM chat_admin_presences;
  v_deleted_presences := SQL%ROWCOUNT;

  DELETE FROM chat_rooms;
  v_deleted_rooms := SQL%ROWCOUNT;

  COMMIT;

  DBMS_OUTPUT.PUT_LINE(
    'Deleted chat data. rooms=' || v_deleted_rooms || '/' || v_old_rooms ||
    ', messages=' || v_deleted_messages || '/' || v_old_messages ||
    ', presences=' || v_deleted_presences || '/' || v_old_presences
  );
END;
/

PROMPT Seeding deterministic chat load-test data...

DECLARE
  v_admin_id users.id%TYPE;
  v_user_id users.id%TYPE;
  v_room_id chat_rooms.id%TYPE;
  v_email users.email%TYPE;
  v_content VARCHAR2(1000);
BEGIN
  MERGE INTO users u
  USING (
    SELECT
      'admin@&K6_DOMAIN' AS email,
      '&K6_PASSWORD_HASH' AS password,
      'K6 Admin' AS name,
      'OTHER' AS gender,
      DATE '1990-01-01' AS birth
    FROM dual
  ) s
  ON (u.email = s.email)
  WHEN MATCHED THEN
    UPDATE SET
      u.password = s.password,
      u.name = s.name,
      u.gender = s.gender,
      u.birth = s.birth,
      u.deleted_at = NULL,
      u.updated_at = SYSTIMESTAMP
  WHEN NOT MATCHED THEN
    INSERT (email, password, name, gender, birth, created_at)
    VALUES (s.email, s.password, s.name, s.gender, s.birth, SYSTIMESTAMP);

  SELECT id INTO v_admin_id
  FROM users
  WHERE email = 'admin@&K6_DOMAIN';

  DELETE FROM user_auth
  WHERE user_id = v_admin_id;

  INSERT INTO user_auth (user_id, auth, created_at)
  VALUES (v_admin_id, 'ROLE_ADMIN', SYSTIMESTAMP);

  INSERT INTO chat_admin_presences (
    admin_id,
    status,
    last_user_message_at,
    last_connected_at,
    created_at,
    updated_at
  )
  VALUES (
    v_admin_id,
    'ONLINE',
    TIMESTAMP '2026-01-01 00:00:00',
    SYSTIMESTAMP,
    SYSTIMESTAMP,
    SYSTIMESTAMP
  );

  FOR i IN 1..&K6_USER_COUNT LOOP
    v_email := 'user' || i || '@&K6_DOMAIN';

    MERGE INTO users u
    USING (
      SELECT
        v_email AS email,
        '&K6_PASSWORD_HASH' AS password,
        'K6 User ' || i AS name,
        CASE MOD(i, 3)
          WHEN 0 THEN 'OTHER'
          WHEN 1 THEN 'MALE'
          ELSE 'FEMALE'
        END AS gender,
        DATE '1995-01-01' + MOD(i, 3650) AS birth
      FROM dual
    ) s
    ON (u.email = s.email)
    WHEN MATCHED THEN
      UPDATE SET
        u.password = s.password,
        u.name = s.name,
        u.gender = s.gender,
        u.birth = s.birth,
        u.deleted_at = NULL,
        u.updated_at = SYSTIMESTAMP
    WHEN NOT MATCHED THEN
      INSERT (email, password, name, gender, birth, created_at)
      VALUES (s.email, s.password, s.name, s.gender, s.birth, SYSTIMESTAMP);

    SELECT id INTO v_user_id
    FROM users
    WHERE email = v_email;

    DELETE FROM user_auth
    WHERE user_id = v_user_id;

    INSERT INTO user_auth (user_id, auth, created_at)
    VALUES (v_user_id, 'ROLE_USER', SYSTIMESTAMP);

    INSERT INTO chat_rooms (
      user_id,
      assigned_admin_id,
      status,
      user_unread_count,
      admin_unread_count,
      last_message_at,
      created_at,
      updated_at
    )
    VALUES (
      v_user_id,
      v_admin_id,
      'OPEN',
      0,
      0,
      TIMESTAMP '2026-01-01 00:00:00' + NUMTODSINTERVAL(&K6_MESSAGES_PER_ROOM, 'SECOND'),
      TIMESTAMP '2026-01-01 00:00:00',
      SYSTIMESTAMP
    )
    RETURNING id INTO v_room_id;

    FOR j IN 1..&K6_MESSAGES_PER_ROOM LOOP
      IF MOD(j, 5) = 0 THEN
        v_content := 'seed admin reply roomUser=' || i || ' msg=' || j;
        INSERT INTO chat_messages (
          room_id,
          sender_id,
          sender_type,
          content,
          created_at
        )
        VALUES (
          v_room_id,
          v_admin_id,
          'ADMIN',
          v_content,
          TIMESTAMP '2026-01-01 00:00:00' + NUMTODSINTERVAL(j, 'SECOND')
        );
      ELSE
        v_content := 'seed user message roomUser=' || i || ' msg=' || j;
        INSERT INTO chat_messages (
          room_id,
          sender_id,
          sender_type,
          content,
          created_at
        )
        VALUES (
          v_room_id,
          v_user_id,
          'USER',
          v_content,
          TIMESTAMP '2026-01-01 00:00:00' + NUMTODSINTERVAL(j, 'SECOND')
        );
      END IF;
    END LOOP;
  END LOOP;

  COMMIT;
  DBMS_OUTPUT.PUT_LINE(
    'Seed complete. users=' || &K6_USER_COUNT ||
    ', rooms=' || &K6_USER_COUNT ||
    ', messages=' || (&K6_USER_COUNT * &K6_MESSAGES_PER_ROOM)
  );
EXCEPTION
  WHEN OTHERS THEN
    ROLLBACK;
    DBMS_OUTPUT.PUT_LINE('Seed failed: ' || SQLERRM);
    RAISE;
END;
/

SELECT COUNT(*) AS k6_users
FROM users
WHERE email = 'admin@&K6_DOMAIN'
   OR email IN (
     SELECT 'user' || LEVEL || '@&K6_DOMAIN'
     FROM dual
     CONNECT BY LEVEL <= &K6_USER_COUNT
   );

SELECT COUNT(*) AS chat_rooms FROM chat_rooms;
SELECT COUNT(*) AS chat_messages FROM chat_messages;
