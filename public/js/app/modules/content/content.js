angular.module('lvr.content', [])
    .controller('lvrContentCtrl', function ($scope, Book, Folder, folders, books) {

        this.test = "test";

        $scope.showDropdown = false;
        $scope.showAsMosaic = true;

        $scope.currentFolder = folders.currentFolder;

        folders.fetchFolderContents($scope.currentFolder.id);

        $scope.createNewFolder = function () {
            var newFolder =  new Folder({
                name: folders.getNewFolderNameInCurrentFolder()
            });

            $scope.currentFolder.contents.folders.unshift(newFolder);

            folders.createFolder($scope.currentFolder.id, newFolder.name)
                //.success(function(data) {
                //    newFolder.id = data.id
                //})
        };
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
