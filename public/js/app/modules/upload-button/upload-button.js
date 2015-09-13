angular.module('lvr.uploadButton', [
    'ngFileUpload'
]).directive('lvrUploadButton', function(constants) {
    return {
        restrict: 'E',
        templateUrl: constants.pathToApp + 'modules/upload-button/upload-button.html',
        controller: function($scope, Upload, folders, books) {

            var _onProgress = function(evt) {
                    var progressPercentage = parseInt(100.0 * evt.loaded / evt.total);
                    console.log(progressPercentage);
                },

                _onSuccess = function(data, status, headers, config) {
                    books.addToContent(data);
                },
                _onError = function(data, status, headers, config) {
                    console.log('Upload error: ', status)
                };

            $scope.uploadFile = function(file) {
                var currentFolderId = folders.currentFolder.id;

                if (currentFolderId === undefined) throw new Error("Trying to upload to undefined folder");

                $scope.upload = Upload.upload({
                    url: constants.api.upload(currentFolderId),
                    method: 'POST',
                    file: file, // or list of files: $files for html5 only
                    fileFormDataName: 'books'
                })
                    .progress(_onProgress)
                    .success(_onSuccess)
                    .error(_onError);

            };

            $scope.onFileSelect = function($files) {
                $files.forEach($scope.uploadFile);
            };
        }
    }
});
