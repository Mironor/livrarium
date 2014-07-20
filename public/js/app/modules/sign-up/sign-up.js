angular.module('lvr.signUp', [])
    .directive('lvrCredentialsSignUpForm', function () {
        return {
            restrict: 'E',
            templateUrl: '/assets/js/app/modules/sign-up/credentials-sign-up-form.html',
            controller: function ($scope, $http, $location, constants, identity) {
                $scope.translationData = {
                    existingEmail: $scope.existingEmail
                };

                $scope.model = {
                    "email": "",
                    "password": ""
                };

                $scope.submit = function () {

                    $scope.emailNotValid = $scope.credentials_sign_up_form.email.$invalid;
                    $scope.passwordNotValid = $scope.credentials_sign_up_form.password.$invalid;

                    if ($scope.credentials_sign_up_form.$valid) {
                        $http.post('/sign-up', $scope.model)
                            .success(function (data) {
                                identity.email = data.identity;
                                $location.path(constants.applicationUrls.cloud);
                            })
                            .error(function (data) {
                                $scope.userAlreadyExists = data.code === constants.errorCodes.userAlreadyExists;
                                $scope.existingEmail = $scope.model.email;
                            });
                    }
                };
            }
        }
    })
    .directive('lvrSocialSignUp', function () {
        return {
            restrict: 'E',
            templateUrl: '/assets/js/app/modules/sign-up/social-sign-up.html'
        }
    });

