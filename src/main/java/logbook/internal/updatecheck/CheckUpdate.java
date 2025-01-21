package logbook.internal.updatecheck;

import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipFile;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import logbook.internal.Launcher;
import logbook.internal.ThreadManager;
import logbook.internal.gui.InternalFXMLLoader;
import logbook.internal.gui.Tools;
import logbook.internal.gui.WindowHolder;
import logbook.internal.logger.LoggerHolder;

/**
 * アップデートチェック
 *
 */
public class CheckUpdate {

    /** GitHub リポジトリのパス */
    public static final String REPOSITORY_PATH = "sakura0689/logbook-kai";

    /** 更新確認先 Github tags API */
    private static final String TAGS = "https://api.github.com/repos/" + REPOSITORY_PATH + "/tags";

    /** 更新確認先 Github releases API */
    private static final String RELEASES = "https://api.github.com/repos/" + REPOSITORY_PATH + "/releases/tags/";

    /** ダウンロードサイトを開くを選択したときに開くURL */
    private static final String OPEN_URL = "https://github.com/" + REPOSITORY_PATH + "/releases";

    /** 検索するtagの名前 */
    /* 例えばv20.1.1 の 20.1.1にマッチ */
    static final Pattern TAG_REGIX = Pattern.compile("\\d+\\.\\d+(?:\\.\\d+)?$");

    /** Prerelease を使う System Property */
    private static final String USE_PRERELEASE = "logbook.use.prerelease";

    public static void run(Stage stage) {
        run(false, stage);
    }

    public static void run(boolean isStartUp) {
        run(isStartUp, null);
    }

    private static void run(boolean isStartUp, Stage stage) {
        Version remoteVersion = remoteVersion();

        if (!Version.UNKNOWN.equals(remoteVersion) && Version.getCurrent().compareTo(remoteVersion) < 0) {
            Platform.runLater(() -> CheckUpdate.openInfo(Version.getCurrent(), remoteVersion, isStartUp, stage));
        } else if (!isStartUp) {
            Tools.Controls.alert(AlertType.INFORMATION, "更新の確認", "最新のバージョンです。", stage);
        }
    }

    /**
     * 最新のバージョンを取得します。
     * @return 最新のバージョン
     */
    private static Version remoteVersion() {
        try {
            JsonArray tags;
            try (JsonReader r = Json.createReader(new ByteArrayInputStream(readURI(URI.create(TAGS))))) {
                tags = r.readArray();
            }
            // Githubのtagsから一番新しいreleasesを取ってくる
            // tagsを処理する
            return tags.stream()
                    //　tagの名前
                    .map(val -> val.asJsonObject().getString("name"))
                    // tagの名前にバージョンを含む?実行中のバージョンより新しい?
                    .filter(tagname -> {
                        Matcher m = TAG_REGIX.matcher(tagname);
                        if (m.find()) {
                            Version remote = new Version(m.group());
                            return (!Version.UNKNOWN.equals(remote) && Version.getCurrent().compareTo(remote) < 0);
                        }
                        return false;
                    })
                    // tagがreleasesにある?
                    .filter(name -> {
                        try {
                            JsonObject releases;
                            try (JsonReader r = Json
                                    .createReader(new ByteArrayInputStream(readURI(URI.create(RELEASES + name))))) {
                                releases = r.readObject();
                            }
                            // releasesにない場合は "message": "Not Found"
                            if (releases.getString("message", null) != null)
                                return false;
                            // draftではない
                            if (releases.getBoolean("draft", false))
                                return false;
                            // prereleaseではない
                            if (!Boolean.getBoolean(USE_PRERELEASE) && releases.getBoolean("prerelease", false))
                                return false;
                            // assetsが1つ以上ある
                            if (releases.getJsonArray("assets") == null || releases.getJsonArray("assets").size() == 0)
                                return false;
                            // 最新版が見つかった!
                            return true;
                        } catch (Exception e) {
                            return false;
                        }
                    })
                    .findFirst()
                    .map(tagname -> {
                        Matcher m = TAG_REGIX.matcher(tagname);
                        m.find();
                        return new Version(m.group());
                    })
                    .orElse(Version.UNKNOWN);
        } catch (Exception e) {
            LoggerHolder.get().warn("最新バージョンの取得に失敗しました", e);
        }
        return Version.UNKNOWN;
    }

    private static byte[] readURI(URI uri) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) uri.toURL().openConnection();
        try {
            // タイムアウトを設定
            connection.setConnectTimeout((int) TimeUnit.SECONDS.toMillis(10));
            connection.setReadTimeout((int) TimeUnit.SECONDS.toMillis(5));
            // 200 OKの場合にURIを読み取る
            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                try (InputStream in = connection.getInputStream()) {
                    byte[] buffer = new byte[1024];
                    int len;
                    while ((len = in.read(buffer, 0, buffer.length)) > 0) {
                        out.write(buffer, 0, len);
                    }
                }
                return out.toByteArray();
            }
        } finally {
            connection.disconnect();
        }
        return new byte[0];
    }

    private static void openInfo(Version o, Version n, boolean isStartUp, Stage stage) {
        String message = "新しいバージョンがあります。ダウンロードサイトを開きますか？\n"
                + "現在のバージョン:" + o + "\n"
                + "新しいバージョン:" + n;
        if (isStartUp) {
            message += "\n※自動アップデートチェックは[その他]-[設定]から無効に出来ます";
        }

        ButtonType update = new ButtonType("自動更新");
        ButtonType visible = new ButtonType("ダウンロードサイトを開く");
        ButtonType no = new ButtonType("後で");

        Alert alert = new Alert(AlertType.INFORMATION);
        alert.getDialogPane().getStylesheets().add("logbook/gui/application.css");
        InternalFXMLLoader.setGlobal(alert.getDialogPane());
        alert.setTitle("新しいバージョン");
        alert.setHeaderText("新しいバージョン");
        alert.setContentText(message);
        alert.initOwner(stage);
        alert.getButtonTypes().clear();
        alert.getButtonTypes().addAll(update, visible, no);
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent()) {
            if (result.get() == update)
                try {
                    launchUpdate(n.toString());
                } catch (Exception e) {
                    LoggerHolder.get().error("Update処理内でエラー発生", e);
                }
            if (result.get() == visible)
                openBrowser();
        }
    }

    private static void openBrowser() {
        try {
            ThreadManager.getExecutorService()
                    .submit(() -> {
                        Desktop.getDesktop()
                                .browse(URI.create(OPEN_URL));
                        return null;
                    });
        } catch (Exception e) {
            LoggerHolder.get().warn("アップデートチェックで例外", e);
        }
    }

    private static void launchUpdate(String version) {

        Stage mainStage = WindowHolder.getInstance().getMainWindow();
        if (mainStage == null) {
            LoggerHolder.get().error("メインウィンドウが取得できませんでした。");
            return;
        }

        // ルートレイアウト
        StackPane pane = new StackPane();
        pane.setPrefSize(700, 600);
        pane.setPadding(new Insets(6));
        pane.getStyleClass().add("root-pane"); // スタイルクラス

        // メインコンテナ
        VBox vbox = new VBox();
        vbox.getStyleClass().add("main-container");

        // ラベル部分
        TextFlow label = new TextFlow(
                new Text("航海日誌 v" + version + "への更新を行います。\n"),
                new Text("更新の準備が出来ましたら[更新]を押して更新を行ってください。"));
        label.getStyleClass().add("info-label");
        vbox.getChildren().add(label);

        // WebViewコンテナ
        StackPane stackPane1 = new StackPane();
        stackPane1.getStyleClass().add("webview-container");
        VBox.setVgrow(stackPane1, Priority.ALWAYS);

        WebView webView = new WebView();
        WebEngine webEngine = webView.getEngine();
        webEngine.loadContent(
                "<html>" +
                        "<head>" +
                        "<script src=\"https://cdnjs.cloudflare.com/ajax/libs/marked/0.3.6/marked.min.js\"></script>" +
                        "<link href=\"https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap.min.css\" rel=\"stylesheet\">"
                        +
                        "<style>" +
                        "body { font-family: 'Meiryo UI', Meiryo, 'Segoe UI', 'Lucida Grande', Verdana, Arial, Helvetica, sans-serif; }"
                        +
                        "</style>" +
                        "</head>" +
                        "<body class=\"container\"></body>" +
                        "</html>");
        stackPane1.getChildren().add(webView);

        // ボタンとチェックボックス
        StackPane stackPane2 = new StackPane();
        stackPane2.getStyleClass().add("button-container");
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.getStyleClass().add("button-box");

        Button updateButton = new Button("更新");
        updateButton.getStyleClass().add("update-button");
        updateButton.setOnAction(event -> startUpdateProcess(version, webEngine));

        // 閉じるボタン
        Button closeButton = new Button("閉じる");
        closeButton.getStyleClass().add("close-button");
        closeButton.setOnAction(event -> {
            Stage stage = (Stage) closeButton.getScene().getWindow();
            stage.close(); // 現在のウィンドウを閉じる
        });
        
        buttonBox.getChildren().addAll(updateButton, closeButton);
        stackPane2.getChildren().add(buttonBox);

        vbox.getChildren().addAll(stackPane1, stackPane2);
        pane.getChildren().add(vbox);

        // リリースノートのリンク処理
        webEngine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == Worker.State.SUCCEEDED) {
                webEngine.executeScript(
                        "(function() {" +
                                "if (window.marked && document.body) {" +
                                "document.body.innerHTML = window.marked('<p>リリース情報をここに表示します。</p>');" +
                                "Array.from(document.getElementsByTagName('a')).forEach(function(a) {" +
                                "a.addEventListener('click', function(e) {" +
                                "e.preventDefault();" +
                                "var href = a.getAttribute('href');" +
                                "if (href) {" +
                                "java.awt.Desktop.getDesktop().browse(new java.net.URI(href));" +
                                "}" +
                                "});" +
                                "});" +
                                "}" +
                                "})();");
            }
        });

        // リリース情報を取得するタスク
        Task<String> fetchReleaseNotesTask = new Task<String>() {
            @Override
            protected String call() throws Exception {
                String apiUrl = RELEASES + "v" + version;
                HttpURLConnection connection = (HttpURLConnection) new URL(apiUrl).openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("Accept", "application/vnd.github.v3+json");
                connection.setRequestProperty("User-Agent", "JavaFX-App");

                if (connection.getResponseCode() != 200) {
                    throw new Exception("GitHub API エラー: HTTP " + connection.getResponseCode());
                }

                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line).append("\n");
                    }
                    return response.toString();
                }
            }
        };

        // タスク失敗時の処理
        fetchReleaseNotesTask.setOnFailed(event -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.initOwner(WindowHolder.getInstance().getMainWindow());
            alert.setTitle("更新情報を取得できませんでした");
            alert.setContentText(fetchReleaseNotesTask.getException().getMessage());
            alert.showAndWait();
        });

        // タスク成功時の処理
        fetchReleaseNotesTask.setOnSucceeded(event -> {
            String releaseJson = fetchReleaseNotesTask.getValue();
            webEngine.reload(); // WebView を更新
            try {
                String releaseNotes = extractReleaseNotes(releaseJson);

                // JavaScript内でエスケープするために、リリースノートの内容を処理
                String escapedReleaseNotes = releaseNotes
                        .replace("'", "\\'") // シングルクォートのエスケープ
                        .replace("\\", "\\\\") // バックスラッシュのエスケープ
                        .replace("\r", "") // キャリッジリターンを削除
                        .replace("\n", "\\n"); // 改行をJavaScript用のエスケープに変換
                try {
                    webEngine.executeScript("(function() {" +
                            "if (window.marked && document.body) {" +
                            "document.body.innerHTML = window.marked('" + escapedReleaseNotes + "');" +
                            "}" +
                            "})();");
                } catch (Exception e) {
                    LoggerHolder.get().error("releaseNotes:" + releaseNotes, e);
                    throw e;
                }
            } catch (Exception e) {
                LoggerHolder.get().error("releaseJson:" + releaseJson, e);
                webEngine.executeScript("document.body.innerHTML = '<p>リリース情報の解析中にエラーが発生しました。</p>';");
            }
        });

        // 別スレッドでタスクを開始
        new Thread(fetchReleaseNotesTask).start();

        // スタイルシートを適用
        Scene scene = new Scene(pane);

        // UpdateWindowを開く
        Stage updateStage = new Stage();
        updateStage.setTitle("航海日誌の更新");
        // 親ウィンドウの中央に表示
        updateStage.setX(mainStage.getX() + (mainStage.getWidth() - 600) / 2);
        updateStage.setY(mainStage.getY() + (mainStage.getHeight() - 400) / 2);

        updateStage.setScene(scene);
        updateStage.initOwner(mainStage);
        updateStage.show();
    }

    /**
     * GitHub リリース JSON からリリースノートを抽出
     */
    private static String extractReleaseNotes(String releaseJson) {

        try (StringReader stringReader = new StringReader(releaseJson);
                JsonReader jsonReader = Json.createReader(stringReader)) {

            JsonObject jsonObject = jsonReader.readObject();

            return jsonObject.getString("body");

        } catch (Exception e) {
            LoggerHolder.get().error("releaseJson:" + releaseJson, e);
        }
        return "";
    }
    
    private static void startUpdateProcess(String version, WebEngine webEngine) {

        Path targetDir;
        try {
            //MainClassであるLauncher.classの実行場所が、logbook-kai.jarのある場所
            targetDir = new File(Launcher.class.getProtectionDomain().getCodeSource().getLocation().toURI())
                .toPath()
                .getParent();
        } catch (Exception e) {
            LoggerHolder.get().error("logbook-kai.jarの実行場所が見つかりませんでした" , e);
            return;
        }
        
        Map<WebEngine, Stage> logStages = new HashMap<WebEngine, Stage>();
        
        Task<String> updateTask = new Task<String>() {
            @Override
            protected String call() throws Exception {
                // ダウンロードおよび更新処理のロジック
                Path tempDir = Files.createTempDirectory("logbook-kai");
                Path tempZip = tempDir.resolve("update.zip");

                String downloadURL = OPEN_URL + "/download/" + "v" + version + "/" + "logbook-kai_" + version + ".zip";
                // ダウンロード
                for (int i = 0; i < 3; i++) {
                    updateMessage("ダウンロード中... " + downloadURL);
                    try (InputStream is = new URL(downloadURL).openStream()) {
                        Files.copy(is, tempZip, StandardCopyOption.REPLACE_EXISTING);
                    }

                    if (Files.size(tempZip) > 0) {
                        break;
                    } else {
                        updateMessage("再ダウンロードしています...");
                    }
                }

                updateMessage("ダウンロード完了");
                updateMessage("更新開始");

                try (ZipFile zipFile = new ZipFile(tempZip.toFile())) {
                    zipFile.stream()
                            .filter(e -> !e.isDirectory() && e.getName().endsWith(".jar"))
                            .forEach(e -> {
                                try {
                                    Path target = Paths.get(targetDir.toString(), e.getName());
                                    Files.createDirectories(target.getParent());
                                    Files.copy(zipFile.getInputStream(e), target, StandardCopyOption.REPLACE_EXISTING);
                                    updateMessage("更新完了: " + target);
                                } catch (IOException ex) {
                                    ex.printStackTrace();
                                }
                            });
                }

                // 一時ファイルを削除
                Files.deleteIfExists(tempZip);
                Files.deleteIfExists(tempDir);

                return "更新が完了しました。航海日誌を再起動してください。";
            }
        };

        updateTask.messageProperty().addListener((obs, oldMessage, newMessage) -> {
            Platform.runLater(() -> {
                Stage logStage = logStages.get(webEngine);
                if (logStage == null) {
                    ListView<String> listView = new ListView<>();
                    logStage = new Stage();
                    Stage mainStage = WindowHolder.getInstance().getMainWindow();
                    // 親ウィンドウの中央位置に表示
                    logStage.setX(mainStage.getX() + (mainStage.getWidth() - 600) / 2);
                    logStage.setY(mainStage.getY() + (mainStage.getHeight() - 400) / 2);
                    
                    logStage.setScene(new Scene(listView, 400, 300));
                    logStage.initOwner(mainStage);
                    logStage.setTitle("更新ログ");
                    
                    logStage.show();
                    logStages.put(webEngine, logStage);
                }
                @SuppressWarnings("unchecked")
                ListView<String> listView = (ListView<String>) logStage.getScene().getRoot();
                listView.getItems().add(newMessage);
            });
        });
        
        updateTask.setOnSucceeded(event -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("更新完了");
            alert.setContentText(updateTask.getValue());
            
            Stage logStage = logStages.get(webEngine);
            if (logStage != null) {
                alert.setX(logStage.getX());
                alert.setY(logStage.getY());
            }
            
            alert.showAndWait();
            
            // logStageを閉じる
            if (logStage != null) {
                logStage.close();
            }
        });

        updateTask.setOnFailed(event -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("更新失敗");
            alert.setContentText("エラーが発生しました: " + updateTask.getException().getMessage());

            Stage logStage = logStages.get(webEngine);
            if (logStage != null) {
                alert.setX(logStage.getX());
                alert.setY(logStage.getY());
            }
            
            alert.showAndWait();
        });

        new Thread(updateTask).start();
    }
}