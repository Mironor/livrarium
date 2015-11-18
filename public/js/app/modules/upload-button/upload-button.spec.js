describe('Upload Button', function () {
    var $httpBackend, Upload, constants, scope, element, folders, Folder,
        validTemplate = '<lvr-upload-button class="upload pure-button" ng-file-select="onFileSelect($files)"></lvr-upload-button>';

    // all this initialisation is here to test chain calls to the $upload service
    var uploadMock = {}, progressMock = {}, successMock = {}, errorMock = {};

    beforeEach(module('lvr', function($provide) {
        $provide.value('bootstrapData', {});

        // Without this, the test says "Unexpected GET request `path_to_icon`", so we will shut it down
        $provide.factory('mdIconDirective', function () {
            return angular.noop;
        });
    }));

    beforeEach(module( 'lvr.uploadButton', 'lvr.components'));

    beforeEach(module('public/js/app/modules/upload-button/upload-button.html', 'public/js/app/modules/components/fab-progress/fab-progress.html'));

    beforeEach(inject(function (_$httpBackend_, _Upload_, $rootScope, $compile, _constants_, _folders_, _Folder_) {
        $httpBackend = _$httpBackend_;
        Upload = _Upload_;
        constants = _constants_;
        folders = _folders_;
        Folder = _Folder_;


        scope = $rootScope.$new();
        element = jQuery(validTemplate);
        $compile(element)(scope);

        Upload.upload  = jasmine.createSpy('uploadFn').and.returnValue(uploadMock);
        uploadMock.progress  = jasmine.createSpy('progressFn').and.returnValue(progressMock);
        progressMock.success = jasmine.createSpy('successFn').and.returnValue(successMock);
        successMock.error = jasmine.createSpy('successFn').and.returnValue(errorMock);

        scope.$apply();
    }));

    afterEach(function () {
        $httpBackend.verifyNoOutstandingExpectation();
        $httpBackend.verifyNoOutstandingRequest();
    });

    it('call upload method of the $upload for each element in the $files array', function () {
        // Given
        var fileList = ["file1", "file2", "file3"];
        folders.currentFolder.id = 1;

        // When
        scope.onFileSelect(fileList);

        // Then
        expect(Upload.upload.calls.count()).toEqual(fileList.length);
    });

    it('should throw error if trying to upload to undefined folder', function () {
        // Given
        var fileList = ["file1", "file2", "file3"];

        // When Then
        expect(function(){
            scope.onFileSelect(fileList)
        }).toThrow();
    });

    it('supply expected parameters to the upload method', function () {
        // Given
        folders.currentFolder = new Folder({
            id: 1,
            name: "test folder"
        });


        var fileList = [{
                name: "file1"
            }],

            expectedArgument = {
                url: constants.api.upload(1),
                method: 'POST',
                file: {
                    name: "file1"
                },
                fileFormDataName: 'file'
            };


        // When
        scope.onFileSelect(fileList);

        // Then
        expect(Upload.upload).toHaveBeenCalledWith(expectedArgument);
    })
});
