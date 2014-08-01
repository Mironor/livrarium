/**
 * This spec was created to verify that test system works as expected
 */
describe('Sign In', function () {
    var scope, element, controller,
        validTemplate = '<lvr-credentials-sign-in-form></lvr-credentials-sign-in-form>';

    beforeEach(module('lvr', 'lvr.signIn'));

    beforeEach(module('public/js/app/modules/sign-in/credentials-sign-in-form.html', 'public/js/app/modules/sign-in/social-sign-in.html'));

    beforeEach(inject(function ($rootScope, $compile) {
        scope = $rootScope.$new();
        element = $compile(validTemplate)(scope);
        scope.$digest();
        controller = element.controller;
    }));

    it('contains "Log in" words', function () {
        expect(element.html()).toContain("Log in");
    });
});
