REM UTF-8対応
chcp 65001 >nul

SET JVM_OPT=-XX:MaxMetaspaceSize=256M

REM update/logbook-kai.jar が存在するか確認
IF EXIST update\logbook-kai.jar (
    echo update/logbook-kai.jar を ./logbook-kai.jar にコピーしています...
    COPY /Y update\logbook-kai.jar logbook-kai.jar
    echo コピーが完了しました。
) ELSE (
    echo update/logbook-kai.jar が見つかりませんでした。コピーをスキップします。
)

REM アプリケーションを起動
START javaw %JVM_OPT% -jar logbook-kai.jar
