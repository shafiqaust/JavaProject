function Utils () {
    this.isFloat = function(x) {
        var floatRegex = /^-?\d+(?:[.,]\d*?)?$/;
        if (!floatRegex.test(x))
            return false;

        x = parseFloat(x);
        if (isNaN(x))
            return false;
        return true;
    }

    this.numberWithCommas = function(x) {
        return x.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ",");
    };

    this.decimalWithCommas = function(x) {
        if (x.toString().indexOf("e") > 0) {
            return x.toFixed(8).replace(/(0+$)/, "");
            //} else if (utils.isFloat(x)) {
            //    return x.toFixed(8).replace(/(0+$)/, "");
        } else {
            return x;
        }
    }

    this.isEmpty = function (val) {
        var dressedVal = val.replace(/\s/g, "");
        if (dressedVal == null || dressedVal.length <= 0) {
            return true;
        }
        return false;
    };

    this.showInfoAlert = function (content) {
        $.Notification.notify("info",'top center', "INFORMATION", content);
    };
    this.showErrorAlert = function (content) {
        if((content != null) && (content.indexOf("session") > 0)) {
            window.location.reload();
        } else if((content != null) && ((content.indexOf("Error") > 0) || (content.indexOf("Exception") > 0) || (content.indexOf("error") > 0) || (content.indexOf("exception") > 0))) {
            $.Notification.notify("error",'top center', "(>_<) ERROR", utils.locale_i18n('utils.abormal.msg'));
        } else {
            $.Notification.notify("error",'top center', "(>_<) ERROR", content);
        }
    };

    this.validateEmail = function (email) {
        var re = /^(([^<>()\[\]\\.,;:\s@"]+(\.[^<>()\[\]\\.,;:\s@"]+)*)|(".+"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/;
        return re.test(email);
    };

    this.successAlert = function (title, text, doneFunction) {
        swal({
            title: title,
            text: text,
            type: "success",
            showCancelButton: false,
            confirmButtonClass: 'btn-success waves-effect waves-light',
            confirmButtonText: utils.locale_i18n('utils.modal.close')
        }, doneFunction);
    }

    this.errorAlert = function (title, text, doneFunction) {
        if((text != null) && (text.indexOf("session") > 0)) {
            window.location.reload();
        } else if((text != null) && ((text.indexOf("Error") > 0) || (text.indexOf("Exception") > 0) || (text.indexOf("error") > 0) || (text.indexOf("exception") > 0))) {
            swal({
                title: title,
                text: text,
                type: "error",
                showCancelButton: false,
                confirmButtonClass: 'btn-danger waves-effect waves-light',
                confirmButtonText: utils.locale_i18n('utils.abormal.msg')
            }, doneFunction);
        } else {
            swal({
                title: title,
                text: text,
                type: "error",
                showCancelButton: false,
                confirmButtonClass: 'btn-danger waves-effect waves-light',
                confirmButtonText: utils.locale_i18n('utils.modal.close')
            }, doneFunction);
        }
    }

    this.infoAlert = function (title, text, doneFunction) {
        swal({
            title: title,
            text: text,
            type: "info",
            showCancelButton: false,
            confirmButtonClass: 'btn-success waves-effect waves-light',
            confirmButtonText: utils.locale_i18n('utils.modal.close')
        }, doneFunction);
    }

    this.pageReload = function () {
        window.location.reload();
    }

    this.convertStatusToMessage = function (status) {
        if (status == "PENDING") {
            return utils.locale_i18n('utils.status.msg1');
        } else if (status == "APPROVAL") {
            return utils.locale_i18n('utils.status.msg2');
        } else if (status == "COMPLETED") {
            return utils.locale_i18n('utils.status.msg3');
        } else if (status == "FAILED") {
            return utils.locale_i18n('utils.status.msg4');
        } else if (status == "CANCEL") {
            return utils.locale_i18n('utils.status.msg5');
        }
    }

    this.locale_init = function (langCode) {
        jQuery.i18n.properties({
            name:'',
            path:'/api/common/properties/',
            mode:'map',
            language:langCode,
            debug: true,
            callback: function () {
            }
        });
    }

    this.locale_i18n = function (msg) {
        var args = "\""+ msg + "\"";
        for (var i = 1; i < arguments.length; i++) {
            args += ", \"" + arguments[i] + "\"";
        }
        /*
        if (parent != this) {
            //return eval("parent.i18n(" + args + ")");
        }
        */
        return eval("jQuery.i18n.prop(" + args + ")");
    }
}

var utils = new Utils();