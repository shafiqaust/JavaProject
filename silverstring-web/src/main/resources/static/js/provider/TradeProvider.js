var TradeProvider = /** @class */ (function () {
    function TradeProvider() {
        this.simpleBaseProvider = new SimpleBaseProvider();
    }

    TradeProvider.prototype.buy = function (callback, params) {
        return this.simpleBaseProvider.post("/api/trade/buy", callback, function(data) {}, params);
    };

    TradeProvider.prototype.sell = function (callback, params) {
        return this.simpleBaseProvider.post("/api/trade/sell", callback, function(data) {}, params);
    };

    TradeProvider.prototype.cancel = function (callback, params) {
        return this.simpleBaseProvider.post("/api/trade/cancel", callback, function(data) {}, params);
    };

    TradeProvider.prototype.getHogas = function (callback, params) {
        return this.simpleBaseProvider.post("/api/trade/getHogas", callback, function(data) {}, params);
    };

    TradeProvider.prototype.getMyOrders = function (callback, params) {
        return this.simpleBaseProvider.post("/api/trade/getMyOrders", callback, function(data) {}, params);
    };

    TradeProvider.prototype.getMarketHistoryOrders = function (callback, params) {
        return this.simpleBaseProvider.post("/api/trade/getMarketHistoryOrders", callback, function(data) {}, params);
    };

    TradeProvider.prototype.getMyHistoryOrders = function (callback, params) {
        return this.simpleBaseProvider.post("/api/trade/getMyHistoryOrders", callback, function(data) {}, params);
    };

    return TradeProvider;
}());