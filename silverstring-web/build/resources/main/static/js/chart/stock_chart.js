var selectionCoin = $('#selectionCoin').val();
var baseCoin = $('#baseCoin').val();
var series;

Highcharts.theme = {
    colors: ['#2b908f', '#90ee7e', '#f45b5b', '#7798BF', '#aaeeee', '#ff0066', '#eeaaee',
        '#55BF3B', '#DF5353', '#7798BF', '#aaeeee'],
    chart: {
        backgroundColor: {
            linearGradient: { x1: 0, y1: 0, x2: 1, y2: 1 },
            stops: [
                [0, '#2a2a2b'],
                [1, '#3e3e40']
            ]
        },
        style: {
            fontFamily: '\'Unica One\', sans-serif'
        },
        plotBorderColor: '#606063'
    },
    title: {
        style: {
            color: '#E0E0E3',
            textTransform: 'uppercase',
            fontSize: '20px'
        }
    },
    subtitle: {
        style: {
            color: '#E0E0E3',
            textTransform: 'uppercase'
        }
    },
    xAxis: {
        gridLineColor: '#707073',
        labels: {
            style: {
                color: '#E0E0E3'
            }
        },
        lineColor: '#707073',
        minorGridLineColor: '#505053',
        tickColor: '#707073',
        title: {
            style: {
                color: '#A0A0A3'
            }
        }
    },
    yAxis: {
        allowDecimals: true,
        gridLineColor: '#707073',
        labels: {
            style: {
                color: '#E0E0E3'
            },
            formatter:
                function() {
                    return  utils.decimalWithCommas(this.value);
                }
        },
        lineColor: '#707073',
        minorGridLineColor: '#505053',
        tickColor: '#707073',
        tickWidth: 1,
        title: {
            style: {
                color: '#A0A0A3'
            }
        }
    },
    tooltip: {
        shared:true,
        backgroundColor: 'rgba(0, 0, 0, 0.85)',
        style: {
            color: '#F0F0F0'
        }
    },
    plotOptions: {
        series: {
            dataLabels: {
                color: '#B0B0B3'
            },
            marker: {
                lineColor: '#333'
            }
        },
        boxplot: {
            fillColor: '#505053'
        },
        candlestick: {
            lineColor: 'white'
        },
        errorbar: {
            color: 'white'
        }
    },
    legend: {
        itemStyle: {
            color: '#E0E0E3'
        },
        itemHoverStyle: {
            color: '#FFF'
        },
        itemHiddenStyle: {
            color: '#606063'
        }
    },
    credits: {
        style: {
            color: '#666'
        }
    },
    labels: {
        style: {
            color: '#707073'
        }
    },
    drilldown: {
        activeAxisLabelStyle: {
            color: '#F0F0F3'
        },
        activeDataLabelStyle: {
            color: '#F0F0F3'
        }
    },
    navigation: {
        buttonOptions: {
            symbolStroke: '#DDDDDD',
            theme: {
                fill: '#505053'
            }
        }
    },
    // scroll charts
    rangeSelector: {
        buttonTheme: {
            fill: '#505053',
            stroke: '#000000',
            style: {
                color: '#CCC'
            },
            states: {
                hover: {
                    fill: '#707073',
                    stroke: '#000000',
                    style: {
                        color: 'white'
                    }
                },
                select: {
                    fill: '#000003',
                    stroke: '#000000',
                    style: {
                        color: 'white'
                    }
                }
            }
        },
        inputBoxBorderColor: '#505053',
        inputStyle: {
            backgroundColor: '#333',
            color: 'silver'
        },
        labelStyle: {
            color: 'silver'
        }
    },

    navigator: {
        handles: {
            backgroundColor: '#666',
            borderColor: '#AAA'
        },
        outlineColor: '#CCC',
        maskFill: 'rgba(255,255,255,0.1)',
        series: {
            color: '#7798BF',
            lineColor: '#A6C7ED'
        },
        xAxis: {
            gridLineColor: '#505053'
        }
    },

    scrollbar: {
        barBackgroundColor: '#808083',
        barBorderColor: '#808083',
        buttonArrowColor: '#CCC',
        buttonBackgroundColor: '#606063',
        buttonBorderColor: '#606063',
        rifleColor: '#FFF',
        trackBackgroundColor: '#404043',
        trackBorderColor: '#404043'
    },

    // special colors for some of the
    legendBackgroundColor: 'rgba(0, 0, 0, 0.5)',
    background2: '#505053',
    dataLabelsColor: '#B0B0B3',
    textColor: '#C0C0C0',
    contrastTextColor: '#F0F0F3',
    maskColor: 'rgba(255,255,255,0.3)'
};

// Apply the theme
Highcharts.setOptions(Highcharts.theme);

Highcharts.setOptions({
    global: {
        useUTC: false
    }
});

function generateChartData() {
    $.ajax({
        url: "/api/chart/" + baseCoin + "/" + selectionCoin
        , type: "GET"
        , dataType: 'json'
        , contentType:"application/json; charset=UTF-8"
        , data: null
        , success: function (data) {
            /**
            console.log(data);
            console.log(">>>>>>>>> " + data.length);
            */
            var chartData = [];
            for (i = 0; i < data.length; i++) {
                chartData.push([
                    data[i][0],
                    parseFloat((Math.round(data[i][1] * 100000) /100000).toPrecision(8)),
                    parseFloat((Math.round(data[i][2] * 100000) /100000).toPrecision(8)),
                    parseFloat((Math.round(data[i][3] * 100000) /100000).toPrecision(8)),
                    parseFloat((Math.round(data[i][4] * 100000) /100000).toPrecision(8))
                ]);
            }

            Highcharts.stockChart('chart_container', {
                chart: {
                    events: {
                        load: function () {
                            series = this.series[0];
                        }
                    }
                },

                rangeSelector: {
                    buttons: [{
                        count: 5,
                        type: 'minute',
                        text: '5M'
                    }, {
                        count: 15,
                        type: 'minute',
                        text: '15M'
                    },{
                        count: 1,
                        type: 'hour',
                        text: '1H'
                    }, {
                        count: 24,
                        type: 'hour',
                        text: '1D'
                    },{
                        count: 24*7,
                        type: 'hour',
                        text: '1W'
                    },{
                        type: 'all',
                        text: 'All'
                    }],
                    inputEnabled: false,
                    selected: 3
                },
                title: {
                    text: ''
                },
                exporting: {
                    enabled: false
                },
                series: [{
                    name: selectionCoin,
                    type: 'candlestick',
                    data: chartData
                }]
            });
        }
        , error:function(e){

        }
    });
}
generateChartData();


function objToString (obj) {
    var str = '';
    for (var p in obj) {
        if (obj.hasOwnProperty(p)) {
            str += p + '::' + obj[p] + '\n';
        }
    }
    return str;
}


