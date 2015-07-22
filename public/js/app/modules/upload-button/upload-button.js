angular.module('lvr.uploadButton', [
    'angularFileUpload'
]).directive('lvrUploadButton', function(constants) {
    return {
        restrict: 'E',
        templateUrl: constants.pathToApp + 'modules/upload-button/upload-button.html',
        controller: function($scope, $upload, folders, books) {

            var _onProgress = function(evt) {
                    var progressPercentage = parseInt(100.0 * evt.loaded / evt.total);
                },

                _onSuccess = function(data, status, headers, config) {
                    books.addToContent(data);
                };

            $scope.uploadFile = function(file) {
                var currentFolderId = folders.currentFolder.id;

                if (currentFolderId === undefined) throw new Error("Trying to upload to undefined folder");

                $scope.upload = $upload.upload({
                    url: constants.api.upload(currentFolderId),
                    method: 'POST',
                    file: file, // or list of files: $files for html5 only
                    fileFormDataName: 'books'
                }).progress(_onProgress).success(_onSuccess);

            };

            $scope.onFileSelect = function($files) {
                _.forEach($files, function(file) {
                    $scope.uploadFile(file);
                });
            };
        }
    }
});
