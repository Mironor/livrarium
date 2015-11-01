angular.module('lvr.content.all', [])
    .controller('ContentAllController', function($scope, folders, books, bookViewer) {
        $scope.currentFolder = folders.currentFolder;

        folders.fetchCurrentFolderContents();

        $scope.books = books.fetchAll();

        $scope.openBook = function(book) {
            bookViewer.openedBook.model = book;
            bookViewer.show = true;
        };
    });
