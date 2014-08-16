angular.module('lvr')
    .factory('Book', function (constants) {

        var bookModel = {

            id: '',
            name: '',
            createDate: 0,
            formats: [],
            pages: 0,
            currentPage: 0,

            initProgressBars: function () {
                this.progressBars = [];

                var barsCount, currentProgress;

                if (this.pages > 0) {
                    barsCount = Math.ceil(this.pages / constants.books.minPagesPerBar);
                    barsCount = Math.min(barsCount, constants.books.maxBarsCount);
                    currentProgress = this.currentPage / this.pages;
                } else {
                    barsCount = 1;
                    currentProgress = 1;
                }

                var progressPerBar = 1 / barsCount;

                for (var i = 0; i < barsCount; i++) {
                    this.progressBars.push(this._generateProgressBar(i, currentProgress, progressPerBar));
                }

                return this;
            },

            _generateProgressBar: function (barIndex, currentProgress, progressPerBar) {
                var barProgressStart = barIndex * progressPerBar,
                    barProgressEnd = (barIndex + 1) * progressPerBar,
                    barCompleteness;

                if (barProgressEnd < currentProgress) {
                    // to the left of the partial bar
                    barCompleteness = 100;
                } else if (barProgressStart > currentProgress) {
                    // to the right of the partial bar
                    barCompleteness = 0;
                } else {
                    // partial bar
                    barCompleteness = ((currentProgress - barProgressStart) / progressPerBar) * 100;
                }

                barCompleteness = Math.round(barCompleteness);

                return {
                    completeness: barCompleteness
                }
            }
        };

        var Book = function (data) {
            angular.extend(this, data);
        };

        angular.extend(Book.prototype, bookModel);

        return Book;
    });
