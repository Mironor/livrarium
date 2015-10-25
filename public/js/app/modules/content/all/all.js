angular.module('lvr.content.all', [])
    .controller('ContentAllController', function($scope, folders, bookViewer) {
        $scope.currentFolder = folders.currentFolder;

        folders.fetchCurrentFolderContents();

        $scope.openBook = function(book) {
            bookViewer.openedBook.model = book;
            bookViewer.show = true;
        };
    });
