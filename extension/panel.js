// 最大表示件数
const MAX_LOGS = 20;

// 現在のリクエストログのリスト
const logs = [];

// ネットワークリクエストの監視を開始
chrome.devtools.network.onRequestFinished.addListener((request) => {
  const fullUrl = request.request.url;
  const url = new URL(fullUrl);

  const uri = url.pathname;
  
  if (
    uri.includes("/kcsapi/") ||
    uri.includes("/kcs2/resources/ship/") ||
    uri.includes("/kcs2/img/common/") ||
    uri.includes("/kcs2/img/duty/") ||
    uri.includes("/kcs2/img/sally/")
  ) {
    const method = request.request.method;
    const endpoint = `${url.origin}${url.pathname}`;
    const queryString = url.search;

    const queryParams = {};
    url.searchParams.forEach((value, key) => {
      queryParams[key] = value;
    });

    // レスポンスボディを取得
    request.getContent((content, encoding) => {
      // 古いログの削除
      if (logs.length >= MAX_LOGS) {
        logs.shift(); // 最も古いログを削除
        const requestsContainer = document.getElementById("request");
        requestsContainer.removeChild(requestsContainer.firstChild);
      }

      // リクエストボディの取得（POSTの場合のみ）
      let requestBodyHtml = "";
      if (method === "POST" && request.request.postData) {
        const postData = request.request.postData.text || "(No Request Body)";
        requestBodyHtml = `
          <h4>Request Body:</h4>
          <pre>${postData}</pre>
        `;
      }

      // レスポンスの表示色を設定
      const responseColor =
        encoding === "base64" ? "rgba(255, 200, 200, 0.5)" : "rgba(200, 220, 255, 0.5)";

      // URIが"/kcs2/"かつ、request.timings内の通信情報がない場合キャッシュからの取得とする
      const isConnect = request.timings && request.timings.connect > 0 && request.timings.send > 0;
      const isCacheLike = uri.includes("/kcs2/") && !isConnect;

      // 折り畳み時の背景色（キャッシュの場合は濃いグレー）
      const headerColor = isCacheLike ? "rgba(50, 50, 50, 0.8)" : "#eee";
      const endpointDisplay = isCacheLike ? `${endpoint} (Cache)` : `${endpoint} ${request.time.toFixed(3)}(ms)`;

      const timings = request.timings
        ? `
          <h4>Timing Information:</h4>
          <pre>${JSON.stringify(request.timings, null, 2)}</pre>
        `
        : "<p><strong>Timing Information:</strong> (No timing data available)</p>";

      const requestHtml = `
        <div class="request-header" style="cursor: pointer; background-color: ${headerColor};">
          <strong>Endpoint:</strong> ${endpointDisplay}
        </div>
        <div class="request-body" style="display: none;">
          <h4>Request</h4>
          <p><strong>Method:</strong> ${method}</p>
          <p><strong>URL:</strong> ${fullUrl}</p>
          <p><strong>Endpoint:</strong> ${endpoint}</p>
          <p><strong>URI:</strong> ${uri}</p>
          <p><strong>Query String:</strong> ${queryString}</p>
          <h4>Query Parameters:</h4>
          <pre>${JSON.stringify(queryParams, null, 2)}</pre>
          ${requestBodyHtml}
          <h4>Response Body:</h4>
          <pre style="background-color: ${responseColor};">${content || "(No Response Body)"}</pre>
          ${timings}
        </div>
      `;

      const requestsContainer = document.getElementById("request");

      // 新しいリクエストを追加
      const requestElement = document.createElement("div");
      requestElement.className = "request";
      requestElement.innerHTML = requestHtml;
      requestsContainer.appendChild(requestElement);

      // 折り畳みの設定
      const header = requestElement.querySelector(".request-header");
      const body = requestElement.querySelector(".request-body");
      header.addEventListener("click", () => {
        body.style.display = body.style.display === "none" ? "block" : "none";
      });

      // ログを保持
      logs.push(requestElement);
    });
  }
});
