# 航海日誌 Proxy設定

航海日誌を使用しながら艦これで遊ぶには、プロキシの設定が必要となります。

お使いのブラウザによって設定方法が異なりますのでご注意ください。

ここでは以下のブラウザにおける設定方法について説明します。

* Mozilla Firefoxの場合
* Google Chromeの場合

なおプロキシの設定を行った場合、航海日誌を起動しないと艦これが起動しなくなりますので注意してください。
(航海日誌を使用しない場合設定を戻して頂く必要があります(逆の手順で戻します))

## Firefox の場合

### Firefox の場合 1/3

航海日誌を起動後、[その他]-[自動プロキシ構成スクリプト]を選択します。

※航海日誌が[スレッドが予期せず終了しました]というエラーメッセージを出した場合、ポートを8888から任意の別のポートに変更して下さい。

![メイン画面](images/firefox_1.png)

### Firefox の場合 2/3

[保存...]を選択してスクリプトファイルを保存後、

 Firefox用のアドレスをコピーします。

![自動プロキシ構成スクリプト](images/firefox_2.png)

### Firefox の場合 3/3

 [オプション](about:preferences)を開き[詳細]-[ネットワーク]-[接続設定]を選択します。
[自動プロキシ設定スクリプトURL]にアドレスを貼り付けます。

![preferences](images/firefox_3.png)

設定は以上です。

## Chrome の場合

Google Chrome単体ではプロキシの設定が出来ないため拡張機能のインストールが必要です。
ここでは[SwitchySharp](https://chrome.google.com/webstore/detail/proxy-switchysharp/dpplabbmogkhghncfbfdeeokoefdjegm)を例にして説明します。

### Chrome の場合 1/4

航海日誌を起動後、[その他]-[自動プロキシ構成スクリプト]を選択します。

※航海日誌が[スレッドが予期せず終了しました]というエラーメッセージを出した場合、ポートを8888から任意の別のポートに変更して下さい。

![メイン画面](images/chrome_1.png)

### Chrome の場合 2/4

[保存...]を選択してスクリプトファイルを保存します。(Firefox用のアドレスは使用しません)

![自動プロキシ構成スクリプト](images/chrome_2.png)

### Chrome の場合 3/4

SwitchySharpのオプションで新しいプロファイルを作成します。

Import PAC Fileを選択し、前の手順で作成したPACファイルを選択して保存します。

![SwitchySharp](images/chrome_3.png)

### Chrome の場合 4/4

作成したプロファイルを有効にします。

![SwitchySharp](images/chrome_4.png)

## Chrome の場合(SwitchyOmega)

### Chrome(SwitchyOmega) の場合 1/4

"Chromeの場合1/4","Chromeの場合2/4"と共通の作業を行い、PACファイルを保存してください。

### Chrome(SwitchyOmega) の場合 2/4

SwitchyOmegaのオプションで新しいプロファイルを作成します。

New profileから、ProfileNameを例として"艦これSwitchyOmega"と設定し、PAC Profileを選択します。

![SwitchySharp](images/chrome_SO1.png)

### Chrome(SwitchyOmega) の場合 3/4

PACファイルをテキストエディタなどで開き、設定内容を"PAC Script"欄にコピーし、"Apply changes"で保存します。

![SwitchySharp](images/chrome_SO2.png)

### Chrome(SwitchyOmega) の場合 4/4

作成したプロファイルを有効にします。

![SwitchySharp](images/chrome_SO3.png)
