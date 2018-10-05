var SimpleBaseProvider = /** @class */ (function () {
    function SimpleBaseProvider() {
    }

    SimpleBaseProvider.prototype.postAsync = function (uri, callback, resultProcFunc, params) {
        $.ajax({
            url: uri
            , type: "POST"
            , dataType: 'json'
            , async: false
            , contentType:"application/json; charset=UTF-8"
            , data: JSON.stringify(params)
            , success: function (result) {
                resultProcFunc(result.data);
                return callback(result);
            }
            , error:function(e){
                var data = new Object();
                data.code = 1000;
                data.msg = JSON.parse(e.responseText).message;
                return callback(data);
            }
        });
        return true;
    };

    SimpleBaseProvider.prototype.post = function (uri, callback, resultProcFunc, params) {
        $.ajax({
            url: uri
            , type: "POST"
            , dataType: 'json'
            , contentType:"application/json; charset=UTF-8"
            , data: JSON.stringify(params)
            , success: function (result) {
                resultProcFunc(result.data);
                return callback(result);
            }
            , error:function(e){
                var data = new Object();
                data.code = 1000;
                data.msg = JSON.parse(e.responseText).message;
                return callback(data);
            }
        });
        return true;
    };

    SimpleBaseProvider.prototype.get = function (uri, callback, resultProcFunc, params) {
        $.ajax({
            url: uri
            , type: "GET"
            , dataType: 'json'
            , contentType:"application/json; charset=UTF-8"
            , data: JSON.stringify(params)
            , success: function (result) {
                resultProcFunc(result.data);
                return callback(result);
            }
            , error:function(e){
                var data = new Object();
                data.code = 1000;
                data.msg = JSON.parse(e.responseText).message;
                return callback(data);
            }
        });
        return true;
    };

    return SimpleBaseProvider;
}());