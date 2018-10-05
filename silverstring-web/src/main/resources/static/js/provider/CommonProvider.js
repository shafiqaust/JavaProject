var CommonProvider = /** @class */ (function () {
    function CommonProvider() {
        this.simpleBaseProvider = new SimpleBaseProvider();
    }

    CommonProvider.prototype.ticker = function (callback, params) {
        return this.simpleBaseProvider.post("/api/common/ticker", callback, function(data) {}, params);
    };

    CommonProvider.prototype.getEthUsdPrice = function (callback, params) {
        return this.simpleBaseProvider.get("/api/common/getEthUsdPrice", callback, function(data) {}, params);
    };

    CommonProvider.prototype.getTopN1Notice = function (callback, params) {
        return this.simpleBaseProvider.post("/api/common/getTopN1Notice", callback, function(data) {}, params);
    };

    CommonProvider.prototype.getCoinAvgPrice = function (callback, params) {
        return this.simpleBaseProvider.post("/api/common/getCoinAvgPrice", callback, function(data) {}, params);
    };

    CommonProvider.prototype.getAllCoinAvgPrices = function (callback, params) {
        return this.simpleBaseProvider.post("/api/common/getAllCoinAvgPrices", callback, function(data) {}, params);
    };

    CommonProvider.prototype.getAllIcoRecommend = function (callback, params) {
        return this.simpleBaseProvider.post("/api/common/getAllIcoRecommend", callback, function(data) {}, params);
    };

    CommonProvider.prototype.get24hGraphData = function (callback, params) {
        return this.simpleBaseProvider.post("/api/common/get24hGraphData", callback, function(data) {}, params);
    };

    CommonProvider.prototype.getCategoriedCoinAvgPrices = function (callback, params) {
        return this.simpleBaseProvider.post("/api/common/getCategoriedCoinAvgPrices", callback, function(data) {}, params);
    };

    return CommonProvider;
}());