var TransactionProvider = /** @class */ (function () {
    function TransactionProvider() {
        this.simpleBaseProvider = new SimpleBaseProvider();
    }

    TransactionProvider.prototype.requestWithdrawal = function (callback, params) {
        return this.simpleBaseProvider.post("/api/transaction/requestWithdrawal", callback, function(data) {}, params);
    };

    TransactionProvider.prototype.cancelWithdrawal = function (callback, params) {
        return this.simpleBaseProvider.post("/api/transaction/cancelWithdrawal", callback, function(data) {}, params);
    };

    TransactionProvider.prototype.getTransactions = function (callback, params) {
        return this.simpleBaseProvider.post("/api/transaction/getTransactions", callback, function(data) {}, params);
    };

    return TransactionProvider;
}());