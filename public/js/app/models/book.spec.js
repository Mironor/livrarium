describe('Book', function () {
    var constants;
    var book, zeroPagesBook, manyPagesBook;

    beforeEach(module('lvr'));

    beforeEach(inject(function (_constants_, Book) {
        constants = _constants_;

        book = new Book({
            id: 'book_one',
            name: 'Book one',
            createDate: 1407608313000,
            formats: ['pdf', 'epub'],
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

    it('should generate valid progress bars', function () {
        // Given
        var expected = [
            {
                completeness: 100
            },
            {
                completeness: 100
            },
            {
                completeness: 50
            },
            {
                completeness: 0
            }
        ];

        // When
        book.initProgressBars();

        // Then
        expect(book.progressBars).toEqual(expected);
    });

    it('should show one full bar if the number of total pages is 0', function () {
        // Given

        // When
        zeroPagesBook.initProgressBars();

        // Then
        expect(zeroPagesBook.progressBars).toEqual([{
            completeness: 100
        }]);
    });

    it('should have no more than constants.books.maxBarsCount bars', function () {
        // Given

        // When
        manyPagesBook.initProgressBars();

        // Then
        expect(manyPagesBook.progressBars.length).toEqual(constants.books.maxBarsCount);
    });
});
