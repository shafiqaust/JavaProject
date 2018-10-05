var CommonService = /** @class */ (function () {
    function CommonService() {
        this.commonProvider = new CommonProvider();
    }

    CommonService.prototype.rendererTickers = function () {
        this.commonProvider.ticker(function(result) {
            var data = result.data;
            for (var index in data) {
                var row = data[index];
                $("#ticker" + index + "Id").text(row.name + " $" + utils.numberWithCommas(Math.round(row.price_usd)) + " " + " (" + (row.percent_change_24h >= 0 ? "+" + row.percent_change_24h : row.percent_change_24h)+ "%)");
            }
        }, null);
    };

    CommonService.prototype.getEthUsdPrice = function () {
        this.commonProvider.getEthUsdPrice(function(result) {
            var data = result.data;
            console.log(">>>>>>>>>>>data.usdPrice>>>>>>> " + data.usdPrice + ',' + data.perCent);
            $("#perCent").html(data.perCent);
            $("#perCentPop").html(data.perCent);
            $("#usdPrice").html(data.usdPrice);
            $("#usdPricePop").html(data.usdPrice);
            sellingService.determine();
        }, null);
    };

    CommonService.prototype.renderer24hGraphData = function (baseCoin, selectedCoin, baseCoinUnit, selectedCoinUnit) {
        $("#selectedGraphSpanId").text(baseCoin);
        $("#graphBaseCoin").html(baseCoinUnit);
        $("#graphSubCoin").html(selectedCoinUnit);

        var params = new Object();
        params.coin = selectedCoin;
        params.baseCoin = baseCoin;

        this.commonProvider.get24hGraphData(function (result) {
            var data = result.data;
            var chartObj = new Chartist.Line('#chart-with-area', {
                labels: data.dates,
                series: [
                    data.prices
                ]
            }, {
                height: 400,
                fullWidth: true,
                chartPadding: {
                    left: 50,
                    right: 20
                },
                axisX: {
                    showLabel:false,
                    showGrid: false
                }
            });

            $("#maxPriceId").text(utils.decimalWithCommas(data.maxPrice) + " " + data.baseCoin.unit);
            $("#minPriceId").text(utils.decimalWithCommas(data.minPrice) + " " + data.baseCoin.unit);
            $("#avgPriceId").text(utils.decimalWithCommas(data.avgPrice) + " " + data.baseCoin.unit);
        }, params);
    };

    return CommonService;
}());