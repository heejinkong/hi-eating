-- Remove only chat data and commit.
--
-- Use this if you want to confirm that old chat data is actually gone before
-- reseeding with database/chat-load-test-reset.sql.

SET SERVEROUTPUT ON

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

SELECT COUNT(*) AS chat_rooms FROM chat_rooms;
SELECT COUNT(*) AS chat_messages FROM chat_messages;
SELECT COUNT(*) AS chat_admin_presences FROM chat_admin_presences;
