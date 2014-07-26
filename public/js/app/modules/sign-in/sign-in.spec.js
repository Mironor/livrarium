describe('Sign In', function () {
    var scope, compile,
        validTemplate = '<lvr-credentials-sign-in-form></lvr-credentials-sign-in-form>';

    beforeEach(module('lvr', 'lvr.signIn'));

    beforeEach(module('app/modules/sign-in/credentials-sign-in-form.html', 'app/modules/sign-in/social-sign-in.html'));

    beforeEach(inject(function ($rootScope, $compile) {
        scope = $rootScope.$new();
        compile = $compile
    }));

    it('contains "Log in" words', function () {
        var element = compile(validTemplate)(scope);

        scope.$digest();

        expect(element.html()).toContain("Log in");
    });
});
