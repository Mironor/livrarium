angular.module('lvr.signIn', [])
    .controller('lvrCredentialsSignInController', function($scope, $http, $location, constants, identity) {
        $scope.model = {
            "email": "",
            "password": ""
        };

        $scope.submit = function() {
            $scope.emailPasswordNotValid = $scope.credentials_sign_in_form.$invalid;
            $scope.accessDenied = false;

            if ($scope.credentials_sign_in_form.$valid) {
                $http.post(constants.api.signInWithCredentials, $scope.model)
                    .success(function(data) {
                        identity.email = data.email;
                        $location.path(constants.applicationUrls.cloud);
                    })
                    .error(function(data) {
                        if (data.code === constants.errorCodes.userNotFound) {
                            $scope.emailPasswordNotValid = true;
                        }
                    });
            }
        }
    });
