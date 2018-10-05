var DashboardService = /** @class */ (function () {
    function DashboardService() {
        this.rendererCategoriedCurrentCoinInfosIntervalId = null;
        this.commonProvider = new CommonProvider();
    }

    DashboardService.prototype.rendererCategoriedCurrentCoinInfos = function (baseCoin) {
        $("#baseCoin").val(baseCoin);
        $("#selectedGraphSpanId").html(baseCoin);

        $("a[id*='TabId']").each(function (i, el) {
            el.setAttribute("class", "nav-link rd-0 py-10px px-20px fs-08 brd-none text-color");
        });
        $('#' + baseCoin + 'TabId').attr("class", "nav-link rd-0 py-10px px-20px fs-08 brd-none text-color active show");

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
                    color = 'indianred';
                } else if (row.marker == "-") {
                    color = '#00b19d';
                }

                var unit = row.baseCoin.unit;

                content += "<tr>";
                content += "<td><img style=\"width:24px;height:24px;\" src=\"" + row.coinInfo.logoUrl + "\"/></td>";
                content += "<td style='color:" + color + "'><a href=\"#\" onclick=\"onGraphClick(\'" + row.baseCoin.name + "\', \'" + row.coinInfo.name + "\', \'" + row.baseCoin.unit + "\', \'" +  row.coinInfo.unit + "\')\">  " + row.coinInfo.unit + "</a></td>";
                content += "<td style='color:" + color + "'>" + utils.decimalWithCommas(row.price) + "</td>";
                if(row.marker == "+") {
                    content += "<td style='color:green'> " + row.marker + row.changePercent + "% </td>";
                } else {
                    content += "<td style='color:red'> " + row.marker + row.changePercent + "% </td>";
                }
                content += "<td>" + utils.decimalWithCommas(row.totalTradeAmount24h) + "</td>";
                content += "<td><a class=\"deal-btn text-white fs-08 py-5px\" href=\"/trade?baseCoin=" + row.baseCoin.name + "&selectionCoin=" + row.coinInfo.name + "\">" + utils.locale_i18n('dashboard.home.graph.trade.btn') + "</a></td>";
                //content += "<td><a class=\"deal-btn text-white fs-08 py-5px\" href=\"#\">" + utils.locale_i18n('dashboard.home.graph.trade.btn') + "</a></td>";
                content += "</tr>";
            }

            $("#currentCoinBodyId").html(content);
        }, params);
    };

    return DashboardService;
}());