var AuthService = /** @class */ (function () {
    function AuthService() {
    }

    AuthService.prototype.uploadIdCard = function (e) {
        var ext = $("#idcard-file").val().split(".").pop().toLowerCase();
        if(ext.length > 0){
            if($.inArray(ext, ["gif","png","jpg","jpeg"]) == -1) {
                utils.errorAlert(utils.locale_i18n('dashboard.authmanage.imgkind.js'), null, null);
                return false;
            }
        }

        $.ajax({
            url: '/api/level/upload/idcard',
            type: 'post',
            enctype: 'multipart/form-data',
            processData: false,
            contentType: false,
            cache: false,
            data: new FormData($("#idcardUploadForm")[0]),
            success: function(data){
                utils.successAlert(utils.locale_i18n('dashboard.authmanage.fileupload.js'), null, null);
                $("#idcard-file").val(null);
            },
            error: function (data) {
                utils.errorAlert(utils.locale_i18n('dashboard.authmanage.fail.js'), result.msg, null);
                $("#idcard-file").val(null);
            }
        })
    };

    AuthService.prototype.uploadDoc = function (e) {
        var ext = $("#doc-file").val().split(".").pop().toLowerCase();
        if(ext.length > 0) {
            if($.inArray(ext, ["gif","png","jpg","jpeg"]) == -1) {
                utils.errorAlert(utils.locale_i18n('dashboard.authmanage.imgkind.js'), null, null);
                return false;
            }
        }

        $.ajax({
            url: '/api/level/upload/doc',
            type: 'post',
            enctype: 'multipart/form-data',
            processData: false,
            contentType: false,
            cache: false,
            data: new FormData($("#docUploadForm")[0]),
            success: function(data) {
                utils.successAlert(utils.locale_i18n('dashboard.authmanage.success.js'), utils.locale_i18n('dashboard.authmanage.fileupload.js'), null);
                $("#doc-file").val(null);
            },
            error: function (data) {
                utils.errorAlert(utils.locale_i18n('dashboard.authmanage.fail.js'), result.msg, null);
                $("#doc-file").val(null);
            }
        })
    };

    return AuthService;
}());