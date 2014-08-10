angular.module('lvr.content', [])
    .controller('lvrContentCtrl', function ($scope, book) {
        $scope.showDropdown = false;
        $scope.showAsMosaic = true;

        $scope.folders = [];
        var books = [
            angular.extend({
                id: 'mosaic_thumb_algos',
                name: 'Oreilly Data Structures and Algorithms with Javascript',
                createDate: 1407608313000,
                formats: ['pdf', 'epub'],
                pages: 501,
                currentPage: 55
            }, book),
            angular.extend({
                id: 'mosaic_thumb_angular',
                name: 'Mastering Web Application Development with Angular JS',
                createDate: 1407608313000,
                formats: ['pdf'],
                pages: 159,
                currentPage: 89
            }, book),
            angular.extend({
                id: 'mosaic_thumb_tdd',
                name: 'Jasmine JavaScript Testing',
                createDate: 1407608313000,
                formats: ['pdf'],
                pages: 329,
                currentPage: 270
            }, book)
        ];

        $scope.books = _.invoke(books, 'initProgressBars');
    })
    .directive('lvrContent', function (constants) {
        return {
            restrict: 'E',
            templateUrl: constants.pathToApp + 'modules/content/content.html'
        };
    })
    .directive('lvrContentMosaic', function (constants) {
        return {
            restrict: 'A',
            templateUrl: constants.pathToApp + 'modules/content/content-mosaic.html'
        }
    })
    .directive('lvrContentList', function (constants) {
        return {
            restrict: 'A',
            templateUrl: constants.pathToApp + 'modules/content/content-list.html'
        }
    })
    .directive('lvrBookProgressbar', function (constants) {
        return {
            restrict: 'E',
            scope: {
                bars: '='
            },
            templateUrl: constants.pathToApp + 'modules/content/book-progressbar.html'
        }
    });
