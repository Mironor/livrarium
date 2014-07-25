angular.module('lvr.signIn', [])
    .directive('lvrCredentialsSignInForm', function () {
        return {
            restrict: 'E',
            templateUrl: 'app/modules/sign-in/credentials-sign-in-form.html',

            controller: function ($scope, $http, $location, constants, identity) {
                $scope.model = {
                    "email": "",
                    "password": ""
                };

                $scope.submit = function () {

                    $scope.emailPasswordNotValid = $scope.credentials_sign_in_form.$invalid;

                    if ($scope.credentials_sign_up_form.$valid){
                        $http.post('/authenticate/credentials', $scope.model)
                            .success(function (data) {
                                identity.email = data.email;
                                $location.path(constants.applicationUrls.cloud);
                            })
                            .error(function (data) {
                                if (data.code === constants.errorCodes.userNotFound){
                                    $scope.emailPasswordNotValid = true;
                                }
                            });
                    }
                }
            }
        }
    })
    .directive('lvrSocialSignIn', function () {
        return {
            restrict: 'E',
            templateUrl: '/assets/js/app/modules/sign-in/social-sign-in.html'
        }
    });
