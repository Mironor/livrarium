angular.module('lvr', [
    'ui.router',
    'pascalprecht.translate',

    'lvr.signIn',
    'lvr.signUp',
    'lvr.cloud'
])
    .config(function ($stateProvider, $urlRouterProvider, $locationProvider, constants) {
        $stateProvider
            .state('index', {
                templateUrl: constants.pathToApp + 'modules/index/index.html'
            })
            .state('index.signIn', {
                url: constants.applicationUrls.signIn,
                templateUrl: constants.pathToApp + 'modules/sign-in/sign-in.html'
            })
            .state('index.signUp', {
                url: constants.applicationUrls.signUp,
                templateUrl: constants.pathToApp + 'modules/sign-up/sign-up.html'
            })
            .state('cloud', {
                url: constants.applicationUrls.cloud,
                templateUrl: constants.pathToApp + 'modules/cloud/cloud.html'
            });

        $urlRouterProvider.otherwise('/');

        $locationProvider.html5Mode(true);
    })
    .config(function ($translateProvider, i18nEn) {
        $translateProvider.translations('en', i18nEn);
        $translateProvider.preferredLanguage('en');
    });

