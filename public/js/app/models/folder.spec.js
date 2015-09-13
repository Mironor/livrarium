describe('Folder', function () {
    var Folder;
    var rootFolder;

    beforeEach(module('lvr'));

    beforeEach(inject(function (_Folder_) {
        Folder = _Folder_;
        rootFolder = new Folder({
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
                            name: 'Beginning with MongoDB',
                            children: []
                        }
                    ]
                }
            ]
        });
    }));

    it("should recursively contain only Folders as children", function() {
        // Given
        function flattenRecursiveFolders(stack, children) {
            children.forEach(function(child) {
                stack.push(child);
                flattenRecursiveFolders(stack, child.children);
            });

            return stack;
        }

        // When
        var foldersArray = flattenRecursiveFolders([], rootFolder.children);

        // Then
        expect(foldersArray.length).toEqual(5);

        foldersArray.forEach(function(folder) {
            expect(folder instanceof Folder).toBeTruthy();
        });
    });
});
