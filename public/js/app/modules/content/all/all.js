angular.module('lvr.content.all', [])
    .controller('ContentAllController', function($scope, content, folders, books, bookViewer) {
        content.replaceBooks(books.fetchAll());
    });
