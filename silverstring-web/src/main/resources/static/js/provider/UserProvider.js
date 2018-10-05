var UserProvider = /** @class */ (function () {
    function UserProvider() {
        this.simpleBaseProvider = new SimpleBaseProvider();
    }

    UserProvider.prototype.changePassword = function (callback, params) {
        return this.simpleBaseProvider.post("/api/user/changePassword", callback, function(data) {}, params);
    };

    UserProvider.prototype.releaseMember = function (callback, params) {
        return this.simpleBaseProvider.post("/api/user/releaseMember", callback, function(data) {}, params);
    };

    UserProvider.prototype.releaseOtp = function (callback, params) {
        return this.simpleBaseProvider.postAsync("/api/user/releaseOtp", callback, function(data) {}, params);
    };

    UserProvider.prototype.confirmOtp = function (callback, params) {
        return this.simpleBaseProvider.postAsync("/api/user/confirmOtp", callback, function(data) {}, params);
    };

    UserProvider.prototype.changeOtpKey = function (callback, params) {
        return this.simpleBaseProvider.postAsync("/api/user/changeOtpKey", callback, function(data) {}, params);
    };

    UserProvider.prototype.getOtpStatus = function (callback, params) {
        return this.simpleBaseProvider.postAsync("/api/user/getOtpStatus", callback, function(data) {}, params);
    };

    UserProvider.prototype.getIsOtpUser = function (callback, params) {
        return this.simpleBaseProvider.postAsync("/api/common/getIsOtpUser", callback, function(data) {}, params);
    };

    return UserProvider;
}());