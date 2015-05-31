angular.module('lvr.content', [])
    .controller('lvrContentCtrl', function ($scope, Book, Folder, folders, books) {
        $scope.showDropdown = false;
        $scope.showAsMosaic = true;

        $scope.currentFolder = folders.getCurrentFolder();
        $scope.folders = [];
        //$scope.getFolders = folders.getCurrentSubFolders();

        $scope.getBooks =  books.currentBooks;

        $scope.createNewFolder = function () {
            $scope.folders.unshift(new Folder({
                label: folders.getNewFolderNameInCurrentFolder()
            }));

            folders.saveRootFolder();
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
