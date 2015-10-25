angular.module('lvr.login', [])
    .controller('lvrCredentialsLoginController', function($scope, $http, $state, constants, identity) {
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
                        $state.go(constants.stateNames.cloudAll);
                    })
                    .error(function(data) {
                        form.email.$error.emailOrPasswordInvalid = data.code === constants.errorCodes.userNotFound;
                        $scope.requestSent = false;
                    });
            }
        }
    });
