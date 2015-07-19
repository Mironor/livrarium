// Inject server variable
var pathToApp = window.pathToApp || 'public/js/app/'; // path for karma tests if undefined

angular.module('lvr')
    .constant('constants', {
        pathToApp: pathToApp,

        api: {
            signInWithCredentials: '/authenticate/credentials',
            signUp: '/sign-up',
            foldersTree: '/folders/tree',
            createFolder: '/folders/create',
            folderContents: function(folderId) {
                return '/folders/contents/' + folderId.toString()
            },
            upload: function(folderId) {
                return '/upload/' + folderId.toString()
            }
        },

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
        },

        folders: {
        }
    });
