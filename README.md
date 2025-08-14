# スーパー支払い君.com

Kotlin/Ktorで構築した企業向け支払い管理システムのREST APIサービスです。

## クイックスタート

### 前提条件
- Java 11以上
- Docker & Docker Compose

### 開発環境起動（推奨）
```bash
# 1. PostgreSQL起動
docker compose -f docker/compose.yaml up -d

# 2. アプリケーション起動
./gradlew run
```

### 統合テスト環境（本番に近い環境）
```bash
# アプリ・DBともにコンテナで起動
docker compose -f docker/compose.integration.yaml up --build -d
```

### 単体テスト実行
```bash
./gradlew test  # 外部依存なし、高速実行
```

## プロジェクト概要

企業向け支払い管理システムの機能：
- ユーザー管理: 法人ユーザーの登録・JWT認証
- 請求書管理: 請求書登録・一覧表示・手数料自動計算
- 手数料計算: 支払金額の4% + 消費税10%

## 技術スタック

| 技術         | バージョン | 用途          |
|------------|-------|-------------|
| Kotlin/JVM | 11+   | メイン言語       |
| Ktor       | 3.2.3 | Webフレームワーク  |
| PostgreSQL | 16.3  | 本番データベース    |
| H2         | -     | テストDB       |
| Exposed    | -     | ORM・SQL DSL |
| Koin       | 4.1.0 | 依存性注入       |
| Flyway     | -     | DBマイグレーション  |

## アーキテクチャ

レイヤード + Clean Architectureによる責務分離：

```
src/main/kotlin/com/example/
├── Application.kt                    # エントリーポイント
├── application/port/                 # 横断的関心事の抽象化
│   └── TransactionRunner.kt
├── domain/                          # ビジネスロジック（フレームワーク独立）
│   ├── auth/                        # 認証ドメイン
│   │   ├── model/                   # User, Email, Password
│   │   ├── service/AuthService.kt   # 認証ビジネスルール
│   │   └── repository/              # データアクセス抽象化
│   └── payable/                     # 支払い管理ドメイン
│       ├── model/                   # Invoice, Money, Rate
│       ├── service/InvoiceService.kt # 手数料計算ルール
│       └── repository/
├── infrastructure/                  # 技術実装
│   ├── database/                    # Exposed実装
│   └── auth/                        # JWT実装
├── presentation/                    # REST API
│   ├── controller/                  # AuthController, InvoiceController
│   └── dto/                         # Request/Response
└── config/                          # フレームワーク設定
```

## テスト戦略

3つの独立したテスト環境：

### 1. 単体テスト（Service層）
```bash
./gradlew test
```
- 対象: ビジネスロジック・計算ルール
- 技術: FakeRepository・MockTransactionRunner
- 特徴: 外部依存なし、高速実行

### 2. 統合テスト（Repository層）
```bash
./gradlew test -Dtest.profile=integration
```
- 対象: SQLクエリ・WHERE/ORDER BY句
- 技術: H2（PostgreSQLモード）
- 特徴: SQL動作検証

### 3. 結合テスト（Full Stack）
```bash
docker compose -f docker/compose.integration.yaml up --build -d
```
- 対象: API・認証・DB連携
- 技術: PostgreSQL + コンテナ
- 特徴: 本番環境相当

## 主要コマンド

### 必須コマンド
| コマンド              | 説明            |
|-------------------|---------------|
| `./gradlew run`   | 開発サーバー起動      |
| `./gradlew test`  | テスト実行         |
| `./gradlew build` | ビルド（品質チェック含む） |

### コード品質
| コマンド                     | 説明          |
|--------------------------|-------------|
| `./gradlew ktlintCheck`  | コードスタイルチェック |
| `./gradlew ktlintFormat` | スタイル自動修正    |
| `./gradlew detekt`       | 静的解析（警告のみ）  |

## 環境設定

### 重要な環境変数
| 変数名                           | 説明     | デフォルト            |
|-------------------------------|--------|------------------|
| `DATABASE_MIGRATION_STRATEGY` | DB作成方法 | `exposed`        |
| `APP_DEVELOPMENT`             | 開発モード  | `true`           |
| `POSTGRES_URL`                | DB接続先  | `localhost:5432` |

### マイグレーション戦略
| 戦略        | 環境  | 説明         |
|-----------|-----|------------|
| `exposed` | 開発  | ORM自動作成    |
| `flyway`  | 本番  | 正式マイグレーション |
| `none`    | テスト | 何もしない      |

## API仕様

### Swagger UI
開発時のAPI探索：http://localhost:8080/swagger-ui

## 開発のポイント

### アーキテクチャ設計
- レイヤード設計: Presentation → Application → Domain ← Infrastructure
- 依存性逆転: 内向きの依存のみ
- ポート・アダプター: インターフェースと実装の分離

### テスト設計  
- 単体テスト: ビジネスロジック（Mock使用）
- 統合テスト: SQL動作（H2使用）
- 結合テスト: 全体動作（PostgreSQL使用）

### セキュリティ
- JWT認証: 24時間有効トークン
- パスワード: BCrypt（ラウンド12）
- バリデーション: Value Objectでドメインレベル検証
