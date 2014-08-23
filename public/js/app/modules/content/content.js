angular.module('lvr.content', [])
    .controller('lvrContentCtrl', function ($scope, Book, Folder, folders) {
        $scope.showDropdown = false;
        $scope.showAsMosaic = true;

        $scope.folders = folders.getCurrentFolder().children;

        console.log($scope.folders);

        var books = [
            new Book({
                id: 'mosaic_thumb_algos',
                name: 'Oreilly Data Structures and Algorithms with Javascript',
                createDate: 1407608313000,
                formats: ['pdf', 'epub'],
                pages: 501,
                currentPage: 55
            }),
            new Book({
                id: 'mosaic_thumb_angular',
                name: 'Mastering Web Application Development with Angular JS',
                createDate: 1407608313000,
                formats: ['pdf'],
                pages: 159,
                currentPage: 89
            }),
            new Book({
                id: 'mosaic_thumb_tdd',
                name: 'Jasmine JavaScript Testing',
                createDate: 1407608313000,
                formats: ['pdf'],
                pages: 329,
                currentPage: 270
            })
        ];

        $scope.books = _.invoke(books, 'initProgressBars');

        $scope.createNewFolder = function () {
            $scope.folders.unshift(new Folder({
                label: "new folder"
            }))
        }
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
