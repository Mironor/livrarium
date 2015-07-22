angular.module('lvr')
    .factory('books', function (folders, Book) {
        return {
            addToContent: function(bookData) {
                folders.currentFolder.contents.books.unshift(new Book(bookData));
            }
        }
    });
