describe("Content", function() {
    var $httpBackend, $compile, $rootScope, constants, i18n,
        folders, Folder, bookViewer, scope, element;

    var validTemplate = '<lvr-content></lvr-content>';

    var rootFolderContentsFixture = function() {
        return {
            folders: [
                {
                    id: 2,
                    name: 'javascript',
                    children: []
                },
                {
                    id: 3,
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

    var folderTreeFixture = function() {
        return {
            id: 1,
            name: "",
            children: [
                {
                    name: 'javascript',
                    children: [
                        {
                            name: 'BDD with Jasmine',
                            children: []
                        },
                        {
                            name: 'Algorithms in js',
                            children: []
                        }
                    ]
                },
                {
                    name: 'database',
                    children: [
                        {
                            name: 'Beginning with MongoDB',
                            children: []
                        }
                    ]
                }
            ]
        };
    };

    beforeEach(module('lvr', 'lvr.content'));

    beforeEach(module(
        'public/js/app/modules/content/content.html',
        'public/js/app/modules/content/content-list.html',
        'public/js/app/modules/content/content-mosaic.html',
        'public/js/app/modules/content/book-progressbar.html'
    ));

    beforeEach(inject(function(_$httpBackend_, $controller, _$rootScope_, _$compile_, _constants_, i18nEn, _bookViewer_, _folders_, _Folder_) {
        $httpBackend = _$httpBackend_;
        $compile = _$compile_;
        $rootScope = _$rootScope_;
        constants = _constants_;
        i18n = i18nEn;
        bookViewer = _bookViewer_;
        folders = _folders_;
        Folder = _Folder_;

        createMosaicView();
    }));

    function createMosaicView(){
        scope = $rootScope.$new();
        create(scope);
    }

    function createListView(){
        scope = $rootScope.$new();
        scope.showAsMosaic = false;
        create(scope);
    }

    function create(scope){
        $httpBackend.when('GET', constants.api.rootContent).respond(rootFolderContentsFixture());

        element = jQuery(validTemplate);
        $compile(element)(scope);

        $httpBackend.flush();
        scope.$apply();
    }

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
    });

    it("should have new folders with incremented count ('New folder #2')", function() {
        // Given
        $httpBackend.whenPOST(constants.api.createFolder).respond(200, {id: 3});

        // When
        element.find('.buttons .button-new-folder').trigger('click');
        element.find('.buttons .button-new-folder').trigger('click');
        $httpBackend.flush();

        // Then
        var folderNames = element.find('.folders .folder span');
        var lastFolderName = folderNames.last().text();

        expect(lastFolderName).toBe(i18n['content.folders.newFolderName'] + i18n['content.numberSign'] + '2');
    });

    it("should append newly created folder to the folder's tree", function() {
        // Given
        $httpBackend.whenGET(constants.api.foldersTree).respond(folderTreeFixture());
        $httpBackend.whenPOST(constants.api.createFolder).respond(200, {id: 3});

        // When
        folders.fetchFolderTree();
        $httpBackend.flush();
        element.find('.buttons .button-new-folder').trigger('click');
        $httpBackend.flush();

        // Then
        var rootFolderChildren = folders.rootFolder.children;

        expect(rootFolderChildren.length).toEqual(3);
        expect(rootFolderChildren[2].name).toBe(i18n['content.folders.newFolderName'])
    });

    it("should open book viewer when clicked on book in mosaic view", function() {
        // Given
        expect(bookViewer.show).toBeFalsy();

        // When
        var bookOne = element.find('.content-mosaic .books .book:first');
        bookOne.dblclick();

        // Then
        expect(bookViewer.show).toBeTruthy();
        expect(bookViewer.openedBook.model.identifier).toEqual(rootFolderContentsFixture().books[0].identifier)
    });

    it("should open book viewer when clicked on book in list view", function() {
        // Given

        // When
        createListView();

        expect(bookViewer.show).toBeFalsy();

        var bookOne = element.find('.content-list .book:first');
        bookOne.dblclick();

        // Then
        expect(bookViewer.show).toBeTruthy();
        expect(bookViewer.openedBook.model.identifier).toEqual(rootFolderContentsFixture().books[0].identifier)
    });
});
