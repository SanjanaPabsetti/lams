        var gradientNumberFormat = function (cellvalue) {
        		var MIN_DATA_VALUE = 0,
        			MAX_DATA_VALUE = 10;
        		
            var dataAsNumber = parseInt(cellvalue, 10);
            if (dataAsNumber == -1) {
            		return "";
            }
            if (dataAsNumber > MAX_DATA_VALUE) {
                dataAsNumber = MAX_DATA_VALUE;
            }
            if (dataAsNumber < MIN_DATA_VALUE) {
                dataAsNumber = MIN_DATA_VALUE;
            }
            var procents = (dataAsNumber - MIN_DATA_VALUE) * 100 / (MAX_DATA_VALUE - MIN_DATA_VALUE);
            var gradientClass;
            switch (true) {
                case (procents < 40):
                		gradientClass = "gradient-red";
                    break;
                case (procents >= 40 && procents <= 60):
                		gradientClass = "gradient-orange";
                    break;
                default:
                		gradientClass = "gradient-green";
                    break;
            }
            
            return '<div class="filled-bar"><div class="gradient ' + gradientClass + '" style="width:' +
                    procents + '%;"></div><div class="filled-bar-text">' +
                    $.fmatter.util.NumberFormat(cellvalue, $.jgrid.formatter["integer"]) +
                    '</div></div>';
        };