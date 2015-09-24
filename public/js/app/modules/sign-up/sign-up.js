angular.module('lvr.signUp', [])
    .controller('lvrCredentialsSignUpController', function($scope, $http, $location, constants, identity) {
        $scope.requestSent = false;

        $scope.translationData = {
            existingEmail: $scope.existingEmail
        };

        $scope.model = {
            email: "",
            password: "",
            rePassword: ""
        };

        $scope.submit = function() {
            var form = $scope.signUpForm;
            form.repassword.$error.notEqualPasswords = $scope.model.password !== $scope.model.rePassword;

            if (form.$valid && !form.repassword.$error.notEqualPasswords ) {

                $scope.requestSent = true;

                $http.post('/sign-up', {
                    "email": $scope.model.email,
                    "password": $scope.model.password
                }).success(function(data) {
                    identity.email = data.email;
                    $location.path(constants.applicationUrls.cloud);
                }).error(function(data) {
                    form.email.$error.userAlreadyExists = data.code === constants.errorCodes.userAlreadyExists;
                    $scope.existingEmail = $scope.model.email;

                    $scope.requestSent = false;
                });
            }
        };
    });

