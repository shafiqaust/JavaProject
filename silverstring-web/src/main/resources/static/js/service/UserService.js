var UserService = /** @class */ (function () {
    function UserService() {
        this.userProvider = new UserProvider();
    }

    UserService.prototype.releaseMember = function () {
        var formData = formToObj(document.getElementById('releaseFormId'));
        if (formData.otp == "") {
            utils.errorAlert(utils.locale_i18n('dashboard.myinfomanange.member.withdrawal.fail.js'), null, null);
            return;
        }

        this.userProvider.releaseMember(function(result) {
            if (result.code == 0000) {
                utils.successAlert(utils.locale_i18n('dashboard.myinfomanage.memeber.withdrawal.success.js'), result.msg, function () {
                    location.href="/logout";
                });
            } else {
                utils.errorAlert(utils.locale_i18n('dashboard.myinfomanange.member.withdrawal.fail.js'), result.msg, null);
            }
        }, formData);
    };

    UserService.prototype.changePassword = function () {
        var formData = formToObj(document.getElementById('myInfoFormId'));

        console.log("password1 >>>>> " + formData.password);
        console.log("password2 >>>>> " + formData.newPassword);
        console.log("password2 >>>>> " + formData.newPassword);

        if (formData.password == "") {
            utils.errorAlert(utils.locale_i18n('dashboard.myinfomanange.pwd.change.fail.js'), null, null);
            return;
        }

        if (formData.newPassword == "") {
            utils.errorAlert(utils.locale_i18n('dashboard.myinfomanange.pwd.change.fail.js'), null, null);
            return;
        }

        if (formData.newPasswordRe == "") {
            utils.errorAlert(utils.locale_i18n('dashboard.myinfomanange.pwd.change.fail.js'), null, null);
            return;
        }

        if (formData.newPasswordRe != formData.newPassword) {
            utils.errorAlert(utils.locale_i18n('dashboard.myinfomanange.pwd.change.fail.js'), null, null);
            return;
        }

        if (formData.newPasswordRe.length < 8 ) {
            utils.errorAlert(utils.locale_i18n('dashboard.myinfomanange.pwd.change.fail.js'), null, null);
            return;
        }

        if (formData.otp == "") {
            utils.errorAlert(utils.locale_i18n('dashboard.myinfomanange.pwd.change.fail.js'), null, null);
            return;
        }

        this.userProvider.changePassword(function(result) {
            if (result.code == 0000) {
                utils.successAlert(utils.locale_i18n('dashboard.myinfomanange.pwd.change.success.js'), result.msg, function () {
                    window.location.reload();
                });
            } else {
                utils.errorAlert(utils.locale_i18n('dashboard.myinfomanange.pwd.change.fail.js'), result.msg, null);
            }
        }, formData);
    };

    UserService.prototype.reqOtpRelease = function () {
        var formData = formToObj(document.getElementById('otpFormId'));

        if (formData.otpReleaseValue == "") {
            utils.errorAlert(utils.locale_i18n('dashboard.otpmanage.use.input.otp.desc1'), null, null);
            return;
        }

        this.userProvider.releaseOtp(function(result) {
            if (result.code == 0000) {
                utils.successAlert(utils.locale_i18n('dashboard.otpmanage.change.success.js'), result.msg, function () {
                    location.href="/otp_manage";
                });
            } else {
                utils.errorAlert(utils.locale_i18n('dashboard.otpmanage.change.fail.js'), result.msg, null);
            }
        }, formData);
    };

    UserService.prototype.reqOtpConfirm = function () {
        var formData = formToObj(document.getElementById('otpFormId'));

        if (formData.otpConfirmValue == "") {
            utils.errorAlert(utils.locale_i18n('dashboard.otpmanage.use.input.otp.desc1'), null, null);
            return;
        }

        this.userProvider.confirmOtp(function(result) {
            if (result.code == 0000) {
                utils.successAlert(utils.locale_i18n('dashboard.otpmanage.change.success.js'), result.msg, function () {
                    location.href="/otp_manage";
                });
            } else {
                utils.errorAlert(utils.locale_i18n('dashboard.otpmanage.change.fail.js'), result.msg, null);
            }
        }, formData);
    };

    UserService.prototype.changeOtpKey = function () {
        var formData = formToObj(document.getElementById('otpFormId'));
        this.userProvider.changeOtpKey(function(result) {
            if (result.code == 0000) {
                console.log(">>>>>>>>>>>>> " + decodeURIComponent(result.data.qrbarcodeURL));
                $("#qrbarcodeURL").attr("src",  decodeURIComponent(result.data.qrbarcodeURL));
                $("#otpCode").html(result.data.otpCode);
            } else {
                utils.errorAlert(utils.locale_i18n('dashboard.otpmanage.change.fail.js'), result.msg, null);
            }
        }, formData);
    };

    UserService.prototype.getOtpStatus = function () {
        var formData = formToObj(document.getElementById('otpFormId'));
        this.userProvider.getOtpStatus(function(result) {
            if (result.code == 0000) {
                $("#otpStatus").val(result.data.otpStatus);
            } else {
                $("#otpStatus").val("false");
            }
        }, formData);
    };

    UserService.prototype.getIsOtpUser = function (email) {
        var params = new Object();
        params.email = email;

        this.userProvider.getIsOtpUser(function(result) {
            if (result.code == 0000) {
                $("#otpStatus").val(result.data.otpStatus);
            } else {
                $("#otpStatus").val("false");
            }
        }, params);
    };

    return UserService;
}());