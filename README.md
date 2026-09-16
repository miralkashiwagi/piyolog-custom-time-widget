# ぴよログAPIウィジェット

指定したURLのJSONから、任意のtype（例: 授乳）の値の最新レコードを
「◯時間◯分前 (HH:mm)」の形式で表示するAndroidホーム画面ウィジェットです。

- ウィジェットをタップすると即座に手動更新
- URLとtype名はウィジェットごとに設定画面から自由入力・後から変更可能
  （URLが90日ごとに変わる場合はURLを再設定してください）
- ホーム画面でウィジェットを長押しすると、横・縦方向にサイズ変更可能
  （変更できる大きさの刻みはホームアプリによって異なります）

# インストール
1. Android端末に`app-piyolog-time-widget.apk`をダウンロードし
2. 端末の設定で「提供元不明のアプリ」のインストールを許可
3. APKをタップしてインストール
4. ホーム画面の「ウィジェットを追加」メニューから「ぴよログAPI」を配置
5. ぴよログAPIのURLと、JSON内のtypeをウィジェットに設定

## 開発手順（GitHub Actions）
### 変更時のバージョン番号アップ
https://github.com/miralkashiwagi/piyolog-custom-time-widget/blob/main/app/build.gradle.kts#L14-L15
のバージョンを変更するとapkインストール時、古いバージョンがインストール済みでも、apkからインストールでアップデートできるようになります。

### ビルド
1. pushすると、リポジトリの **Actions** タブでワークフローが自動的に開始されます（数分かかります）。
2. ワークフローが緑色のチェックマーク（成功）になったら、そのワークフローの実行結果ページ下部の**Artifacts** セクションに `piyolog-time-widget-release-apk` というzipがあるのでダウンロードします。中に `app-piyolog-time-widget.apk` が入っています。


### フォークして使う場合（署名鍵の設定）
このリポジトリのAPKは特定の署名鍵で署名する設定になっています。フォークしたリポジトリでは、あなた自身の署名鍵をGitHub Secretsに登録しないと、ビルド自体は成功してもインストールできない（またはアップデートできない）ことがあります。以下の手順を1回だけ行ってください。

1. **署名鍵を作る**（手元のPC。Java同梱の`keytool`コマンドを使用）
   ```
   keytool -genkeypair -v -keystore release.keystore -alias release -keyalg RSA -keysize 2048 -validity 10950 -storepass "任意のパスワード" -keypass "同じパスワード"
   ```
   ※`storepass`と`keypass`は必ず同じ値にしてください（PKCS12形式のキーストアでは異なる値だと署名時にエラーになります）。
2. **base64に変換**
   ```
   base64 -w0 release.keystore > release.keystore.base64.txt
   ```
   （macOSの場合は `base64 -i release.keystore -o release.keystore.base64.txt`）
3. **フォーク先リポジトリの Settings → Secrets and variables → Actions** で以下3つを登録
   | Secret名 | 値 |
   |---|---|
   | `RELEASE_KEYSTORE_BASE64` | `release.keystore.base64.txt` の中身 |
   | `RELEASE_KEYSTORE_PASSWORD` | 手順1で決めたパスワード |
   | `RELEASE_KEY_ALIAS` | 手順1で指定したalias（例: `release`） |
4. 上記を設定した状態でmainブランチにpushすれば、以降のビルドは常にあなた自身の鍵で署名されます。
5. `release.keystore` と パスワードは今後のアップデートにも必要です。紛失すると同じ署名でのアップデート配布ができなくなるので、安全な場所に保管し、**リポジトリにはコミットしないでください**

## 補足
- typeの照合は完全一致（大文字小文字も区別）です。JSON内の `"type":"授乳"` のような表記に合わせて入力してください。
- ネットワークエラーやHTTPエラー時は「取得失敗」と表示されます。
