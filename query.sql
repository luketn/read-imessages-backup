SELECT
    handle.id as sender,
    message.is_from_me,
    message.text,
    message.date

FROM chat_message_join
         INNER JOIN message
                    ON message.rowid = chat_message_join.message_id
         INNER JOIN handle
                    ON handle.rowid = message.handle_id
WHERE chat_message_join.chat_id = ?