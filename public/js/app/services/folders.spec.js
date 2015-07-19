describe('Folders', function() {
    var $httpBackend, constants, i18n,
        foldersService, Folder;

    var rootFolderId = -1;

    // function instead of value to regenerate folder tree each time (as we are working with mutable objects)
    var rootSubFolders = function() {
    };

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

    var folderTreeFixture = function() {
        return {
            id: rootFolderId,
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
                            label: 'Beginning with MongoDB',
                            children: []
                        }
                    ]
                }
            ]
        };
    };


    beforeEach(module('lvr'));

    beforeEach(inject(function(_$httpBackend_, _constants_, i18nEn, folders, _Folder_) {
        constants = _constants_;
        i18n = i18nEn;

        foldersService = folders;
        Folder = _Folder_;

        $httpBackend = _$httpBackend_;

        $httpBackend.when('GET', constants.api.foldersTree)
            .respond(folderTreeFixture());

        $httpBackend.when('GET', constants.api.folderContents(-1))
            .respond(rootFolderContents());
    }));

    afterEach(function() {
        $httpBackend.verifyNoOutstandingExpectation();
        $httpBackend.verifyNoOutstandingRequest();
    });


    it("should retrieve root folder", function() {
        // Given

        // When
        foldersService.fetchFolderTree();
        $httpBackend.flush();
        var rootFolder = foldersService.rootFolder;

        // Then
        expect(rootFolder.name).toEqual("");
        expect(rootFolder.children.length).toEqual(2);
    });

    it("should retrieve root's contents", function() {
        // Given

        // When
        foldersService.fetchFolderContents(rootFolderId);
        $httpBackend.flush();
        var folder = foldersService.currentFolder;

        // Then
        expect(folder.name).toEqual("");

        expect(folder.contents.folders.length).toEqual(2);
        expect(_.map(folder.contents.folders, 'id')).toEqual([1, 2]);

        expect(folder.contents.books.length).toEqual(3);
        expect(_.map(folder.contents.books, 'identifier')).toEqual(['uuid-1', 'uuid-2', 'uuid-3']);
    });

    it("should pick the the name for a new folder (if a folder with a default name already exists in current folder)", function() {
        // Given
        var currentFolder = foldersService.currentFolder,
            newFolder = new Folder({
                label: i18n['content.folders.newFolderName']
            });

        currentFolder.contents.folders.push(newFolder);

        // When
        var secondNewFolderName = foldersService.getNewFolderNameInCurrentFolder();

        // Then
        expect(secondNewFolderName).toBe(i18n['content.folders.newFolderName'] + i18n['content.numberSign'] + '2')
    });

    it("should send request to create a new folder", function() {
        // Given
        var newFolderName = "new folder";

        // When Then
        $httpBackend.expectPOST(constants.api.createFolder, {
            parentId: -1,
            name: newFolderName
        }).respond(200, {id: 3});
        foldersService.createFolder(rootFolderId, newFolderName);
        $httpBackend.flush();
    });

    /*

     it("should generate correct path to current folder", function () {
     // Given
     var expectedPath = "/database/Beginning with MongoDB";

     // When

     // Then
     expect(foldersService.getCurrentPath()).toBe()

     })
     */
});