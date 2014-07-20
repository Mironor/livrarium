angular.module('lvr')
    .constant('constants', {
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
