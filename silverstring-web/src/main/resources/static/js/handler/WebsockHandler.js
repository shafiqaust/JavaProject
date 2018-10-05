var WebsockHandler = /** @class */ (function () {

    function WebsockHandler() {
        this.tradeService = new TradeService();
    }

    WebsockHandler.prototype.connect = function() {
        var BASE_URL = $("#baseUrl").val();
        var sock = new SockJS(BASE_URL + '/websock');
        var client = Stomp.over(sock);
        client.connect({}, function (frame) {
            console.log('connected stomp over sockjs');
            client.subscribe('/topic/exchange', function(messagePacket) {
                var payload = JSON.parse(messagePacket.body);

                console.log("================== " + payload);

                var cmd = payload.cmd;
                var scope = payload.scope;
                var _coin = payload.coin;
                var _baseCoin = payload.baseCoin;
                var userId = payload.userId;
                var data = payload.data;
                var selectedCoin = $('#selectionCoin').val();
                var baseCoin = $('#baseCoin').val();
                var selectedUserId = $('#userId').val();

                console.log("cmd ================== " + cmd);

                if (cmd == "CHART") {
                    if (selectedCoin == _coin && baseCoin == _baseCoin) {
                        this.tradeService.rendererCoinAvgPrice(selectionCoin, baseCoin);
                        console.log("!!!!=====",data[0] + "," +data[1]+ "," +data[2]+ "," +data[3]+ "," +data[4]);
                        series.addPoint([data[0], data[1], data[2], data[3], data[4]], true, true);
                    }
                }

                if (cmd == "TRADE") {
                    this.tradeService.rendererCategoriedCurrentCoinInfos(baseCoin);
                    this.tradeService.rendererCoinAvgPrice(selectionCoin, baseCoin); //coinAvgMarkAndPercentId
                    this.tradeService.rendererMyWalletInfo(); // myWalletInfoBodyId
                    this.tradeService.rendererHogaOrders(selectionCoin, baseCoin, 6); //buyHogaBodyId,sellHogaBodyId
                    this.tradeService.rendererMarketHistoryOrders(selectionCoin, baseCoin, 10); //marketHistoryBodyId
                    this.tradeService.rendererMyHistoryOrders(selectionCoin, baseCoin, 0, 10); //myHistoryBodyId
                    this.tradeService.rendererMyOrders(selectionCoin, baseCoin, 0); //myOrderBodyId
                }
            });
        });
    };

    return WebsockHandler;
}());