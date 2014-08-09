describe('Book', function () {
    var bookModel;

    beforeEach(module('lvr'));

    beforeEach(inject(function (book) {
        bookModel = angular.extend({
            id: 'book_one',
            name: 'Book one',
            createDate: 1407608313000,
            formats: ['pdf', 'epub'],
            pages: 160,
            currentPage: 80
        }, book);
    }));

    it('should generate valid progress bars', function () {
        // Given
        var expected = [
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
        bookModel.initProgressBars();

        // Then
        expect(bookModel.progressBars).toEqual(expected);
    })
});
