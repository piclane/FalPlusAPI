FalPlusAPI
----
[Foltia ANIME LOCKER](https://foltia.com/ANILOC/) 用の GraphQL API です。

## 提供機能

この API では以下のエンティティーを提供します。

- 放送 (subtitle)
- 番組 (program)
- チャンネル (station)
- キーワードグループ (keyword group)

### 問い合わせ (query)

- 単一の放送の取得
- 条件を指定しての複数の放送の取得
- 条件を指定しての複数の番組の取得
- 条件を指定しての複数のチャンネルの取得
- 条件を指定しての複数の EPG 番組の取得
- 条件を指定しての複数のキーワードグループの取得
- ライブ
  - ライブバッファ時間の取得 
- ディスク情報の取得

### 書き換え (mutation)

- 放送の情報の変更
- 放送動画のアップロード
- 放送動画の削除
- チャンネル情報の変更
- ライブ
  - 開始
  - 終了
  - すべて終了
- トランスコードの開始指示

### 購読 (subscription)

未使用 (ANIME LOCKER が httpd v2.2 なので、このまま使わないかも)

### 認証・認可

未サポート (そのうちサポートするかも)

## API へのアクセス方法

GraphQL API ですので、GraphQL クライアントを各自ご用意下さい。

### 例

以下の条件の放送を取得します
- 録画済みの放送
- 録画中の放送を含む
- 番組IDが 1234 である

```graphql
query FindSubtitles(
    $query: SubtitleQueryInput,
    $offset: Int!,
    $limit: Int!
) {
    subtitles(query: $query, offset: $offset, limit: $limit) {
        total
        data {
            pId
            tId
            subtitle
            countNo
            startDateTime
            duration
            fileStatus
            thumbnailUri
            tsVideoUri
            sdVideoUri
            hdVideoUri
            station {
                stationName
                digitalStationBand
            }
            program {
                tId
                title
            }
            keywordGroups {
                keywordGroupId
                keyword
            }
        }
    }
}
```

variables
```json
{
  "query": {
    "hasRecording": true,
    "nowRecording": true,
    "tId": 1234
  },
  "offset": 0,
  "limit": 100
}
```

エンドポイント
```
http://<hostname>/api/graphql
```

## インストール方法

### インストール

Foltia ANIME LOCKER に foltia ユーザーでログインした後、以下のコマンドを実行して下さい。
```bash
$ curl -L -o /tmp/fapi.tar.gz "$(curl -sS -H 'Accept: application/vnd.github.v3+json' 'https://api.github.com/repos/piclane/FalPlusAPI/releases/latest' | grep '"browser_download_url"' | sed -E -e 's/^.*"(https:[^"]+)"$/\1/')"
$ cd /tmp
$ tar zxf fapi.tar.gz
$ rm fapi.tar.gz
$ cd foltia_api-<version>
$ su -c "$(pwd)/install.sh" -
```

### アンインストール

Foltia ANIME LOCKER に foltia ユーザーでログインした後、以下のコマンドを実行して下さい。
```bash
$ cd /tmp/foltia_api-<version>
$ su -c "$(pwd)/uninstall.sh" -
```
