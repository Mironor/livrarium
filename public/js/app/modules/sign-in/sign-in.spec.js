describe('Sign In', function () {
    var $scope;

    beforeEach(module('lvr', 'lvr.signIn'));

    beforeEach(module('app/modules/sign-in/credentials-sign-in-form.html', 'app/modules/sign-in/social-sign-in.html'));

    beforeEach(inject(function ($rootScope) {
        $scope = $rootScope.$new();
    }));

    it('contains "Log in" words', inject(function ($compile) {
        var element = $compile('<lvr-credentials-sign-in-form></lvr-credentials-sign-in-form>')($scope);

        $scope.$digest();

        expect(element.html()).toContain("Log in");
    }));
});
