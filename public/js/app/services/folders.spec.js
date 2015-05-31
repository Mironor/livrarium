describe('Folders', function() {
    var $httpBackend, constants, i18n,
        foldersService, Folder;

    // function instead of value to regenerate folder tree each time (as we are working with mutable objects)
    var generateFolderTreeFixture = function() {
        return {
            id: 1,
            name: "",
            children:[
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

    beforeEach(module('lvr', function($provide) {
        $provide.value('bootstrapData', {
            rootFolder: generateFolderTreeFixture()
        })
    }));

    beforeEach(inject(function(_$httpBackend_, _constants_, i18nEn, folders, _Folder_) {
        constants = _constants_;
        i18n = i18nEn;

        foldersService = folders;
        Folder = _Folder_;

        $httpBackend = _$httpBackend_;
    }));

    afterEach(function() {
        $httpBackend.verifyNoOutstandingExpectation();
        $httpBackend.verifyNoOutstandingRequest();
    });

    it("should correctly initialize root folder", function() {
        // Given
        var rootFolder = foldersService.getCurrentFolder();

        // When

        // Then
        expect(rootFolder.children.length).toEqual(2);
    });

    it("should retrieve root folder", function() {
        // Given
        var rootFolder = foldersService.getRootFolder();

        // When

        // Then
        expect(rootFolder.name).toEqual("");
        expect(rootFolder.children.length).toEqual(2);
    });

    it("should recursively contain only Folders as children", function() {
        // Given
        var rootFolder = foldersService.getCurrentFolder(),
            foldersArray;

        function flattenRecursiveFolders(stack, children) {
            _(children).forEach(function(child) {
                stack.push(child);
                flattenRecursiveFolders(stack, child.children);
            });

            return stack;
        }

        // When
        foldersArray = flattenRecursiveFolders([], rootFolder.children);

        // Then
        expect(foldersArray.length).toEqual(5);

        _(foldersArray).forEach(function(folder) {
            expect(folder instanceof Folder).toBeTruthy();
        });
    });

    /*
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
     */
});