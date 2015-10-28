describe('Book', function () {
    var constants;
    var book, zeroPagesBook, manyPagesBook;

    beforeEach(module('lvr'));

    beforeEach(inject(function (_constants_, Book) {
        constants = _constants_;

        book = new Book({
            identifier: 'uuid-1',
            name: 'Book one',
            createDate: 1407608313000,
            format: 'pdf',
            pages: 160,
            currentPage: 100
        });

        zeroPagesBook = new Book({
            pages: 0
        });

        manyPagesBook = new Book({
            pages: 1000
        })
    }));
});
