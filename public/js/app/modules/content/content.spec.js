describe("Content", function () {
    var constants, i18n,
        contentController, Folder,
        scope, newFolderName="New folder";

    beforeEach(module('lvr'));

    beforeEach(inject(function ($httpBackend, $controller, _constants_, i18nEn, folders, _Folder_) {
        constants = _constants_;
        i18n = i18nEn;
        Folder = _Folder_;
        scope = {};

        // Bypass http request
        spyOn(folders, "getCurrentFolder").and.returnValue({
            children: []
        });

        // Block post request
        spyOn(folders, "saveRootFolder");

        contentController = $controller('lvrContentCtrl', {
            $scope: scope
        });
    }));

    /*
    it("should add a new folder when 'add new folder' button is clicked", function () {
        // Given
        expect(scope.folders.length).toEqual(0);

        // When
        scope.createNewFolder();

        // Then
        expect(scope.folders.length).toEqual(1);
        expect(scope.folders[0].label).toBe(i18n['content.folders.newFolderName']);
    })
    */
});
