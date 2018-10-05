var TradeService = /** @class */ (function () {
    function TradeService() {
        this.commonProvider = new CommonProvider();
        this.walletProvider = new WalletProvider();
        this.tradeProvider = new TradeProvider();
    }

    TradeService.prototype.selectHogaOrder = function (type, price) {
        $("#" + type + "Price").val(price);
        this.determine(type, 0);
    };

    TradeService.prototype.determineBalance = function (type) {
        // price 입력값 체크
        var price = $("#" + type + "Price").val();
        if (price == "" || price.indexOf("-") >= 0 || isNaN(price) == true) {
            $("#" + type + "Price").val(0);
            return;
        } else {
            var dotLen = price.lastIndexOf(".") >= 0 ?  price.substring(price.lastIndexOf(".")).length - 1 : 0;
            if(dotLen > 8) {
                $("#" + type + "Price").val(parseFloat(price).toFixed(8));
            } else {
                $("#" + type + "Price").val(price);
            }
        }

        // amount 입력값 체크
        var amount = $("#" + type + "Amount").val();
        if (amount == "" || amount.indexOf("-") >= 0 || isNaN(amount) == true) {
            $("#" + type + "Amount").val(0);
            return;
        } else {
            var dotLen = amount.lastIndexOf(".") >= 0 ?  amount.substring(amount.lastIndexOf(".")).length - 1 : 0;
            if(dotLen > 8) {
                $("#" + type + "Amount").val(parseFloat(amount).toFixed(8));
            } else {
                $("#" + type + "Amount").val(amount);
            }
        }
    };

    TradeService.prototype.determine = function (type, percent) {
        this.determineBalance(type)

        var formData = formToObj(document.getElementById(type + 'FormId'));
        var price = $("#" + type + "Price").val();//가격
        var amount = $("#" + type + "Amount").val();//수량

        //사용가능금액
        var availableBalance = 0;
        if (type == "buy") {
            availableBalance = formData.baseWalletAvailableBalance;
        } else if (type == "sell") {
            availableBalance = formData.walletAvailableBalance;
        } else {
            return;
        }

        // 판매금액
        var balance = 0;
        var fee = 0;
        if (type == "buy") {
            balance = (price * amount).toFixed(8);
        } else {
            balance = amount;
        }

        //수수료
        var fee = (balance * (formData.tradingFeePercent / 100).toFixed(8)).toFixed(8);

        //구매금액
        var totalBalance = 0;
        if (type == "buy") {
            totalBalance = amount;
        } else {
            totalBalance = (price * amount).toFixed(8);
        }

        balance = (parseFloat(balance) + parseFloat(fee)).toFixed(8);

        $("#" + type + "Balance").text(balance);
        $("#" + type + "Fee").text(fee);
        $("#" + type + "TotalBalance").text(totalBalance);

        console.log(">>>>>> price " + price);
        console.log(">>>>>> amount " + amount);
        console.log(">>>>>> balance " + balance);
        console.log(">>>>>> free " + fee);
        console.log(">>>>>> totalBalance " + totalBalance);

        return balance;
    };

    TradeService.prototype.rendererCoinAvgPrice = function (selectionCoin, baseCoin) {
        var params = new Object();
        params.coin = selectionCoin;
        params.baseCoin = baseCoin;

        this.commonProvider.getCoinAvgPrice(function(result) {
            var data = result.data;
            $("#coinAvgPriceId").text(utils.decimalWithCommas(data.price));
            var content = "";
            if (data.marker == "+") {
                $("#coinAvgMarkAndPercentId").css("color", "green");
                content = data.marker + " " + data.changePercent + "%";
            } else if (data.marker == "-") {
                $("#coinAvgMarkAndPercentId").css("color", "red");
                content = data.marker + " " + data.changePercent + "%";
            } else {
                $("#coinAvgMarkAndPercentId").css("color", "black");
                content = " 0%";
            }
            $("#coinAvgMarkAndPercentId").text(content);
            $("#coinTotalTradeAmount24hId").text(utils.decimalWithCommas(data.totalTradeAmount24h));
        }, params);
    };

    TradeService.prototype.rendererMyWalletInfo = function () {
        this.walletProvider.getMyWalletInfos(function(result) {
            if (result.code == 0000) {
                for(var index in result.data.infos) {
                    var info = result.data.infos[index];
                    var baseCoin = $('#baseCoin').val();
                    var selectedCoin = $('#selectionCoin').val();

                    if (info.coin.name == baseCoin) {
                        $("#baseWalletAvailableBalanceFromBuy").val(utils.decimalWithCommas(info.wallet.availableBalance));
                        $("#baseWalletAvailableBalanceDisplayFromBuy").html(utils.decimalWithCommas(info.wallet.availableBalance));
                        $("#baseWalletAvailableBalanceFromSell").val(utils.decimalWithCommas(info.wallet.availableBalance));
                    } else if (info.coin.name == selectedCoin) {
                        $("#walletAvailableBalanceFromSell").val(utils.decimalWithCommas(info.wallet.availableBalance));
                        $("#walletAvailableBalanceDisplayFromSell").html(utils.decimalWithCommas(info.wallet.availableBalance));
                        $("#walletAvailableBalanceFromBuy").val(utils.decimalWithCommas(info.wallet.availableBalance));
                    }
                }
            } else {
                utils.errorAlert(utils.locale_i18n('dashboard.trade.load.fail.js'), result.msg, null);
            }
        }, null);
    };

    TradeService.prototype.rendererHogaOrders = function (selectionCoin, baseCoin, pageSize) {
        var params = new Object();
        params.fromCoin = selectionCoin;
        params.toCoin = baseCoin;
        params.sellPageNo = 0;
        params.sellPageSize = pageSize;
        params.buyPageNo = 0;
        params.buyPageSize = pageSize;

        this.tradeProvider.getHogas(function(result) {
            if (result.code == 0000) {
                if (result.data.buy.length > 0) {
                    //buy
                    var content1 = "";
                    for (var index in result.data.buy) {
                        var row = result.data.buy[index];
                        var num = index;
                        var color = "#CC0000";
                        var id = "buyRow"+index;
                        content1 += "<tr id='" + id + "' onclick=\"javascript:tradeService.selectHogaOrder('sell', " + row.price + ")\">";
                        content1 += "<td width='20%'></td>";
                        content1 += "<td width='30%' style='color:" + color + ";text-align:left;'><span>" + parseFloat(row.price).toFixed(8) + "</span></td>";
                        content1 += "<td width='20%'></td>";
                        content1 += "<td width='30%' style='color:" + color + ";text-align:left;'><span>" + row.amount + "</span></td>";
                        content1 += "</tr>";
                    }

                    $("#buyHogaBodyId").html(content1);
                } else {
                    $("#buyHogaBodyId").html("");
                }

                if (result.data.sell.length > 0) {
                    //sell
                    var content2 = "";
                    for (var index in result.data.sell) {
                        var row = result.data.sell[index];
                        var num = index;
                        var color = "#0033FF";
                        var id = "sellRow"+index;
                        content2 += "<tr id='" + id + "'onclick=\"javascript:tradeService.selectHogaOrder('buy', " + row.price + ")\">";
                        content2 += "<td width='20%'></td>";
                        content2 += "<td width='30%' style='color:" + color + ";text-align:left;'><span>" + parseFloat(row.price).toFixed(8) + "</span></td>";
                        content2 += "<td width='20%'></td>";
                        content2 += "<td width='30%' style='color:" + color + ";text-align:left;'><span>" + row.amount + "</span></td>";
                        content2 += "</tr>";
                    }

                    $("#sellHogaBodyId").html(content2);
                } else {
                    $("#sellHogaBodyId").html("");
                }
            } else {
                utils.errorAlert(utils.locale_i18n('dashboard.trade.load.fail.js'), result.msg, null);
            }
        }, params);
    };

    TradeService.prototype.rendererMarketHistoryOrders = function (selectionCoin, baseCoin, pageSize) {
        var params = new Object();
        params.fromCoin = selectionCoin;
        params.toCoin = baseCoin;
        params.pageNo = 0;
        params.pageSize = pageSize;

        this.tradeProvider.getMarketHistoryOrders(function (result) {
            if (result.code == 0000) {
                if (result.data.historyOrders.length == 0) {return;}

                var content = "";
                for (var index in result.data.historyOrders) {
                    var row = result.data.historyOrders[index];
                    var num = index;
                    var orderType = "";
                    var color = 'black';
                    if (row.orderType == "BUY") {
                        color = '#CC0000';
                        orderType = utils.locale_i18n('dashboard.trade.buy.title.js');
                    } else if (row.orderType == "SELL") {
                        color = '#0033FF';
                        orderType = utils.locale_i18n('dashboard.trade.sell.title.js');
                    }

                    content += "<tr>";
                    content += "<td style='color:" + color + "'>" + row.completedDtm + "</td>";
                    content += "<td style='color:" + color + "'>" + orderType + "</td>";
                    content += "<td style='color:" + color + "'>" + utils.decimalWithCommas(row.price) + "</td>";
                    content += "<td style='color:" + color + "'>" + utils.decimalWithCommas(row.amount) + "</td>";
                    content += "</tr>";
                }

                $("#marketHistoryBodyId").html(content);
            } else {
                utils.errorAlert(utils.locale_i18n('dashboard.trade.load.fail.js'), result.msg, null);
            }
        }, params);
    };

    TradeService.prototype.rendererMyHistoryOrders = function (selectionCoin, baseCoin, pageNo, pageSize) {
        var params = new Object();
        params.fromCoin = selectionCoin;
        params.toCoin = baseCoin;
        params.pageNo = pageNo;
        params.pageSize = pageSize;

        this.tradeProvider.getMyHistoryOrders(function (result) {
            if (result.code == 0000) {

                var content = "<tr><td>"+utils.locale_i18n('dashboard.trade.none.trade.history.js')+"<td></tr>";
                if (result.data.historyOrders.length > 0) {
                    content = "";
                    for (var index in result.data.historyOrders) {
                        var row = result.data.historyOrders[index];
                        var num = index;
                        var orderType = "";
                        var coin = "";
                        var color = 'black';
                        if (row.orderType == "BUY") {
                            color = '#CC0000';
                            orderType = utils.locale_i18n('dashboard.trade.buy.title.js');
                            coin = row.toCoin.name;
                        } else if (row.orderType == "SELL") {
                            color = '#0033FF';
                            orderType = utils.locale_i18n('dashboard.trade.sell.title.js');
                            coin = row.fromCoin.name;
                        }

                        content += "<tr>";
                        content += "<td style='color:" + color + "'>" + row.completedDtm + "</td>";
                        content += "<td style='color:" + color + "'>" + row.orderType + "</td>";
                        content += "<td style='color:" + color + "'>" + utils.decimalWithCommas(row.price) + "</td>";
                        content += "<td style='color:" + color + "'>" + utils.decimalWithCommas(row.amount) + "</td>";
                        content += "<td style='color:" + color + "'>" + row.toCoin.name + "</td>";
                        content += "<td style='color:" + color + "'>" + utils.decimalWithCommas(row.tradeBalance) + "</td>";
                        content += "<td style='color:" + color + "'>" + row.fromCoin.name + "</td>";
                        content += "<td style='color:" + color + "'>" + utils.decimalWithCommas(row.usingBalance) + "</td>";
                        content += "<td style='color:" + color + "'>" + utils.decimalWithCommas(row.feeBalance) + "</td>";
                        content += "</tr>";
                    }
                }
                $("#myHistoryBodyId").html(content);
            } else {
                utils.errorAlert(utils.locale_i18n('dashboard.trade.load.fail.js'), result.msg, null);
            }
        }, params);
    };

    TradeService.prototype.rendererMyOrders = function (selectionCoin, baseCoin, pageNo) {
        var params = new Object();
        params.fromCoin = selectionCoin;
        params.toCoin = baseCoin;
        params.pageNo = pageNo;
        params.pageSize = 10;

        this.tradeProvider.getMyOrders(function (result) {
            if (result.code == 0000) {
                //buy
                var content = "<tr><td>"+utils.locale_i18n('dashboard.trade.none.order.js')+"<td></tr>";
                if (result.data.orders.length > 0) {
                    content = "";
                    for (var index in result.data.orders) {

                        var row = result.data.orders[index];
                        var num = index;
                        var orderType = "";
                        var coin = "";
                        var color = 'black';
                        if (row.orderType == "BUY") {
                            color = '#CC0000';
                            orderType = utils.locale_i18n('dashboard.trade.buy.title.js');
                            coin = row.toCoin.name;
                        } else if (row.orderType == "SELL") {
                            color = '#0033FF';
                            orderType = utils.locale_i18n('dashboard.trade.sell.title.js');
                            coin = row.fromCoin.name;
                        }

                        content += "<tr>";
                        content += "<td style='color:" + color + "'>" + row.regDtm + "</td>";
                        content += "<td style='color:" + color + "'>" + coin + "</td>";
                        content += "<td style='color:" + color + "'>" + orderType + "</td>";
                        content += "<td style='color:" + color + "'>" + utils.decimalWithCommas(row.price) + "</td>";
                        content += "<td style='color:" + color + "'>" + utils.decimalWithCommas(row.amountRemaining) + " (" + utils.decimalWithCommas(row.amount) + ")" + "</td>";
                        content += "<td>" + '<button type="button" class="btn btn-danger waves-effect waves-light w-md btn-xs m-b-1" onclick="javascript:cancelOrder(' + row.id + ')">' + utils.locale_i18n('trade.div9.column6') + '</button>' + "</td>";
                        content += "</tr>";
                    }
                }

                $("#myOrderBodyId").html(content);
            } else {
                utils.errorAlert(utils.locale_i18n('dashboard.trade.load.fail.js'), result.msg, null);
            }
        }, params);
    };

    TradeService.prototype.cancelOrder = function (orderId) {
        var params = new Object();
        params.orderId = orderId;

        this.tradeProvider.cancel(function(result) {
            if (result.code == 0000) {
                utils.successAlert(utils.locale_i18n('dashboard.trade.cancel.order.js'), result.msg, function () {
                    init();
                });
            } else {
                utils.errorAlert(utils.locale_i18n('dashboard.trade.cancel.order.fail.js'), result.msg, function () {

                });
            }
        }, params);
    };

    TradeService.prototype.rendererCategoriedCurrentCoinInfos = function (baseCoin) {
        var params = new Object();
        params.coin = baseCoin;

        this.commonProvider.getCategoriedCoinAvgPrices(function (result) {
            var content = "";
            for (var index in result.data) {
                var row = result.data[index];
                var num = index;
                var orderType = "";
                var color = 'gray';
                if (row.marker == "+") {
                    color = '#CC0000';
                } else if (row.marker == "-") {
                    color = '#0033FF';
                }

                var unit = row.baseCoin.unit;

                //coinInfo
                content += "<tr>";
                content += "<td class='fs-08 text-vl-center'><a href=\"/trade?baseCoin=" + baseCoin + "&selectionCoin=" + row.coinInfo.name + "\">" + row.coinInfo.unit + "</a></td>";
                content += "<td class='fs-08 text-vl-center'>" + utils.decimalWithCommas(row.price) + "</td>";
                if(row.marker == '+') {
                    content += "<td class='fs-08 text-vl-center'><font color='green'>" + row.marker + row.changePercent + "% </font>" + "</td>";
                } else {
                    content += "<td class='fs-08 text-vl-center'><!--" + row.marker + row.gapPrice + " " + unit + "--><font color='red'>" + row.marker + row.changePercent + "% </font>" + "</td>";
                }
                content += "<td class='fs-08 text-vl-center'>" + utils.decimalWithCommas(row.totalTradeAmount24h) + "</td>";
                content += "</tr>";
            }

            $("#currentCoinBodyId").html(content);
        }, params);
    };

    TradeService.prototype.reqOrder = function (type) {
        var formData = formToObj(document.getElementById(type + 'FormId'));
        if (type == 'buy') {
            if (($("#" + type + "Price").val() == null) || ($("#" + type + "Price").val() == "")  ||  ($("#" + type + "Price").val() == 0)) {
                utils.errorAlert(utils.locale_i18n('dashboard.trade.price.zero.js'), null, null);
                return;
            } else if (($("#" + type + "Amount").val() == null) || ($("#" + type + "Amount").val() == "")  ||  ($("#" + type + "Amount").val() == 0)) {
                utils.errorAlert(utils.locale_i18n('dashboard.trade.amount.zero.js'), null, null);
                return;
            } else if (parseFloat($("#" + type + "Balance").html()) > parseFloat($("#baseWalletAvailableBalanceFromBuy").val())) {
                // 매수시 판매금액이 사용가능금액보다 작은경우
                utils.errorAlert(utils.locale_i18n('dashboard.trade.insufficient.js'), null, null);
                return;
            } else {
                this.tradeProvider.buy(function (result) {
                    if (result.code == 0000) {
                        var msg = "";
                        if (result.data.tradedOrders != null) {
                            for(var index in result.data.tradedOrders) {
                                var tradedOrder = result.data.tradedOrders[index];
                                msg += utils.locale_i18n('dashboard.trade.symbol.quantity.js') + tradedOrder.amount + ","+ utils.locale_i18n('dashboard.trade.symbol.price.js') + tradedOrder.price + utils.locale_i18n('dashboard.trade.buy.completion.js') + "\n";
                            }
                        } else {
                            msg = result.msg;
                        }

                        utils.successAlert(utils.locale_i18n('dashboard.trade.buy.success.js'), msg, function () {
                            init();
                        });
                    } else {
                        utils.errorAlert(utils.locale_i18n('dashboard.trade.buy.fail.js'), result.msg, null);
                    }
                }, formData);
            }
        } else if (type == 'sell') {
            if (($("#" + type + "Price").val() == null) || ($("#" + type + "Price").val() == "")  ||  ($("#" + type + "Price").val() == 0)) {
                utils.errorAlert(utils.locale_i18n('dashboard.trade.price.zero.js'), null, null);
                return;
            } else if (($("#" + type + "Amount").val() == null) || ($("#" + type + "Amount").val() == "")  ||  ($("#" + type + "Amount").val() == 0)) {
                utils.errorAlert(utils.locale_i18n('dashboard.trade.amount.zero.js'), null, null);
                return;
            } else if(parseFloat($("#" + type + "Balance").html()) > parseFloat($("#walletAvailableBalanceFromSell").val())) {
                // 매도시 판매금액이 사용금액보다 작은경우
                utils.errorAlert(utils.locale_i18n('dashboard.trade.insufficient.js'), null, null);
                return;
            } else {
                this.tradeProvider.sell(function(result) {
                    if (result.code == 0000) {
                        var msg = "";
                        if (result.data.tradedOrders != null) {
                            for(var index in result.data.tradedOrders) {
                                var tradedOrder = result.data.tradedOrders[index];
                                msg += utils.locale_i18n('dashboard.trade.symbol.quantity.js') + tradedOrder.amount + ", "+ utils.locale_i18n('dashboard.trade.symbol.price.js') + tradedOrder.price + tradedOrder.price + " " +utils.locale_i18n('dashboard.trade.sell.completion.js') + "\n";
                            }
                        } else {
                            msg = result.msg;
                        }

                        utils.successAlert(utils.locale_i18n('dashboard.trade.sell.request.success.js'), msg, function () {
                            init();
                        });
                    } else {
                        utils.errorAlert(utils.locale_i18n('dashboard.trade.sell.request.fail.js'), result.msg, null);
                    }
                }, formData);
            }
        }
    };

    return TradeService;
}());