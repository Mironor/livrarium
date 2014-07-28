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
            userNotFound: 4002,
            userAlreadyExists: 4005
        }
    });
