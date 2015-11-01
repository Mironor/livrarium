describe('Books', function() {
    var $httpBackend, constants, i18n,
        booksService, foldersService, Book;

    var allBooksFixture = function() {
        return [{
            identifier: 'uuid-1',
            name: 'Book one',
            createDate: 1407608313000,
            formats: ['pdf', 'epub'],
            pages: 160,
            currentPage: 100
        }, {
            identifier: 'uuid-2',
            name: 'Book two',
            createDate: 1407608313000,
            formats: ['pdf'],
            pages: 170,
            currentPage: 0
        }, {
            identifier: 'uuid-3',
            name: 'Book three',
            createDate: 1407608313000,
            formats: ['epub'],
            pages: 10,
            currentPage: 1
        }];
    };

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

    it("should fetch all user's books", function() {
        // Given
        $httpBackend.expect('GET', constants.api.allBooks)
            .respond(allBooksFixture());

        // When
        var books = booksService.fetchAll();
        $httpBackend.flush();

        // Then
        expect(books.length).toEqual(3);
        expect(_.map(books, 'identifier')).toEqual(['uuid-1', 'uuid-2', 'uuid-3']);
    });
});