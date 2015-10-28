fdescribe('Sign up', function () {
    var $httpBackend, $state, scope, controller, constants, identity;

    beforeEach(module('lvr', 'lvr.signUp'));

    beforeEach(module('stateMock'));

    beforeEach(inject(function($controller, _$state_, _$httpBackend_,  $rootScope, _constants_, _identity_) {

        $httpBackend = _$httpBackend_;
        $state = _$state_;
        constants = _constants_;
        identity = _identity_;

        scope = $rootScope.$new();
        scope.signUpForm = {
            email: {
                $error: {}
            },
            repassword:{
                $error: {}
            }
        };
        controller = $controller('lvrCredentialsSignUpController', {$scope: scope});
    }));

    afterEach(function () {
        $httpBackend.verifyNoOutstandingExpectation();
        $httpBackend.verifyNoOutstandingRequest();
    });

    it('should not submit request if form is not valid', function() {
        // Given
        scope.signUpForm.$valid = false;

        // When
        scope.submit();
        scope.$digest();

        // Then
        expect(scope.requestSent).toBeFalsy();
        $httpBackend.verifyNoOutstandingRequest();
    });

    it('should not submit request if there is already a sent request', function() {
        // Given
        scope.signUpForm.$valid = true;
        scope.requestSent = true;

        // When
        scope.submit();
        scope.$digest();

        // Then
        $httpBackend.verifyNoOutstandingRequest();
    });

    it('should not submit request if passwords are not the same', function () {
        // Given
        scope.signUpForm.$valid = true;
        scope.model.password = "valid_password";
        scope.model.rePassword = "second_valid_password";

        // When
        scope.submit();
        scope.$digest();

        // Then
        $httpBackend.verifyNoOutstandingRequest();
    });

    it('should show error if user already exists', function () {
        // Given
        expect(scope.signUpForm.email.$error.useralreadyexists).toBeFalsy();

        scope.signUpForm.$valid = true;

        $httpBackend.expectPOST(constants.api.signUp).respond(500, {
            "code": constants.errorCodes.userAlreadyExists
        });

        // When
        scope.submit();
        $httpBackend.flush();
        scope.$digest();

        // Then
        expect(scope.signUpForm.email.$error.useralreadyexists).toBeTruthy();
    });

    it('should do transition to cloud after successful sign up', function () {
        // Given
        scope.signUpForm.$valid = true;
        scope.requestSent = false;
        var email = "valid@email.com";
        scope.model.email = email;

        $httpBackend.expectPOST(constants.api.signUp).respond({
            "email": email
        });
        $state.expectTransitionTo(constants.stateNames.cloudAll);

        // When
        scope.submit();
        $httpBackend.flush();
        scope.$digest();

        // Then
    });

    it('should modify identity value on successful sign up', function () {
        // Given
        scope.signUpForm.$valid = true;
        scope.requestSent = false;
        var email = "valid@email.com";
        scope.model.email = email;

        $httpBackend.expectPOST(constants.api.signUp).respond({
            "email": email
        });
        $state.expectTransitionTo(constants.stateNames.cloudAll);

        // When
        scope.submit();
        $httpBackend.flush();
        scope.$digest();

        // Then
        expect(identity.email).toBe(email);
    });

});
