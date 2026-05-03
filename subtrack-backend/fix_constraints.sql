-- Drop old auto-named constraints (safe — skips if not found)
ALTER TABLE reminders     DROP CONSTRAINT IF EXISTS uk8fhv182q5syvo9eci4ivvtiur;
ALTER TABLE subscriptions DROP CONSTRAINT IF EXISTS ukf9024xthtwaye3x7v8ffhhjj8;
ALTER TABLE users         DROP CONSTRAINT IF EXISTS uk6dotkott2kjsp8vw4d0m25fb7;

-- Drop new-named ones too so Hibernate recreates them fresh
ALTER TABLE reminders     DROP CONSTRAINT IF EXISTS uq_reminder_sub_type_date;
ALTER TABLE subscriptions DROP CONSTRAINT IF EXISTS uq_user_subscription_name;
ALTER TABLE users         DROP CONSTRAINT IF EXISTS uq_users_email;
