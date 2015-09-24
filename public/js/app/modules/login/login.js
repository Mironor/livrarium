angular.module('lvr.login', [])
    .controller('lvrCredentialsLoginController', function($scope, $http, $location, constants, identity) {
        $scope.requestSent = false;

        $scope.model = {
            "email": "",
            "password": ""
        };

        $scope.submit = function() {
            var form = $scope.loginForm;

            if (form.$valid && !$scope.requestSent) {

                $scope.requestSent = true;

                $http.post(constants.api.signInWithCredentials, $scope.model)
                    .success(function(data) {
                        identity.email = data.email;
                        $location.path(constants.applicationUrls.cloud);
                    })
                    .error(function(data) {
                        if (data.code === constants.errorCodes.userNotFound) {
                            form.email.$error.emailOrPasswordInvalid = true;
                        }
                        $scope.requestSent = false;
                    });
            }
        }
    });
