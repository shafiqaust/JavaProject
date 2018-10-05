var ActionLogProvider = /** @class */ (function () {
    function ActionLogProvider() {
        this.simpleBaseProvider = new SimpleBaseProvider();
    }

    ActionLogProvider.prototype.getActionLogs = function (callback, params) {
        return this.simpleBaseProvider.post("/api/user/getActionLogs", callback, function(data) {}, params);
    };

    return ActionLogProvider;
}());