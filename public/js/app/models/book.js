angular.module('lvr')
    .factory('Book', function (constants) {

        var bookModel = {

            identifier: undefined,
            name: "",
            pages: 0,
            currentPage: 0

        };

        var Book = function (data) {
            angular.extend(this, data);
        };

        angular.extend(Book.prototype, bookModel);

        return Book;
    });
