var SupportProvider = /** @class */ (function () {
    function SupportProvider() {
        this.simpleBaseProvider = new SimpleBaseProvider();
    }

    SupportProvider.prototype.getSupports = function (callback, params) {
        return this.simpleBaseProvider.post("/api/support/getSupports", callback, function(data) {}, params);
    };

    SupportProvider.prototype.regist = function (callback, params) {
        return this.simpleBaseProvider.post("/api/support/regist", callback, function(data) {}, params);
    };

    return SupportProvider;
}());