var SellingProvider = /** @class */ (function () {
    function SellingProvider() {
        this.simpleBaseProvider = new SimpleBaseProvider();
    }

    SellingProvider.prototype.getCoinSelling = function (callback, params) {
        return this.simpleBaseProvider.post("/api/wallet/getCoinSelling", callback, function(data) {}, params);
    };

    return SellingProvider;
}());