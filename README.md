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

- データベース: FakeRepository（インメモリテストダブル）
- テーブル作成: なし
- 利点: 高速、外部依存なし、CIで安定動作、ピュアなユニットテスト

## テスト方針

### 基本方針
- アウトサイドインなテスト方針を採用
- サービスの関数ごとにテストを実装
- 最低限の振る舞い担保として正常系1本のみの実装でスタート

### テスト構成
- FakeRepository等は`test/.../service/fixture/`に作成してテストダブルとして利用
- Given-When-Thenパターンを厳密に適用
- データベースに依存しないインメモリテストを基本とする
- ExposedとのORM結合まで担保する必要が出てきた場合はH2インメモリDBを使用した統合テストに切り替え可能

### テストケースの命名規則
```kotlin
@Test
fun `[関数名] - [テストケースの説明]`() = runTest {
    // Given: 前提条件
    
    // When: 実行
    
    // Then: 検証
}
```

### 例: AuthServiceTest
```kotlin
class AuthServiceTest {
    private val userRepository = FakeUserRepository()
    private val authService = AuthService(userRepository)

    @BeforeTest
    fun setUp() {
        userRepository.clear()
    }

    @Test
    fun `registerUser - サービス利用するための自身のユーザを登録できること`() = runTest {
        // Given-When-Thenパターンで正常系テスト実装
        // 最低限の振る舞い担保として1本のみ
    }
}

## 環境設定・マイグレーション戦略

### 環境変数

| 変数名                           | デフォルト値                                                | 説明         |
|-------------------------------|-------------------------------------------------------|------------|
| `APP_DEVELOPMENT`             | `true`                                                | 開発モード有効/無効制御 |
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
| `./gradlew ktlintCheck` | コードスタイルチェック        |
| `./gradlew ktlintFormat`| コードスタイル自動修正        |
| `./gradlew detekt`      | 静的解析（警告のみ）         |

### コード品質チェック

プロジェクトでは ktlint と detekt による自動コード品質チェックを導入しています：

#### 自動実行タイミング
- Build時: `./gradlew build` 実行時にktlintCheck → エラーでビルド失敗

#### 設定ポリシー
- ktlint: コードスタイル違反時はビルド失敗（品質強制）
- detekt: 静的解析結果は警告のみ（開発効率重視）

#### エラー修正方法
```bash
# コードスタイルエラーを自動修正
./gradlew ktlintFormat

# 修正内容をステージング
git add .
```

アプリケーション起動成功時のログ例:

```
2024-12-04 14:32:45.584 [main] INFO  Application - Application started in 0.303 seconds.
2024-12-04 14:32:45.682 [main] INFO  Application - Responding at http://0.0.0.0:8080
```

## プロジェクト構成

レイヤードアーキテクチャパターンに従い、関心事の分離を明確にした構成:

```
src/
├── main/kotlin/com/example/
│   ├── Application.kt                    # アプリケーションエントリーポイント
│   ├── config/                          # 設定レイヤー
│   │   ├── database/                    # データベース接続設定
│   │   ├── di/                          # Koin依存性注入設定
│   │   └── http/                        # CORS・HTTP設定
│   ├── domain/                          # ドメインレイヤー（ビジネスロジック）
│   │   ├── auth/                        # 認証ドメイン
│   │   │   ├── model/                   # User等のエンティティ
│   │   │   │   └── valueobject/         # Email, Password等
│   │   │   ├── repository/              # リポジトリインターフェース
│   │   │   └── service/                 # AuthService等
│   │   └── payable/                     # 支払い管理ドメイン
│   ├── infrastructure/                  # インフラストラクチャレイヤー
│   │   └── database/
│   │       ├── repository/              # UserRepositoryImpl等
│   │       └── schema/                  # UsersTable等
│   │           └── customtypes/         # CitextColumnType等
│   └── presentation/                    # プレゼンテーションレイヤー（API）
│       ├── controller/                  # AuthController等
│       ├── dto/                        # データ転送オブジェクト
│       │   ├── request/                # RegisterRequest等
│       │   └── response/               # RegisterResponse, ErrorResponse等
│       ├── routing/                    # Routes.kt
│       └── validation/                 # RegisterValidation等
└── test/kotlin/com/example/
    └── domain/
        └── auth/
            └── service/
                ├── fixture/             # FakeUserRepository等
                └── AuthServiceTest.kt   # サービス単位のテスト
```

### レイヤー責任

- ドメインレイヤー: ビジネスロジック・エンティティ・ルールを含む。外部依存から独立
- インフラストラクチャレイヤー: データベースアクセス・外部サービス等の技術的関心事を実装
- プレゼンテーションレイヤー: HTTPリクエスト/レスポンス処理・APIコントラクトを担当
- 設定レイヤー: アプリケーション設定・フレームワーク設定を管理

### Value Objectsパターンの活用

プロジェクトではドメイン駆動設計のValue Objectsパターンを採用し、型安全性とバリデーションの集約を実現:

#### Email Value Object
```kotlin
data class Email(val value: String) {
    init {
        require(value.isNotBlank()) { "Email address must not be blank" }
        require(isValidEmailFormat(value)) { "Invalid email format" }
    }
}
```

#### Password Value Object
```kotlin
data class Password private constructor(val value: String, private val isHashed: Boolean = false) {
    // 生パスワード用コンストラクタ（バリデーション付き）
    constructor(rawPassword: String) : this(rawPassword, false) {
        require(rawPassword.length >= 8) { "Password must be at least 8 characters" }
        require(isValidPasswordComplexity(rawPassword)) { "Password complexity requirements not met" }
    }
    
    // ハッシュ化メソッド
    fun hash(): Password = Password(BCrypt.hash(value), true)
    
    companion object {
        fun fromHashed(hashedPassword: String): Password = Password(hashedPassword, true)
    }
}
```

#### メリット
- 型安全性: プリミティブ型の代わりにドメイン固有の型を使用
- バリデーションの集約: ビジネスルールをValue Object内に集約
- 不正データの排除: 無効な値のオブジェクト作成を防止
- ドメイン表現力向上: コードがビジネス要求を明確に表現

## 🔐 セキュリティ

### パスワード管理
- 複雑性要件: 8-128文字、大文字・小文字・数字・記号の3カテゴリ以上
- ハッシュ化: BCryptラウンド12を使用
- 生パスワード保護: Password Value Objectで平文パスワードを隠蔽

### 認証システム仕様

#### 提供機能
- ユーザー登録: 企業ユーザーの新規登録（`POST /v1/auth/register`）
- ユーザーログイン: JWT認証による認証システム（`POST /v1/auth/login`）
- バリデーション: Email/Password Value Objectsによる型安全な検証
- セキュリティ: BCrypt(ラウンド12)によるパスワードハッシュ化

### JWT認証仕様

トークン形式:
- アルゴリズム: HMAC256
- 有効期限: 24時間（86400秒）
- ペイロード: ユーザーID、メール、名前、会社名
- 使用方法: `Authorization: Bearer <token>` ヘッダーで送信

注意事項:
- リフレッシュトークンは現在未実装（将来的な改善課題）
- トークンは安全に保管し、第三者に漏洩しないよう注意
- トークン有効期限切れの場合は再ログインが必要

### Swagger UI（API仕様書）

#### アクセス方法
- URL: `http://localhost:8080/swagger-ui`
- 利用可能環境: `APP_DEVELOPMENT=true`の場合のみ

#### 機能
- インタラクティブなAPI探索
- リクエスト・レスポンスのサンプル表示
- 実際のAPIテスト実行
- JWT Bearer認証のサポート

#### 仕様書管理
- OpenAPI仕様書: `src/main/resources/openapi/documentation.yaml`
- 仕様書は手動で作成・更新が必要(生成AIで作れば工数は気にならない)
- 新しいAPI追加時は対応するOpenAPI定義も追加すること
