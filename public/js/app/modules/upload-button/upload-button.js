angular.module('lvr.uploadButton', [
    'angularFileUpload'
]).directive('lvrUploadButton', function (constants) {
    return {
        restrict: 'E',
        templateUrl: constants.pathToApp + 'modules/upload-button/upload-button.html',
        controller: function ($scope, $upload, folders, books) {
            $scope.uploadFile = function (file) {
                var currentFolderId = folders.getCurrentFolder().id;

                $scope.upload = $upload.upload({
                    url: constants.api.upload(currentFolderId),
                    method: 'POST',
                    file: file, // or list of files: $files for html5 only
                    fileFormDataName: 'books'
                }).progress(function (evt) {
                    var progressPercentage = parseInt(100.0 * evt.loaded / evt.total);
                }).success(function (data, status, headers, config) {
                    books.appendToContent(data);
                    //$scope.$apply();
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
