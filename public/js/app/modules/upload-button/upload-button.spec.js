describe('Upload Button', function () {
    var $httpBackend, $upload, constants, scope, element, folders, Folder,
        validTemplate = '<lvr-upload-button class="upload pure-button" ng-file-select="onFileSelect($files)"></lvr-upload-button>';

    // all this initialisation is here to test chain calls to the $upload service
    var uploadMock = {}, progressMock = {}, successMock = {};

    beforeEach(module('lvr', function($provide) {
        $provide.value('bootstrapData', {})
    }));

    beforeEach(module( 'lvr.uploadButton'));


    beforeEach(module('public/js/app/modules/upload-button/upload-button.html'));

    beforeEach(inject(function (_$httpBackend_, _$upload_, $rootScope, $compile, _constants_, _folders_, _Folder_) {
        $httpBackend = _$httpBackend_;
        $upload = _$upload_;
        constants = _constants_;
        folders = _folders_;
        Folder = _Folder_;

        scope = $rootScope.$new();
        element = jQuery(validTemplate);
        $compile(element)(scope);

        $upload.upload  = jasmine.createSpy('uploadFn').and.returnValue(uploadMock);
        uploadMock.progress  = jasmine.createSpy('progressFn').and.returnValue(progressMock);
        progressMock.success = jasmine.createSpy('successFn').and.returnValue(successMock);

        scope.$apply();
    }));

    afterEach(function () {
        $httpBackend.verifyNoOutstandingExpectation();
        $httpBackend.verifyNoOutstandingRequest();
    });

    xit('call upload method of the $upload for each element in the $files array', function () {
        // Given
        var fileList = ["file1", "file2", "file3"];

        // When
        scope.onFileSelect(fileList);

        // Then
        expect($upload.upload.calls.count()).toEqual(fileList.length);
    });

    xit('supply expected parameters to the upload method', function () {
        // Given
        spyOn(folders, 'getCurrentFolder').and.returnValue(new Folder({
            id: 1,
            name: "test folder",
            children: []
        }));


        var fileList = [{
                name: "file1"
            }],

            expectedArgument = {
                url: constants.api.upload(1),
                method: 'POST',
                file: {
                    name: "file1"
                },
                fileFormDataName: 'books'
            };


        // When
        scope.onFileSelect(fileList);

        // Then
        expect($upload.upload).toHaveBeenCalledWith(expectedArgument);
    })
});
