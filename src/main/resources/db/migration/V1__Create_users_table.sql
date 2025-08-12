-- citext拡張を有効化
CREATE EXTENSION IF NOT EXISTS citext;

-- ユーザーテーブル
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY, -- ユーザーID
    company_name VARCHAR(255) NOT NULL, -- 企業名
    name VARCHAR(255) NOT NULL, -- 氏名
    email CITEXT UNIQUE NOT NULL, -- メールアドレス（大文字小文字を区別しない）
    password VARCHAR(255) NOT NULL, -- パスワード
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(), -- 作成日時
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW() -- 更新日時
);