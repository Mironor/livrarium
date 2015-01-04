angular.module('lvr.signIn', [])
    .directive('lvrCredentialsSignInForm', function (constants) {
        return {
            restrict: 'E',
            templateUrl: constants.pathToApp + 'modules/sign-in/credentials-sign-in-form.html',

            controller: function ($scope, $http, $location, constants, identity) {
                $scope.model = {
                    "email": "",
                    "password": ""
                };

                $scope.submit = function () {
                    $scope.emailPasswordNotValid = $scope.credentials_sign_in_form.$invalid;
                    $scope.accessDenied = false;

                    if ($scope.credentials_sign_in_form.$valid) {
                        $http.post(constants.api.signInWithCredentials, $scope.model)
                            .success(function (data) {
                                identity.email = data.email;
                                $location.path(constants.applicationUrls.cloud);
                            })
                            .error(function (data) {
                                if (data.code === constants.errorCodes.userNotFound) {
                                    $scope.emailPasswordNotValid = true;
                                }
                            });
                    }
                }
            }
        }
    })
    .directive('lvrSocialSignIn', function (constants) {
        return {
            restrict: 'E',
            templateUrl: constants.pathToApp + 'modules/sign-in/social-sign-in.html'
        }
    });
