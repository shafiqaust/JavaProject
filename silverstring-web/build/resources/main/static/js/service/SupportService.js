var SupportService = /** @class */ (function () {
    function SupportService() {
        this.supportProvider = new SupportProvider();
    }

    SupportService.prototype.rendererSupports = function (pageNo) {
        var params = new Object();
        params.pageNo = pageNo;
        params.pageSize = 100;
        this.supportProvider.getSupports(function(result) {
            if (result.code == 0000) {
                var body = $("#supportBodyId");
                var rows = "";
                result.data.contents.forEach(function(content) {
                    rows += '<tr role="row" class="odd">' +
                        '<td class="sorting_1" width="20%">' + content.regDtm + '</td>' +
                        '<td width="10%">' + (content.type == 'REQUEST' ? utils.locale_i18n('dashboard.supportmanage.question.js') : utils.locale_i18n('dashboard.supportmanage.answer.js'))+ '</td>' +
                        '<td width="10%">' + content.status + '</td>' +
                        '<td><div align="left"><a href="support_view?id=' + content.id + '">' + content.title + '</div></td>' +
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
                        '<a href="#" onclick="supportService.rendererSupports(' + (page-1) + '); return false">' + page + '</a>' +
                        '</li>';
                }

                rows += '<li class="paginate_button" aria-controls="datatable-keytable" tabindex="0">' +
                     '<a href="#">' + utils.locale_i18n('comm.paging.next.js') + '</a>' +
                    '</li>';
                pageObj.html(rows);
                */
            } else {
                utils.errorAlert(utils.locale_i18n('dashboard.supportmanage.load.fail.js'), result.msg, null);
            }
        }, params);
    };


    SupportService.prototype.regist = function () {
        var params = new Object();
        params.title = $("#add_title").val();
        params.content = $("#add_content").val();

        if (params.title == "" || params.content == "") {
            utils.errorAlert(utils.locale_i18n('dashboard.supportmanage.correct.js'), null, null);
            return;
        }

        this.supportProvider.regist(function(result) {
            if (result.code == 0000) {
                supportService.rendererSupports(0);
                utils.successAlert(utils.locale_i18n('dashboard.supportmanage.success.js'), result.msg, null);
            } else {
                utils.errorAlert(utils.locale_i18n('dashboard.supportmanage.fail.js'), result.msg, null);
            }
        }, params);
    };

    return SupportService;
}());