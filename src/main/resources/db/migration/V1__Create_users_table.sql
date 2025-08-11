-- ユーザーテーブル
CREATE TABLE users (
    id SERIAL PRIMARY KEY, -- ユーザーID
    company_name VARCHAR(255) NOT NULL, -- 企業名
    name VARCHAR(255) NOT NULL, -- 氏名
    email VARCHAR(255) UNIQUE NOT NULL, -- メールアドレス
    password VARCHAR(255) NOT NULL, -- パスワード
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(), -- 作成日時
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW() -- 更新日時
);