angular.module('lvr.uploadButton', [
    'ngFileUpload'
]).directive('lvrUploadButton', function(constants) {
    return {
        restrict: 'E',
        templateUrl: constants.pathToApp + 'modules/upload-button/upload-button.html',
        controller: function($scope, $mdDialog, Upload, folders, books) {

            $scope.progress = 0;

            $scope.uploadFile = function(file) {

                if (file.$error === 'pattern') {
                    $mdDialog.show($mdDialog.alert()
                        .parent(angular.element(document.querySelector('body')))
                        .clickOutsideToClose(true)
                        .title('Error: file type is not supported')
                        .content('Uploaded file type is not supported, please provide one of the following:<br>' +
                            file.$errorParam.replace(/,/g, ', '))
                        .ariaLabel('File type not supported')
                        .ok('Ok')
                    );
                    return;
                }

                if (file.$error === 'maxSize') {
                    $mdDialog.show($mdDialog.alert()
                        .parent(angular.element(document.querySelector('body')))
                        .clickOutsideToClose(true)
                        .title('Error: file size is too large')
                        .content('File size above ' + file.$errorParam + ' are not allowed')
                        .ariaLabel('File size it too large')
                        .ok('Ok')
                    );
                    return;
                }

                var currentFolderId = folders.currentFolder.id;

                if (currentFolderId === undefined) throw new Error("Trying to upload to undefined folder");

                $scope.upload = Upload.upload({
                    url: constants.api.upload(currentFolderId),
                    method: 'POST',
                    file: file, // or list of files: $files for html5 only
                    fileFormDataName: 'file'
                }).progress(_onProgress).success(_onSuccess).error(_onError);

            };


            function _onProgress(evt) {
                $scope.progress = parseInt(100.0 * evt.loaded / evt.total);
            }

            function _onSuccess(data, status, headers, config) {
                $scope.progress = 0;
                books.addToContent(data);
            }

            function _onError(data, status, headers, config) {
                $scope.progress = 0;
                console.log('Upload error: ', data, status)
            }

            $scope.onFileSelect = function($files) {
                $files.forEach($scope.uploadFile);
            };
        }
    }
});
