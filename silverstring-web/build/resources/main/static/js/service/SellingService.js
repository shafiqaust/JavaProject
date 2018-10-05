var SellingService = /** @class */ (function () {
    function SellingService() {
        this.commonProvider = new CommonProvider();
        this.sellingProvider = new SellingProvider();
    }

    SellingService.prototype.determineBalance = function () {
        // price 입력값 체크
        var payAmount = $("#payAmount").val();
        if (payAmount == "" || payAmount.indexOf("-") >= 0 || isNaN(payAmount) == true) {
            $("#payAmount").val(0);
            $("#payAmountPop").html(0);
            return;
        } else {
            var dotLen = payAmount.lastIndexOf(".") >= 0 ?  payAmount.substring(payAmount.lastIndexOf(".")).length - 1 : 0;
            if(dotLen > 8) {
                $("#payAmount").val(parseFloat(payAmount).toFixed(8));
                $("#payAmountPop").html(parseFloat(payAmount).toFixed(8));
            } else {
                $("#payAmount").val(payAmount);
                $("#payAmountPop").html(payAmount);
            }
        }
    };

    SellingService.prototype.determine = function () {
        this.determineBalance();

        var payAmount = parseFloat($("#payAmount").val());
        var perCent = parseFloat($("#perCent").html());

        buyTotalAmount = (payAmount / perCent).toFixed(8)
        $("#buyTotalAmount").text(buyTotalAmount);

        buyTotalAmount = (payAmount / perCent).toFixed(8)
        $("#buyTotalAmountPop").text(buyTotalAmount);

        console.log(">>>>>> buyTotalAmount " + buyTotalAmount);

        return buyTotalAmount;
    };

    SellingService.prototype.reqSelling = function (buyFlag) {
        var formData = formToObj(document.getElementById('myInfoFormId'));
        var payAmount = parseFloat($("#payAmount").val());
        var perCent = parseFloat($("#perCent").html());
        var onceMinAmount = parseFloat($("#onceMinAmount").html());
        var availableBalance = parseFloat($("#availableBalance").html());

        if ((payAmount == null) || (payAmount == "")  ||  (payAmount == 0) || isNaN(payAmount) == true) {
            utils.errorAlert(utils.locale_i18n('dashboard.coin_selling.amount.zero.js'), null, null);
            return false;
        } else if ((payAmount / perCent).toFixed(8) < onceMinAmount) {
            utils.errorAlert(utils.locale_i18n('dashboard.coin_selling.amount.min.js'), null, null);
            return false;
        } else if (payAmount.toFixed(8) > availableBalance) {
            utils.errorAlert(utils.locale_i18n('dashboard.coin_selling.amount.available.js'), null, null);
            return false;
        } else {
            if(buyFlag) {
                this.sellingProvider.getCoinSelling(function (result) {
                    if (result.code == 0000) {
                        utils.successAlert(utils.locale_i18n('dashboard.coin_selling.success.js'), result.msg, function () {
                            //init();
                        });
                    } else {
                        utils.errorAlert(utils.locale_i18n('dashboard.coin_selling.fail.js'), result.msg, null);
                    }
                }, formData);
            }

            return true;
        }
    };

    return SellingService;
}());