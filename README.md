# KickPropagator

Purpur などのバックエンドサーバーからプレイヤーが明示的にキックされた際に、Velocity ネットワーク全体からも強制切断するプラグインです。

## 機能

- 指定したバックエンドサーバーからのキックを検知し、ネットワーク全体から切断
- 接続タイムアウトやクラッシュによる切断はフォールバック動作に委ねる（キックとは区別）
- 対象サーバーを設定ファイルで柔軟に指定可能
- `/kickpropagator reload` コマンドで再起動不要のコンフィグリロード

## 動作環境

| 項目 | バージョン |
|------|-----------|
| Velocity | 3.x |
| Java | 17 以上 |

## インストール

1. [Releases](../../releases) から最新の `velocity-kick-propagator-x.x.x.jar` をダウンロード
2. Velocity の `plugins/` フォルダに配置
3. Velocity を起動（または再起動）
4. 生成された `plugins/kick-propagator/config.yml` を編集

## 設定

初回起動時に `plugins/kick-propagator/config.yml` が自動生成されます。

```yaml
# キックされた場合にネットワーク全体から切断するサーバー名のリスト
# velocity.toml の servers セクションで設定したサーバー名と一致させてください
target-servers:
  - purpur
  - survival
  - creative

# true: target-servers に含まれるサーバーからのキックのみネットワーク切断
# false: すべてのサーバーからのキックをネットワーク切断
use-target-servers: true
```

設定変更後は `/kickpropagator reload` で即時反映できます。

## コマンド・権限

| コマンド | 権限ノード | 説明 |
|---------|-----------|------|
| `/kickpropagator reload` | `kickpropagator.reload` | 設定ファイルをリロード |

## キックと接続エラーの区別

以下のメッセージを含む切断はサーバークラッシュ・タイムアウトとみなし、Velocity のフォールバック動作に委ねます。

- `Connection reset / closed / timed out`
- `Read timed out`
- `End of stream`
- `Broken pipe`
- `Server closed`
- `IOException`

これら以外のメッセージを持つキックは明示的なキック（`/kick` コマンドやプラグインによるキック）と判定し、ネットワーク切断を実行します。

## ビルド方法

Java 17 以上がインストールされた環境で `build.bat` をダブルクリックするだけでビルドできます。Maven は未インストールでも自動ダウンロードされます。

```
build.bat
```

生成物: `target/velocity-kick-propagator-1.0.0.jar`

## ライセンス

MIT License
