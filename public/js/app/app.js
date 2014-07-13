angular.module('lvr', [
    'ui.router',
    'lvr.signIn',
    'lvr.signUp'
])
    .config(function ($stateProvider, $urlRouterProvider, $locationProvider, constants) {
        $stateProvider
            .state('index', {
                templateUrl: '/assets/js/app/modules/index/index.html'
            })
            .state('index.signIn', {
                url: constants.applicationUrls.signIn,
                templateUrl: '/assets/js/app/modules/sign-in/sign-in.html'
            })
            .state('index.signUp', {
                url: constants.applicationUrls.signUp,
                templateUrl: '/assets/js/app/modules/sign-up/sign-up.html'
            })
            .state('cloud', {
                url: constants.applicationUrls.cloud,
                templateUrl: '/assets/js/app/modules/cloud/cloud.html'
            });

        $urlRouterProvider.otherwise('/');

        $locationProvider.html5Mode(true);
    });

