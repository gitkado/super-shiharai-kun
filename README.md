# スーパー支払い君.com

支払い管理システムのKotlin/Ktor REST APIサービスです。

## 概要

企業向けの支払い管理システムで、請求書の登録・管理と手数料計算を行います。

- 技術スタック: Kotlin/Ktor, PostgreSQL, Docker
- アーキテクチャ: レイヤードアーキテクチャ + Clean Architecture
- 手数料: 4% + 消費税10%

## ビジネス機能

- ユーザー管理: 法人ユーザーの登録・認証
- 請求書管理: 請求書の登録・一覧表示
  - 手数料計算: 支払金額の4% + 消費税10%を自動計算
- 認証・認可: JWT認証によるセキュアなAPI

## 技術機能

| 技術要素                      | 説明                      |
|---------------------------|-------------------------|
| Ktor 3.2.3            | Kotlin製の非同期Webフレームワーク   |
| PostgreSQL 16.3       | 本番・開発用リレーショナルデータベース     |
| H2 Database           | テスト用高速インメモリDB           |
| Exposed ORM           | Kotlin製のタイプセーフなSQL DSL  |
| Flyway                | データベースマイグレーションツール       |
| Koin 4.1.0            | 軽量な依存性注入フレームワーク         |
| HikariCP              | 高性能なJDBCコネクションプール       |
| JWT認証                 | ステートレスな認証トークンシステム       |
| Docker                | アプリケーションコンテナ化           |
| kotlinx.serialization | Kotlin公式のJSONシリアライゼーション |

## 事前準備

開発環境に必要なツール:

```bash
# Java 11以上がインストールされていることを確認
java -version

# JAVA_HOME環境変数の設定例
export JAVA_HOME=/path/to/your/java

# SDKMAN利用者の場合
export JAVA_HOME=$HOME/.sdkman/candidates/java/current
```

## 開発環境・実行ルール

プロジェクトでは3つの異なる環境セットアップを使い分けています。

### 1. ローカル開発環境

用途: 日常的な開発作業  
特徴: アプリはローカル実行、DBのみコンテナ

```bash
# PostgreSQL起動（コンテナ）
docker compose -f docker/compose.yaml up -d

# アプリケーション起動（ローカル）
./gradlew run
```

- データベース: PostgreSQL (localhost:5432)
- テーブル作成: Exposed ORM (`migration_strategy: exposed`)
- 接続先: http://localhost:8080
- 利点: 高速な開発サイクル、デバッグしやすい

### 2. 結合確認環境

用途: 本番環境に近い環境での結合テスト  
特徴: アプリ・DBともにコンテナで動作

```bash
# アプリ + DB をコンテナで起動
docker compose -f docker/compose.integration.yaml up --build -d
```

- データベース: PostgreSQL (コンテナ内)
- テーブル作成: Flyway migrations (`migration_strategy: flyway`)
- 接続先: http://localhost:8080
- 利点: 本番に近い環境、CI/CDでも使用可能

### 3. テスト環境

用途: 単体テスト・自動テスト実行  
特徴: 外部依存なし、高速実行

```bash
# テスト実行（PostgreSQL不要）
./gradlew test
```

- データベース: H2 インメモリDB
- テーブル作成: なし (`migration_strategy: none`)
- 利点: 高速、外部依存なし、CIで安定動作

## 環境設定・マイグレーション戦略

### 環境変数

| 変数名                           | デフォルト値                                                | 説明         |
|-------------------------------|-------------------------------------------------------|------------|
| `DATABASE_MIGRATION_STRATEGY` | `exposed`                                             | マイグレーション方法 |
| `POSTGRES_URL`                | `jdbc:postgresql://localhost:5432/super_shiharai_kun` | DB接続URL    |
| `POSTGRES_USER`               | `myuser`                                              | DBユーザー名    |
| `POSTGRES_PASSWORD`           | `mypassword`                                          | DBパスワード    |
| `JAVA_OPTS`                   | `-XX:+UseContainerSupport`                            | JVMオプション   |

### マイグレーション戦略の使い分け

| 戦略        | 環境      | 説明                          |
|-----------|---------|-----------------------------|
| `exposed` | ローカル開発  | Exposed ORMでテーブル自動作成、開発効率重視 |
| `flyway`  | 結合確認・本番 | Flywayでマイグレーション実行、データ整合性重視  |
| `none`    | テスト     | 何もしない、テスト速度重視               |

## 開発フロー

```bash
# 1. 日常開発
docker compose up -d          # DB起動
./gradlew run                # 開発開始

# 2. テスト実行
./gradlew test               # 単体テスト（H2使用）

# 3. 結合確認
docker compose -f docker/compose.integration.yaml up --build -d
curl http://localhost:8080/health  # 動作確認
```

## ビルド・実行コマンド

プロジェクトのビルドと実行に使用するGradleタスク:

| タスク                     | 説明                 |
|-------------------------|--------------------|
| `./gradlew test`        | テスト実行（H2インメモリDB使用） |
| `./gradlew run`         | ローカル開発サーバー起動       |
| `./gradlew build`       | 全体ビルド              |
| `./gradlew buildFatJar` | 実行可能JAR作成（全依存関係含む） |
| `./gradlew buildImage`  | Dockerイメージ作成       |

アプリケーション起動成功時のログ例:

```
2024-12-04 14:32:45.584 [main] INFO  Application - Application started in 0.303 seconds.
2024-12-04 14:32:45.682 [main] INFO  Application - Responding at http://0.0.0.0:8080
```

## プロジェクト構成

レイヤードアーキテクチャパターンに従い、関心事の分離を明確にした構成:

```
src/main/kotlin/com/example/
├── Application.kt                    # アプリケーションエントリーポイント
├── config/                          # 設定レイヤー
│   ├── database/                    # データベース接続設定
│   ├── di/                          # Koin依存性注入設定
│   ├── security/                    # JWT認証設定
│   ├── http/                        # CORS・HTTP設定
│   ├── monitoring/                  # ログ・監視設定
│   └── serialization/               # JSONシリアライゼーション設定
├── domain/                          # ドメインレイヤー（ビジネスロジック）
│   ├── model/                       # ドメインエンティティ
│   ├── repository/                  # リポジトリインターフェース
│   ├── service/                     # ビジネスロジックサービス
│   ├── constants/                   # ビジネス定数・ルール
│   └── exception/                   # ビジネス例外
├── infrastructure/                  # インフラストラクチャレイヤー
│   ├── database/                    # データベース実装
│   │   ├── schema/                  # テーブル定義・ORM
│   │   └── repository/              # リポジトリ実装
│   └── security/                    # セキュリティ実装
├── presentation/                    # プレゼンテーションレイヤー（API）
│   ├── dto/                        # データ転送オブジェクト
│   │   ├── request/                # リクエストDTO
│   │   └── response/               # レスポンスDTO
│   ├── controller/                 # RESTコントローラー
│   └── routing/                    # ルート定義
└── util/                           # 技術的ユーティリティ関数
```

### レイヤー責任

- ドメインレイヤー: ビジネスロジック・エンティティ・ルールを含む。外部依存から独立
- インフラストラクチャレイヤー: データベースアクセス・外部サービス等の技術的関心事を実装
- プレゼンテーションレイヤー: HTTPリクエスト/レスポンス処理・APIコントラクトを担当
- 設定レイヤー: アプリケーション設定・フレームワーク設定を管理
- ユーティリティレイヤー: 技術的なヘルパー関数・拡張関数を提供
