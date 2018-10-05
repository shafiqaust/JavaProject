var ActionLogService = /** @class */ (function () {
    function ActionLogService() {
        this.actionLogProvider = new ActionLogProvider();
    }

    ActionLogService.prototype.rendererActionLogs = function (pageNo) {
        var params = new Object();
        params.pageNo=pageNo;
        params.pageSize = 20;

        console.log(">>>>>>>>>>>>>>>>>>>>>>>   " + utils.locale_i18n('landing.regist.email.empty'));

        this.actionLogProvider.getActionLogs(function(result) {
            if (result.code == 0000) {
                var body = $("#actionLogsBodyId");
                var rows = "";
                result.data.actionLogs.forEach(function(actionLog) {
                    rows += '<tr role="row" class="odd">' +
                        '<td class="sorting_1">' + actionLog.regDtm + '</td>' +
                        '<td>' + actionLog.tag + '</td>' +
                        '<td>' + actionLog.ip + '</td>' +
                        '<td>' + actionLog.userAgent + '</td>' +
                        '</tr>';
                });
                body.html(rows);

                /**
                var pageObj = $("#actionLogs-datatable-keytable_paginate");
                rows = '<li class="paginate_button previous disabled" aria-controls="datatable-keytable" tabindex="0" id="datatable-keytable_previous">' +
                    '<a href="#">' + utils.locale_i18n('comm.paging.prev.js') + '</a>' +
                    '</li>';

                for (var page = 1;page <= result.data.pageTotalCnt; page++) {
                    var className = "paginate_button";
                    if (page -1 == result.data.pageNo) {
                        className += " active";
                    }
                    rows += '<li class="' + className + '" aria-controls="datatable-keytable" tabindex="0">' +
                        '<a href="#" onclick="actionLogProvider.rendererActionLogs(' + (page-1) + '); return false">' + page + '</a>' +
                        '</li>';
                }

                rows += '<li class="paginate_button" aria-controls="datatable-keytable" tabindex="0">' +
                     '<a href="#">' + utils.locale_i18n('comm.paging.next.js') + '</a>' +
                    '</li>';
                pageObj.html(rows);
                */
            } else {
                utils.errorAlert(utils.locale_i18n('dahsboard.accessmanage.load.fail.js'), result.msg, null);
            }
        }, params);
    };

    return ActionLogService;
}());