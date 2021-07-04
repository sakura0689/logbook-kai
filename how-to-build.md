# ビルド手順

## ビルド

以下のコマンドを実行して、ビルドします

```
mvn package
```

## テスト

以下のコマンドを実行して、テストします

```
mvn test
```

## テスト(テストレポート込み surefire,jacoco)

以下のコマンドを実行して、テストします

```
mvn test　-Dmaven.test.failure.ignore=true site -DgenerateReports=false surefire-report:report jacoco:report
```
