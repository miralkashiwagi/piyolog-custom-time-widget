# ぴよログAPI

指定したURLのJSONから、任意のtype（Custom1、Pee、Formulaなど）の最新レコードを
「◯時間◯分前 (HH:mm)」の形式で表示するAndroidホーム画面ウィジェットです。

- 2 x 1サイズのウィジェットでtypeを1つ表示
- 30分ごとに自動更新（Android標準の最小間隔）
- ウィジェット本体をタップすると即座に手動更新
- 右上の歯車アイコンから、URLとtype名をいつでも再設定可能

## 導入手順（GitHub Actions / Android Studio不要）

Android StudioもAndroid SDKもローカルにインストールせず、GitHubのクラウド上でAPKをビルドする方法です。
必要なのは **GitHubアカウント** と **git** （またはブラウザからのファイルアップロード）だけです。

このプロジェクトにはGradle公式が推奨する **Gradle Wrapper**（`gradlew` / `gradlew.bat` / `gradle/wrapper/`）が
同梱されています。CIでもローカルでも、Gradle自体を別途インストールする必要はなく、常に
`./gradlew`（Windowsは`gradlew.bat`）経由でビルドが実行され、プロジェクトが指定したバージョンの
Gradleが自動的にダウンロード・使用されます。

### 1. GitHubに新しいリポジトリを作る
GitHub上で新規リポジトリ（Public/Privateどちらでも可）を作成します。例: `custom1-widget`

### 2. このzipの中身をリポジトリにpushする
ローカルにgitがある場合:
```bash
cd Custom1Widget   # このzipを展開したフォルダ
git init
git add .
git commit -m "Initial commit"
git branch -M main
git remote add origin https://github.com/あなたのユーザー名/custom1-widget.git
git push -u origin main
```
gitを使わない場合は、GitHubのWeb UIの「Add file > Upload files」からzipを展開したフォルダの中身（`.github`フォルダも含めて全部）をドラッグ＆ドロップしてもOKです。ただし`.github/workflows/build.yml`が含まれていることを必ず確認してください（隠しフォルダなので見落としやすいです）。

### 3. ビルドが自動で走る
pushすると、リポジトリの **Actions** タブで「Build APK」というワークフローが自動的に開始されます（数分かかります）。
手動で走らせたい場合は、Actionsタブ → Build APK → 「Run workflow」ボタンでも実行できます。

### 4. APKをダウンロードする
ワークフローが緑色のチェックマーク（成功）になったら、そのワークフローの実行結果ページ下部の
**Artifacts** セクションに `custom1-widget-debug-apk` というzipがあるのでダウンロードします。
中に `app-debug.apk` が入っています。

### 5. スマホにインストールする
1. ダウンロードした`app-debug.apk`をAndroid端末に転送（Google Drive経由、USB、メールなど）
2. 端末の設定で「提供元不明のアプリ」のインストールを許可
3. APKをタップしてインストール
4. ホーム画面の「ウィジェットを追加」メニューから「ぴよログAPI」を配置

### トラブルシューティング

- **`sdkmanager tools` で失敗する / `Failed to find package 'tools'`**: これは古いバージョンのワークフローで`android-actions/setup-android`アクションを使っていた場合に発生する既知の問題です（Googleが廃止したパッケージを要求してしまう）。このzipの`build.yml`では該当アクションを使わず、GitHubランナーにプリインストール済みのAndroid SDKをそのまま使う構成に修正済みです。
- **Permission denied: ./gradlew**: `chmod +x ./gradlew`が必要です（`build.yml`には既に含めています。ローカルでcloneした直後に手動実行する場合も同様にしてください）。

### コマンドラインのみでビルドしたい場合（Android Studio不要・PC上）

Wrapperが同梱されているため、PC側で用意するのはJDK 17とAndroid SDK（`sdkmanager`が使えれば十分）だけです。Gradle自体のインストールは不要です。

```bash
export ANDROID_HOME=/path/to/android-sdk
yes | $ANDROID_HOME/cmdline-tools/latest/bin/sdkmanager --licenses
$ANDROID_HOME/cmdline-tools/latest/bin/sdkmanager "platform-tools" "platforms;android-34" "build-tools;34.0.0"

./gradlew assembleDebug
# 生成物: app/build/outputs/apk/debug/app-debug.apk

adb install app/build/outputs/apk/debug/app-debug.apk
```

### コードを修正したいとき
`app/src/main/java/...`内のKotlinファイルや`res`内のレイアウトを編集して、再度`git push`するだけで
Actionsが再ビルドし、新しいAPKがArtifactsに生成されます。手元でのビルド環境構築は一切不要です。

---

## 導入手順（Android Studio）

1. Android Studioで **File > New > New Project** を選択
   - テンプレート: **No Activity**（または Empty Views Activity でも可。生成された `MainActivity` 関連ファイルは使わないので削除してOK）
   - Language: **Kotlin**
   - Minimum SDK: **API 26 (Android 8.0)以上**
   - Package name（applicationId）は自由でよいですが、ここでは `com.example.custom1widget` を前提にファイルを作っています。別のパッケージ名にする場合は、このzip内の全`.kt`ファイル先頭の `package com.example.custom1widget` と、AndroidManifest.xml内の `android:name="."〜"` の参照、`android.appwidget.provider` の `@xml/...` 参照はそのままで大丈夫です（パッケージ名はディレクトリ構成とファイル冒頭のpackage宣言だけ揃っていればOK）。

2. プロジェクトが作成されたら、このzipの中身を以下のように配置（上書き）してください。

```
あなたのプロジェクト/
└── app/
    ├── build.gradle.kts          ← このzipの内容で置き換え（既存のdependenciesが必要ならマージ）
    └── src/main/
        ├── AndroidManifest.xml   ← このzipの内容で置き換え
        ├── java/com/example/custom1widget/
        │   ├── TrackerCore.kt
        │   ├── WidgetPrefs.kt
        │   ├── WidgetProviderSmall.kt
        │   ├── WidgetConfigureSmallActivity.kt
        └── res/
            ├── drawable/
            │   ├── widget_background.xml
            │   └── ic_settings.xml
            ├── layout/
            │   ├── widget_small.xml
            │   ├── activity_configure_small.xml
            ├── values/strings.xml（既存のstrings.xmlとマージしてください。app_nameのみ追加）
            └── xml/
                └── tracker_widget_small_info.xml
```

   - `MainActivity.kt` や `activity_main.xml` など、テンプレートが生成した不要なファイルは削除して構いません。
   - `mipmap` のアプリアイコン（`ic_launcher`）はテンプレートのものがそのまま使えます。

3. Android Studio右上の **Sync Now**（Gradle同期）を実行し、エラーが出ないことを確認。

4. 実機またはエミュレータでアプリを一度インストール（Run）してください。
   ※このアプリには通常の画面（ランチャーアイコン）はありません。インストール後、ホーム画面の「ウィジェットを追加」メニューに以下のウィジェットが表示されます。
   - **ぴよログAPI**

5. ホーム画面にウィジェットをドラッグして配置すると、設定画面が開きます。
   - URLと表示したいtype名（例: `Custom1`）を入力して保存

6. 配置後はウィジェット本体をタップするといつでも手動更新できます。右上の歯車アイコンをタップするとURLとtype名を再設定できます。またOS標準で約30分ごとに自動更新されます。

## URLが90日ごとに変わったとき

該当のウィジェット右上にある歯車アイコンをタップすると設定画面が開き、
URLとtype名を入力し直して保存できます。

## 補足

- typeの照合は完全一致（大文字小文字も区別）です。JSON内の `"type":"Custom1"` のような表記に合わせて入力してください。
- ネットワークエラーやHTTPエラー時は「取得失敗」と表示されます。
- 認証（APIキー等）は現状未対応です。必要になった場合は `TrackerCore.fetchJson()` 内の
  `connection.setRequestProperty(...)` にヘッダーを追加してください。
