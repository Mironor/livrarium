angular.module('lvr')
    .factory('books', function ($http, constants, folders, Book) {
        return {
            /**
             * Appends supplied book to current folder's content
             * @param bookData
             */
            addToContent: function(bookData) {
                folders.currentFolder.contents.books.unshift(new Book(bookData));
            },

            /**
             * Fetches all current user's books
             * Returns array that is initially empty and filled when the request is completed,
             * this is done so that angular could keep link to the array
             * @returns {Array}
             */
            fetchAll: function() {
                var result = [];

                $http.get(constants.api.allBooks)
                    .success(function(data) {
                        result.push.apply(result, _.map(data, function(bookData){
                            return new Book(bookData);
                        }));
                        console.log(result);
                    });

                return result;
            }
        }
    });
