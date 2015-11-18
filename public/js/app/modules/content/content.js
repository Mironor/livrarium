angular.module('lvr.content', [
    'lvr.content.all'
]).factory('content', function($http, constants, i18nEn, Folder, Book) {
    var ContentService = {
        books: [],

        addBook: function(book) {

        },

        addBooks: function(books) {

        },

        replaceBooks: function(books) {
            this.books = books;
        },

        folders: []
    };

    return ContentService;
}).directive('lvrContent', function(constants) {
    return {
        restrict: 'E',
        templateUrl: constants.pathToApp + 'modules/content/content.html',
        controller: function($scope, content, bookViewer, identity) {

            $scope.idUser = identity.id;
            $scope.books = content.books;

            $scope.openBook = function(book) {
                bookViewer.openedBook.model = book;
                bookViewer.show = true;
            };
        }
    }
});
