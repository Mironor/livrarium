// Inject server variable
var pathToApp = window.pathToApp || 'public/js/app/'; // path for karma tests if undefined

angular.module('lvr')
    .constant('constants', {
        pathToApp: pathToApp,

        applicationUrls: {
            signIn: '/',
            signUp: '/sign-up',
            cloud: '/cloud'
        },

        errorCodes: {
            userNotFound: 4004,
            accessDenied: 4002,
            userAlreadyExists: 4005
        },

        books: {
            minPagesPerBar: 50,
            maxBarsCount: 10
        }
    });
