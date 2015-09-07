angular.module('lvr.content', [])
    .controller('lvrContentCtrl', function($scope, Book, Folder, folders, bookViewer) {

        $scope.showDropdown = false;
        $scope.showAsMosaic = ($scope.showAsMosaic === undefined) ? true : $scope.showAsMosaic;

        $scope.currentFolder = folders.currentFolder;

        folders.fetchCurrentFolderContents();

        $scope.createNewFolder = function() {
            var newFolder = new Folder({
                name: folders.getNewFolderNameInCurrentFolder()
            });

            $scope.currentFolder.contents.folders.push(newFolder);
            folders.rootFolder.children.push(newFolder);

            folders.createFolder($scope.currentFolder.id, newFolder.name);
        };

        $scope.openFolder = function(folder) {

        };

        $scope.openBook = function(book) {
            bookViewer.openedBook.model = book;
            bookViewer.show = true;
        };
    })
    .directive('lvrContent', function(constants) {
        return {
            restrict: 'E',
            templateUrl: constants.pathToApp + 'modules/content/content.html',
            controller: 'lvrContentCtrl'
        };
    })
    .directive('lvrContentMosaic', function(constants) {
        return {
            restrict: 'A',
            templateUrl: constants.pathToApp + 'modules/content/content-mosaic.html'
        }
    })
    .directive('lvrContentList', function(constants) {
        return {
            restrict: 'A',
            templateUrl: constants.pathToApp + 'modules/content/content-list.html'
        }
    })
    .directive('lvrBookProgressbar', function(constants) {
        return {
            restrict: 'E',
            scope: {
                bars: '='
            },
            templateUrl: constants.pathToApp + 'modules/content/book-progressbar.html'
        }
    });
