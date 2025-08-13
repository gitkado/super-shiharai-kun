-- 請求書テーブル
CREATE TABLE invoices (
    id BIGSERIAL PRIMARY KEY, -- 請求書ID
    user_id BIGINT REFERENCES users(id) ON DELETE CASCADE, -- 企業ID
    issue_date DATE NOT NULL, -- 発行日
    payment_amount DECIMAL(15, 2) NOT NULL, -- 支払金額
    fee DECIMAL(15, 2) NOT NULL, -- 手数料
    fee_rate DECIMAL(5, 4) NOT NULL, -- 手数料率（例: 0.04 = 4%）
    tax_amount DECIMAL(15, 2) NOT NULL, -- 消費税
    tax_rate DECIMAL(5, 4) NOT NULL, -- 消費税率（例: 0.10 = 10%）
    total_amount DECIMAL(15, 2) NOT NULL, -- 請求金額
    payment_due_date DATE NOT NULL, -- 支払期日
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(), -- 作成日時
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW() -- 更新日時
);