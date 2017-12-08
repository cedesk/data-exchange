/*
 * Copyright 2017 Skolkovo Institute of Science and Technology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

var randomScalingFactor = function () {
    return Math.round(Math.random() * 100);
};


var config = {type: 'radar'};
config.options = {
    legend: {position: 'bottom'},
    scale: {
        ticks: {beginAtZero: true}
    }
};

config.data = {};
config.data.datasets = [];
config.data.datasets[0] = {
    label: "2014",
    backgroundColor: Chart.helpers.color(window.chartColors.red).alpha(0.2).rgbString(),
    borderColor: window.chartColors.red,
    pointBackgroundColor: window.chartColors.red,
    data: [
        randomScalingFactor(),
        randomScalingFactor(),
        randomScalingFactor(),
        randomScalingFactor(),
        randomScalingFactor(),
        randomScalingFactor(),
        randomScalingFactor()
    ]
};
config.data.datasets[1] = {
    label: "2015",
    backgroundColor: Chart.helpers.color(window.chartColors.blue).alpha(0.2).rgbString(),
    borderColor: window.chartColors.blue,
    pointBackgroundColor: window.chartColors.blue,
    data: [
        randomScalingFactor(),
        randomScalingFactor(),
        randomScalingFactor(),
        randomScalingFactor(),
        randomScalingFactor(),
        randomScalingFactor(),
        randomScalingFactor()
    ]
};

config.data.labels = ["FigureOfMerit1", "FigureOfMerit2", "FigureOfMerit3", "FigureOfMerit4", "FigureOfMerit5", "FigureOfMerit6", "FigureOfMerit7"];

var tradespaceRadar = new Chart(document.getElementById("canvas"), config);

var updateTradespaceRadar = function () {
    config.data.labels = labels;
    config.data.datasets = datasets.map(function (item) {
        return {
            label: item
        }
    });
    config.data.datasets.forEach(function (item, index) {
        var color = window.chartColors[Object.keys(window.chartColors)[index]];
        item.backgroundColor = Chart.helpers.color(color).alpha(0.2).rgbString();
        item.borderColor = color;
        item.pointBackgroundColor = color;
        item.data = data.get(index);
    });
    tradespaceRadar.update()
};
