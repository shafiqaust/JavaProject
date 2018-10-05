var WalletService = /** @class */ (function () {
    function WalletService() {
        this.walletProvider = new WalletProvider();
        this.transactionProvider = new TransactionProvider();
    }

    WalletService.prototype.determine = function (id_value) {
        var amount = $("[id=" + id_value + "]").val();
        if (amount == "" || amount.indexOf("-") >= 0 || isNaN(amount) == true) {
            $("[id=" + id_value + "]").val(0);
            return;
        } else {
            var dotLen = amount.lastIndexOf(".") >= 0 ?  amount.substring(amount.lastIndexOf(".")).length - 1 : 0;
            if(dotLen > 8) {
                $("[id=" + id_value + "]").val(parseFloat(amount).toFixed(8));
            } else {
                $("[id=" + id_value + "]").val(amount);
            }
        }

        return amount;
    };

    WalletService.prototype.rendererMyWalletInfo = function () {
        this.walletProvider.getMyWalletInfos(function(result) {
            if (result.code == 0000) {
                var totalContent = "";
                var content1 = "";
                var content2 = "";

                var rows = result.data.infos.length / 4;
                var total = 0;

                for(var i = 0; i <= rows; i++) {
                    content1 = "<tr>";
                    content2 = "<tr>";
                    for(var j = 0;j < 4; j++, total++) {
                        console.log(">>>>>>>>>>>>>>" + total);
                        if(total >= result.data.infos.length) {
                            break;
                        } else {
                            var info = result.data.infos[total];
                            var img = "<img style=\"width:18px;height:18px;margin-right: 5px;\" src=\"" + info.coin.logoUrl + "\"/>";

                            content1 += "<td style='width:25%'>" + img + info.coin.unit + "</td>";
                            content2 += "<td style='width:25%;color:black;font-weight:bold;'>" + utils.decimalWithCommas(info.wallet.availableBalance);
                            if (info.wallet.usingBalance > 0) {
                                content2 += " (" + utils.decimalWithCommas(info.wallet.usingBalance) + '&nbsp;&nbsp;' + utils.locale_i18n('dashboard.wallet.use.title.js')  + ") " + "</td>";
                            } else {
                                content2 += "</td>"
                            }
                        }
                    }

                    content1 += "</tr>";
                    content2 += "</tr>";

                    console.log(content1 + content2);
                    totalContent += content1 + content2 + "<tr><td><td/><td><td/><td><td/><td><td/></tr>";
                }

                $("#myWalletInfoBodyId").html(totalContent);
            } else {
                utils.errorAlert(utils.locale_i18n('dashboard.wallet.load.fail.js'), result.msg, null);
            }
        }, null);
    };

    WalletService.prototype.rendererWithdrawalTransactions = function (selectionCoin, pageNo) {
        var selectionCoin = $("#selectionCoin").val();
        //$("#withdrawal-nav-li-" + selectionCoin).attr("class", "active");
        $("[id^='withdrawal-nav']").attr("class", "tab-pane");
        $("#withdrawal-nav-content-" + selectionCoin).attr("class", "tab-pane active");

        var params = new Object();
        params.category = "send";
        params.coinName = selectionCoin;
        params.pageNo = pageNo;
        params.pageSize = 100;
        this.transactionProvider.getTransactions(function(result) {
            if (result.code == 0000) {
                var transactionBody = $("#withdrawalTransactionBodyId");
                var rows = "";
                result.data.transactions.forEach(function(transaction) {
                console.log(">>>>> " + transaction.status);
                    rows += '<tr role="row" class="odd">' +
                        '<td width="7%" class="sorting_1">' + utils.convertStatusToMessage(transaction.status) + '</td>' +
                        '<td>' + (selectionCoin == "KRW" ? transaction.bankNm + "  " + transaction.address + "  " + transaction.recvNm: transaction.address) + '</td>' +
                        '<td width="5%">' + transaction.amount + '</td>' +
                        '<td width="5%">' + transaction.confirmation + '</td>' +
                        '<td>' + transaction.regDt + '</td>' +
                        '<td>' + (transaction.status == "PENDING" ? "<button type=\"button\" class=\"btn btn-danger fs-08\" onclick='cancelWithdrawal(\"" + transaction.coin.name +  "\", \"" + transaction.id + "\")' >CANCEL</button>" : transaction.completeDtm) + '</td>' +
                        '<td>' + (transaction.status == "COMPLETED" ? '<a href=' + transaction.coin.rpcUrl + transaction.txId + ' target="_blank">' + transaction.txId + '</a>' : '') + '</td>' +
                        '</tr>';
                });
                transactionBody.html(rows);
            } else {
                utils.errorAlert(utils.locale_i18n('dashboard.wallet.withdrawal.load.fail.js'), result.msg, null);
            }
        }, params);
    };

    WalletService.prototype.rendererDepositTransactions = function (selectionCoin, pageNo) {
        var selectionCoin = $("#selectionCoin").val();

        //$("#deposit-nav-li-" + selectionCoin).attr("class", "active");
        $("[id^='deposit-nav']").attr("class", "tab-pane");
        $("#deposit-nav-content-" + selectionCoin).attr("class", "tab-pane active");

        var params = new Object();
        params.category = "receive";
        params.coinName = selectionCoin;
        params.pageNo = pageNo;
        params.pageSize = 100;
        this.transactionProvider.getTransactions(function(result) {
            if (result.code == 0000) {
                var transactionBody = $("#depositTransactionBodyId");
                var rows = "";
                result.data.transactions.forEach(function(transaction) {
                    rows += '<tr role="row" class="odd">' +
                        '<td width="7%" class="sorting_1">' + utils.convertStatusToMessage(transaction.status) + '</td>' +
                        '<td>' + (selectionCoin == "KRW" ? transaction.bankNm + "  " + transaction.address + "  " + transaction.recvNm: transaction.address) + '</td>' +
                        '<td width="5%">' + transaction.amount + '</td>' +
                        '<td width="5%">' + transaction.confirmation + " / " + transaction.coin.depositAllowConfirmation + '</td>' +
                        '<td>' + transaction.regDt + '</td>' +
                        '<td>' + (transaction.status == "PENDING" ? "-" : transaction.completeDtm) + '</td>' +
                        '<td>' + (transaction.status == "COMPLETED" ? '<a href=' + transaction.coin.rpcUrl + transaction.txId + ' target="_blank">' + transaction.txId + '</a>' : '') + '</td>' +
                        //'<td>' + (transaction.status == "COMPLETED" ? '<a href="#" onclick="utils.infoAlert(\'' + utils.locale_i18n('dashboard.wallet.deposit.trade.detail.js') + '\', \'' + transaction.txId + '\', null);return false">' + utils.locale_i18n('dashboard.wallet.view.title.js') + '</a>' : "-") + '</td>' +
                        '</tr>';
                });
                transactionBody.html(rows);
            } else {
                utils.errorAlert(utils.locale_i18n('dashboard.wallet.deposit.load.fail.js'), result.msg, null);
            }
        }, params);
    };

    WalletService.prototype.doWithdrawal = function (coin) {
        var formData = formToObj(document.getElementById('withdrawal' + coin + 'FormId'));

        if (formData.amount == "" || formData.amount < parseFloat(formData.minWithdrawalAmount)) {
            utils.errorAlert(utils.locale_i18n('dashboard.wallet.withdrawal.application.fee.js') + " " + utils.locale_i18n('dashboard.wallet.over.title.js') + " " + formData.minWithdrawalAmount + " " + formData.coinUnit, null, null);
            return;
        }

        if (formData.amount <= parseFloat(formData.withdrawalFee)) {
            utils.errorAlert(utils.locale_i18n('dashboard.wallet.withdrawal.application.fee.js') + " " + utils.locale_i18n('dashboard.wallet.over.title.js') + " " + formData.withdrawalFee + " " + formData.coinUnit, null, null);
            return;
        }

        if (formData.amount > parseFloat(formData.realWithdrawalAmount)) {
            utils.errorAlert(utils.locale_i18n('dashboard.wallet.payment.application.fee.possible.js'), null, null);
            return;
        }

        if (formData.amount > parseFloat(formData.myLimitAmount)) {
            utils.errorAlert(utils.locale_i18n('dashboard.wallet.day.rest.payment.js'), null, null);
            return;
        }

        if (formData.amount > parseFloat(formData.onedayAmount)) {
            utils.errorAlert(utils.locale_i18n('dashboard.wallet.day.payment.limit.js'), null, null);
            return;
        }

        if (formData.amount > parseFloat(formData.onceAmount)) {
            utils.errorAlert(utils.locale_i18n('dashboard.wallet.one.payment.limit.js'), null, null);
            return;
        }

        if (formData.otp == "") {
            utils.errorAlert(utils.locale_i18n('dashboard.wallet.otp.empty.js'), null, null);
            return;
        }

        if (formData.address == "") {
            if (formData.coinName == "KRW") {
                utils.errorAlert(utils.locale_i18n('dashboard.wallet.withdrawal.account.js'), null, null);
            } else {
                utils.errorAlert(utils.locale_i18n('dashboard.wallet.withdrawal.address.js'), null, null);
            }
            return;
        }

        if (formData.coinName == "KRW") {
            if (formData.recvNm == "") {
                utils.errorAlert(utils.locale_i18n('dashboard.wallet.recipient.js'), null, null);
                return;
            }

            if (formData.bankNm == utils.locale_i18n('dashboard.wallet.bank.select.title.js')) {
                utils.errorAlert(utils.locale_i18n('dashboard.wallet.bank.select.js'), null, null);
                return;
            }
        }

        this.transactionProvider.requestWithdrawal(function(result) {
            if (result.code == 0000) {
                utils.successAlert(utils.locale_i18n('dashboard.wallet.withdrawal.request.success.js'), result.msg, function () {
                    location.href = "/withdrawal_manage?selectionCoin=" + coin;
                });
            } else {
                utils.errorAlert(utils.locale_i18n('dashboard.wallet.withdrawal.request.fail.js'), result.msg, null);
            }
        }, formData);
    };

    WalletService.prototype.onCancelWithdrawal = function (coin, transaction_id) {
        var params = new Object();
        params.transaction_id = transaction_id;

        this.transactionProvider.cancelWithdrawal(function(result) {
            if (result.code == 0000) {
                utils.successAlert(utils.locale_i18n('dashboard.wallet.withdrawal.cancel.success.js'), result.msg, function () {
                    location.href = "/withdrawal_manage?selectionCoin=" + coin;
                });
            } else {
                utils.errorAlert(utils.locale_i18n('dashboard.wallet.withdrawal.cancel.fail.js'), result.msg, null);
            }
        }, params);
    };

    WalletService.prototype.createWallet = function (coin) {
        //$("#deposit-history-title-id").text(coin + utils.locale_i18n('dashboard.wallet.deposit.trade.history.js'));

        var params = new Object();
        params.coinName = coin;

        this.walletProvider.create(function(result) {
            if (result.code == 0000) {
                utils.successAlert(utils.locale_i18n('dashboard.wallet.deposit.generate.success.js'), result.msg, function () {
                    location.href = "/deposit_manage?selectionCoin=" + coin;
                });
            } else {
                utils.errorAlert(utils.locale_i18n('dashboard.wallet.deposit.generate.fail.js'), result.msg, null);
            }
        }, params);
    };

    return WalletService;
}());

function showMyAssets(content1, content2, total, infoList) {
}