angular.module('lvr.signIn', [])
    .directive('lvrCredentialsSignInForm', function () {
        return {
            restrict: 'E',
            templateUrl: '/assets/js/app/modules/sign-in/credentials-sign-in-form.html',

            controller: function ($scope, $http, $location, constants, identity) {
                $scope.model = {
                    "email": "",
                    "password": ""
                };

                $scope.error = undefined;

                $scope.submit = function () {
                    $http.post('/authenticate/credentials', $scope.model)
                        .success(function (data) {
                            identity.email = data.email;
                            $location.path(constants.applicationUrls.cloud);
                        })
                        .error(function (data) {
                            $scope.error = data;
                        });
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
