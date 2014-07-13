angular.module('lvr.signUp', [])
    .directive('lvrCredentialsSignUpForm', function () {
        return {
            restrict: 'E',
            templateUrl: '/assets/js/app/modules/sign-up/credentials-sign-up-form.html',
            controller: function ($scope, $http, $location, constants,  identity) {
                $scope.model = {
                    "email": "",
                    "password": ""
                };

                $scope.error = undefined;

                $scope.submit = function () {
                    $http.post('/sign-up', $scope.model)
                        .success(function (data) {
                            identity.email = data.identity;
                            console.log(data.identity);
                            $location.path(constants.applicationUrls.cloud);
                        })
                        .error(function (data) {
                            $scope.error = data;
                        });
                }
            }
        }
    })
    .directive('lvrSocialSignUp', function () {
        return {
            restrict: 'E',
            templateUrl: '/assets/js/app/modules/sign-up/social-sign-up.html'
        }
    });

