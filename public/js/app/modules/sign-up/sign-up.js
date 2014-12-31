angular.module('lvr.signUp', [])
    .directive('lvrCredentialsSignUpForm', function (constants) {
        return {
            restrict: 'E',
            templateUrl: constants.pathToApp + 'modules/sign-up/credentials-sign-up-form.html',
            controller: function ($scope, $http, $location, constants, identity) {
                $scope.translationData = {
                    existingEmail: $scope.existingEmail
                };

                $scope.model = {
                    email: "",
                    password: "",
                    rePassword: ""
                };

                $scope.submit = function () {

                    $scope.emailNotValid = $scope.credentials_sign_up_form.email.$invalid;
                    $scope.passwordNotValid = $scope.credentials_sign_up_form.password.$invalid;
                    $scope.passwordsAreEqual = $scope.model.password === $scope.model.rePassword;

                    if ($scope.credentials_sign_up_form.$valid && !$scope.passwordsAreEqual) {
                        $http.post('/sign-up', {
                            "email": $scope.model.email,
                            "password": $scope.model.password
                        }).success(function (data) {
                            identity.email = data.identity;
                            $location.path(constants.applicationUrls.cloud);
                        }).error(function (data) {
                            $scope.userAlreadyExists = data.code === constants.errorCodes.userAlreadyExists;
                            $scope.existingEmail = $scope.model.email;
                        });
                    }
                };
            }
        }
    })
    .directive('lvrSocialSignUp', function (constants) {
        return {
            restrict: 'E',
            templateUrl: constants.pathToApp + 'modules/sign-up/social-sign-up.html'
        }
    });

