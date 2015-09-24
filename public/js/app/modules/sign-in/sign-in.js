angular.module('lvr.signIn', [])
    .controller('lvrCredentialsSignInController', function($scope, $http, $location, constants, identity) {
        $scope.requestSent = false;

        $scope.model = {
            "email": "",
            "password": ""
        };

        // The submit button is outside of the form, so we need to pass the form into the scope
        $scope.forms = {};

        $scope.submit = function() {
            $scope.emailPasswordNotValid = $scope.signInForm.$invalid;
            $scope.accessDenied = false;

            if ($scope.signInForm.$valid && !$scope.requestSent) {

                $scope.requestSent = true;

                $http.post(constants.api.signInWithCredentials, $scope.model)
                    .success(function(data) {
                        identity.email = data.email;
                        $location.path(constants.applicationUrls.cloud);
                    })
                    .error(function(data) {
                        if (data.code === constants.errorCodes.userNotFound) {
                            $scope.signInForm.email.$error.emailOrPasswordInvalid = true;
                        }
                        $scope.requestSent = false;
                    });
            }
        }
    });
