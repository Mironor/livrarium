angular.module('lvr')
    .factory('book', function (constants) {
        return {
            initProgressBars: function () {
                this.progressBars = [];

                var barsCount = Math.floor(this.pages / constants.minPagesPerBar),
                    currentProgress = this.currentPage / this.pages,
                    progressPerBar = 1 / barsCount;

                for (var i = 0; i < barsCount; i++) {
                    var barProgressStart = i * progressPerBar,
                        barProgressEnd = (i + 1) * progressPerBar,
                        barCompleteness;

                    if (barProgressEnd < currentProgress) {
                        barCompleteness = 100;
                    } else if (barProgressStart > currentProgress) {
                        barCompleteness = 0;
                    } else {
                        barCompleteness = ((currentProgress - barProgressStart) / progressPerBar) * 100;
                    }

                    barCompleteness = Math.round(barCompleteness);

                    this.progressBars.push({
                        completeness: barCompleteness
                    })
                }

                return this;
            }
        }
    });
