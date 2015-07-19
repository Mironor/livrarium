describe("Content", function() {
    var $httpBackend, constants, i18n,
        Folder, scope, element;

    var validTemplate = '<lvr-content></lvr-content>';

    var rootFolderContents = function() {
        return {
            folders: [
                {
                    id: 1,
                    name: 'javascript',
                    children: []
                },
                {
                    id: 2,
                    name: 'database',
                    children: []
                }
            ],
            books: [{
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
            }]
        }
    };

    beforeEach(module('lvr', 'lvr.content'));

    beforeEach(module(
        'public/js/app/modules/content/content.html',
        'public/js/app/modules/content/content-list.html',
        'public/js/app/modules/content/content-mosaic.html',
        'public/js/app/modules/content/book-progressbar.html'
    ));

    beforeEach(inject(function(_$httpBackend_, $controller, $rootScope, $compile, _constants_, i18nEn, folders, _Folder_) {
        $httpBackend = _$httpBackend_;
        constants = _constants_;
        i18n = i18nEn;
        Folder = _Folder_;

        $httpBackend.when('GET', constants.api.folderContents(-1))
            .respond(rootFolderContents());

        scope = $rootScope.$new();
        element = jQuery(validTemplate);
        $compile(element)(scope);

        $httpBackend.flush();
        scope.$apply();
    }));

    afterEach(function () {
        $httpBackend.verifyNoOutstandingExpectation();
        $httpBackend.verifyNoOutstandingRequest();
    });

    it("should add a new folder when 'add new folder' button is clicked", function() {
        // Given
        var initialFolders = element.find('.folders .folder');
        expect(initialFolders.length).toEqual(2);

        var initialBooks = element.find('.books .book');
        expect(initialBooks.length).toEqual(3);

        $httpBackend.expectPOST(constants.api.createFolder).respond(200, {id: 3});

        // When
        element.find('.buttons .button-new-folder').trigger('click');
        $httpBackend.flush();

        // Then
        var afterFolders = element.find('.folders .folder');
        expect(afterFolders.length).toEqual(3);

        var afterBooks = element.find('.books .book');
        expect(afterBooks.length).toEqual(3);
    })
});
