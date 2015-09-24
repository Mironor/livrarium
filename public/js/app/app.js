angular.module('lvr', [
    'ngMaterial',
    'ui.router',
    'pascalprecht.translate',

    'lvr.components',
    'lvr.signIn',
    'lvr.signUp',
    'lvr.cloud'
])
    .config(function($stateProvider, $urlRouterProvider, $locationProvider, constants) {
        $stateProvider
            .state('index', {
                url: constants.applicationUrls.signIn,
                templateUrl: constants.pathToApp + 'modules/sign-in/sign-in.html'
            })
            .state('signUp', {
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
    .config(function($translateProvider, i18nEn) {
        $translateProvider.translations('en', i18nEn);
        $translateProvider.preferredLanguage('en');
    })
    .config(function($mdThemingProvider) {
        $mdThemingProvider.theme('default')
            .primaryPalette('orange')
            .accentPalette('blue')
            .warnPalette('deep-orange')
            .backgroundPalette('grey', {
                'default': '50'
            })
    });

