angular.module('lvr', [
    'ngMaterial',
    'ngAnimate',
    'ngMessages',
    'ui.router',
    'pascalprecht.translate',

    'lvr.components',
    'lvr.login',
    'lvr.signUp',
    'lvr.cloud'
])
    .config(function($stateProvider, $urlRouterProvider, $locationProvider, constants) {
        $stateProvider
            .state(constants.stateNames.index, {
                url: constants.applicationUrls.login,
                templateUrl: constants.pathToApp + 'modules/login/login2.html'
            })
            .state(constants.stateNames.signUp, {
                url: constants.applicationUrls.signUp,
                templateUrl: constants.pathToApp + 'modules/sign-up/sign-up.html'
            })
            .state(constants.stateNames.cloud, {
                url: constants.applicationUrls.cloud,
                templateUrl: constants.pathToApp + 'modules/cloud/cloud.html'
            })
            .state(constants.stateNames.cloudAll, {
                url: constants.applicationUrls.cloudAll,
                templateUrl: constants.pathToApp + 'modules/content/all/all.html'
            });

        $urlRouterProvider.otherwise('/cloud');

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

