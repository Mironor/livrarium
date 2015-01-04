/**
 * This spec was created to verify that test system works as expected
 */
describe('Sign in', function () {
    var httpBackend, location, scope,
        element, form, constants, identity,
        validTemplate = '<lvr-credentials-sign-in-form></lvr-credentials-sign-in-form>';

    beforeEach(module('lvr', 'lvr.signIn'));

    beforeEach(module('public/js/app/modules/sign-in/credentials-sign-in-form.html', 'public/js/app/modules/sign-in/social-sign-in.html'));

    beforeEach(inject(function ($httpBackend, $location, $rootScope, $compile, _constants_, _identity_) {
        httpBackend = $httpBackend;
        location = $location;
        constants = _constants_;
        identity = _identity_;

        scope = $rootScope.$new();
        element = angular.element(validTemplate);
        $compile(element)(scope);

        scope.$apply();

        form = scope.credentials_sign_in_form;
    }));

    afterEach(function() {
        httpBackend.verifyNoOutstandingExpectation();
        httpBackend.verifyNoOutstandingRequest();
    });

    it('contains "Log in" words', function () {
        expect(element.html()).toContain("Log in");
    });

    it('doesn\'t show any error by default', function () {
        var $shownErrorDivs = $(element).find('.error').not('.ng-hide');
        expect($shownErrorDivs).not.toExist();
    });

    it('should show error if email is invalid', function () {
        // Given
        form.email.$setViewValue("invalid@}@email.com");
        form.password.$setViewValue("valid_password");

        // When
        scope.submit();
        scope.$digest();

        // Then
        expect(element.find('.email-password-not-valid').hasClass('ng-hide')).toBeFalsy()
    });

    it('should show error if password is too short', function () {
        // Given
        form.email.$setViewValue("valid@email.com");
        form.password.$setViewValue("inval");

        // When
        scope.submit();
        scope.$digest();

        // Then
        expect(element.find('.email-password-not-valid').hasClass('ng-hide')).toBeFalsy()
    });

    it('should show error if user does not exist', function () {
        // Given
        form.email.$setViewValue("valid@email.com");
        form.password.$setViewValue("valid_password");

        httpBackend.expectPOST(constants.api.signInWithCredentials)
            .respond(500, {
                "code": constants.errorCodes.userNotFound
            });

        // When
        scope.submit();
        httpBackend.flush();
        scope.$digest();

        // Then
        expect(element.find('.email-password-not-valid').hasClass('ng-hide')).toBeFalsy()
    });

    it('should modify identity value on successful log in', function () {
        // Given
        var email = "valid@email.com";
        form.email.$setViewValue(email);
        form.password.$setViewValue("valid_password");

        httpBackend.expectPOST(constants.api.signInWithCredentials)
            .respond({
                "email": email
            });

        // When
        scope.submit();
        httpBackend.flush();
        scope.$digest();

        // Then
        expect(identity.email).toBe(email);
    });

    it('should redirect user after a successful authentication', function () {
        // Given
        var email = "valid@email.com";
        form.email.$setViewValue(email);
        form.password.$setViewValue("valid_password");

        httpBackend.expectPOST(constants.api.signInWithCredentials)
            .respond({
                "email": email
            });

        // When
        scope.submit();
        httpBackend.flush();
        scope.$digest();

        // Then
        expect(location.path()).toBe(constants.applicationUrls.cloud);
    });
});
