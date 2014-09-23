describe('Folders', function () {
    var httpBackend, constants, i18n,
        foldersService, Folder, Book,
        folderTree = [
            {
                label: 'javascript',
                children: [
                    {
                        label: 'BDD with Jasmine',
                        children: []
                    },
                    {
                        label: 'Algorithms in js',
                        children: []
                    }
                ]
            },
            {
                label: 'database',
                children: [
                    {
                        label: 'Beginning with MongoDB',
                        children: []
                    }
                ]
            }
        ];

    beforeEach(module('lvr'));

    beforeEach(inject(function ($httpBackend, _constants_, i18nEn, folders, _Folder_) {
        constants = _constants_;
        i18n = i18nEn;

        httpBackend = $httpBackend;
        httpBackend.when('GET', constants.applicationUrls.folders)
            .respond(folderTree);

        Folder = _Folder_;

        foldersService = folders;
        foldersService.initRootFolder();

        httpBackend.flush();
    }));

    afterEach(function () {
        httpBackend.verifyNoOutstandingExpectation();
        httpBackend.verifyNoOutstandingRequest();
    });

    it("should correctly initialize root folder", function () {
        // Given
        var rootFolder = foldersService.getRootFolder();

        // When

        // Then
        expect(rootFolder.children.length).toEqual(2);
    });

    it("should recursively contain only Folders as children", function () {
        // Given
        var rootFolder = foldersService.getRootFolder(),
            foldersArray;

        function flattenRecursiveFolders(stack, children) {
            _(children).forEach(function (child) {
                stack.push(child);
                flattenRecursiveFolders(stack, child.children);
            });

            return stack;
        }

        // When
        foldersArray = flattenRecursiveFolders([], rootFolder.children);

        // Then
        expect(foldersArray.length).toEqual(5);

        _(foldersArray).forEach(function (folder) {
            expect(folder instanceof Folder).toBeTruthy();
        });
    });

    it("should be able to save folder tree by sending it to the server", function () {
        // Given
        httpBackend.expectPOST(constants.applicationUrls.folders, folderTree).respond(200, {});

        // When
        foldersService.saveRootFolder();
        httpBackend.flush();

        // Then

    });


    it("should pick the the name for a new folder (if a folder with a default name already exists in current folder)", function () {
        // Given
        var currentFolder = foldersService.getCurrentFolder(),
            newFolder = new Folder({
                label: i18n['content.folders.newFolderName']
            });

        currentFolder.children.push(newFolder);

        // When
        var secondNewFolderName = foldersService.getNewFolderNameInCurrentFolder();

        // Then
        expect(secondNewFolderName).toBe(i18n['content.folders.newFolderName'] + i18n['content.numberSign'] + '2')
    });

    it("should generate correct path to current folder", function () {
        // Given
        var expectedPath = "/database/Beginning with MongoDB";

        // When

        // Then
        expect(foldersService.getCurrentPath()).toBe()

    })
});