describe('Books', function() {
    var $httpBackend, constants, i18n,
        booksService, foldersService, Book;

    beforeEach(module('lvr'));

    beforeEach(inject(function(_$httpBackend_, _constants_, i18nEn, folders, books, _Book_) {
        $httpBackend = _$httpBackend_;

        constants = _constants_;
        i18n = i18nEn;

        foldersService = folders;
        booksService = books;
        Book = _Book_;
    }));

    afterEach(function() {
    });


    it("should append a new book (created from data) to current folder", function() {
        // Given
        var bookData = {
            identifier: 'uuid-1',
            name: 'Book one',
            createDate: 1407608313000,
            formats: ['pdf', 'epub'],
            pages: 160,
            currentPage: 100
        };

        // When
        booksService.addToContent(bookData);
        var currentFolder = foldersService.currentFolder;

        // Then
        expect(currentFolder.contents.books.length).toEqual(1);
    });
});