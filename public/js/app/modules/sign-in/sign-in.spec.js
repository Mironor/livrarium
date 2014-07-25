describe('Sign In', function () {
    var $scope;

    beforeEach(module('lvr.signIn', function ($provide) {
        $provide.constant('constants', {
            applicationUrls: {
                signIn: '/',
                signUp: '/sign-up',
                cloud: '/cloud'
            },

            errorCodes: {
                userNotFound: 4002,
                userAlreadyExists: 4005
            }
        });

        $provide.value('identity', {
            email: undefined
        })
    }));

    beforeEach(module('app/modules/sign-in/credentials-sign-in-form.html', 'app/modules/sign-in/social-sign-in.html'));

    beforeEach(module('pascalprecht.translate', function ($translateProvider) {
        $translateProvider.translations('en', {
            "login.login": "Log in"
        });
        $translateProvider.preferredLanguage('en');
    }));

    beforeEach(inject(function ($rootScope) {
        $scope = $rootScope.$new();
    }));

    it('contains "Log in" words', inject(function ($compile) {
        var element = $compile('<lvr-credentials-sign-in-form></lvr-credentials-sign-in-form>')($scope);

        $scope.$digest();

        expect(element.html()).toContain("Log in");
    }));
});
