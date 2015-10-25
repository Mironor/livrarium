angular.module('lvr')
    .factory('books', function ($http, constants, folders, Book) {
        return {
            addToContent: function(bookData) {
                folders.currentFolder.contents.books.unshift(new Book(bookData));
            },

            fetchAll: function() {
                var result = [];

                $http.get(constants.api.allBooks)
                    .success(function(data) {
                        result = _.map(data, function(bookData){
                            return new Book(bookData);
                        });
                    }.bind(this));

                return result;
            }
        }
    });
