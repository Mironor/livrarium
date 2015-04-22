angular.module('lvr.uploadButton', [
    'angularFileUpload'
]).directive('lvrUploadButton', function (constants) {
    return {
        restrict: 'E',
        templateUrl: constants.pathToApp + 'modules/upload-button/upload-button.html',
        controller: function ($scope, $upload, folders) {
            $scope.uploadFile = function (file) {
                var currentFolderId = folders.getCurrentFolder().id;

                $scope.upload = $upload.upload({
                    url: constants.api.upload(currentFolderId),
                    method: 'POST',
                    file: file, // or list of files: $files for html5 only
                    fileFormDataName: 'books'
                });
            };

            $scope.onFileSelect = function ($files) {
                _.forEach($files, function (file) {
                    $scope.uploadFile(file);
                });
            };
        }
    }
});
