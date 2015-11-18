describe('Login', function() {
    var $httpBackend, $state, scope, controller, constants, identity;

    beforeEach(module('lvr', 'lvr.login'));

    beforeEach(module('stateMock'));

    beforeEach(inject(function($controller, _$state_, _$httpBackend_, $rootScope, _constants_, _identity_) {

        $httpBackend = _$httpBackend_;
        $state = _$state_;
        constants = _constants_;
        identity = _identity_;


        scope = $rootScope.$new();
        scope.loginForm = {
            email: {
                $error: {}
            }
        };
        controller = $controller('lvrCredentialsLoginController', {$scope: scope});
    }));

    afterEach(function() {
        $httpBackend.verifyNoOutstandingExpectation();
        $httpBackend.verifyNoOutstandingRequest();
        $state.ensureAllTransitionsHappened();
    });

    it('should not submit request if form is not valid', function() {
        // Given
        scope.loginForm.$valid = false;

        // When
        scope.submit();
        scope.$digest();

        // Then
        expect(scope.requestSent).toBeFalsy();
        $httpBackend.verifyNoOutstandingRequest();
    });

    it('should not submit request there is already a sent request', function() {
        // Given
        scope.loginForm.$valid = true;
        scope.requestSent = true;

        // When
        scope.submit();
        scope.$digest();

        // Then
        $httpBackend.verifyNoOutstandingRequest();
    });

    it('should show error if user does not exist', function() {
        // Given
        expect(scope.loginForm.email.$error.emailOrPasswordInvalid).toBeFalsy();

        scope.loginForm.$valid = true;
        scope.model.email = "valid@email.com";
        scope.model.password = "valid_password";

        $httpBackend.expectPOST(constants.api.signInWithCredentials).respond(500, {
            "code": constants.errorCodes.userNotFound
        });

        // When
        scope.submit();
        $httpBackend.flush();
        scope.$digest();

        // Then
        expect(scope.loginForm.email.$error.emailOrPasswordInvalid).toBeTruthy();
    });

    it('should do transition to cloud after successful login', function() {
        // Given
        scope.loginForm.$valid = true;
        var email = "valid@email.com";
        var idRoot = 1;
        scope.model.email = email;
        scope.model.password = "valid_password";

        $httpBackend.expectPOST(constants.api.signInWithCredentials).respond({
            "email": email,
            "idRoot": idRoot
        });
        $state.expectTransitionTo(constants.stateNames.cloudAll);

        // When
        scope.submit();
        $httpBackend.flush();
        scope.$digest();

        // Then
    });

    it('should modify identity value on successful log in', function() {
        // Given
        scope.loginForm.$valid = true;
        var email = "valid@email.com";
        var idRoot = 1;
        scope.model.email = email;
        scope.model.password = "valid_password";

        $httpBackend.expectPOST(constants.api.signInWithCredentials).respond({
            "email": email,
            "idRoot": idRoot
        });
        $state.expectTransitionTo(constants.stateNames.cloudAll);

        // When
        scope.submit();
        $httpBackend.flush();
        scope.$digest();

        // Then
        expect(identity.email).toBe(email);
        expect(identity.idRoot).toBe(idRoot);
    });
});
