var WalletProvider = /** @class */ (function () {
    function WalletProvider() {
        this.simpleBaseProvider = new SimpleBaseProvider();
    }

    WalletProvider.prototype.getMyWalletInfos = function (callback, params) {
        return this.simpleBaseProvider.post("/api/wallet/getMyWalletInfos", callback, function(data) {}, params);
    };

    WalletProvider.prototype.create = function (callback, params) {
        return this.simpleBaseProvider.post("/api/wallet/create", callback, function(data) {}, params);
    };

    return WalletProvider;
}());