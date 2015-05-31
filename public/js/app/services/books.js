angular.module('lvr')
    .factory('books', function (Book) {
        return {
            // This public field may only be used by controllers that need to be binded directly to the array
            // No one should directly manipulate it (use helper methods instead)
            currentBooks: [],

            setBooks: function(newBooks) {
                this.currentBooks = newBooks;
            },

            getBooks: function() {
                return this.currentBooks;
            },

            appendToContent: function(bookData) {
                this.currentBooks.push(new Book(bookData));
            }
        }
    });
