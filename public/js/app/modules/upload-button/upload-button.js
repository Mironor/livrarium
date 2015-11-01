angular.module('lvr.uploadButton', [
    'ngFileUpload'
]).directive('lvrUploadButton', function(constants) {
    return {
        restrict: 'E',
        templateUrl: constants.pathToApp + 'modules/upload-button/upload-button.html',
        controller: function($scope, Upload, folders, books) {

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


            function _onProgress(evt) {
                var progressPercentage = parseInt(100.0 * evt.loaded / evt.total);
                console.log(progressPercentage);
            }

            function _onSuccess(data, status, headers, config) {
                books.addToContent(data);
            }

            function _onError(data, status, headers, config) {
                console.log('Upload error: ', data, status)
            }

            $scope.onFileSelect = function($files) {
                $files.forEach($scope.uploadFile);
            };
        }
    }
});
