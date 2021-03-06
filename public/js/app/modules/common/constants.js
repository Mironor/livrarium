var pathToApp = window.pathToApp || 'public/js/app/'; // this is most useful for karma tests

angular.module('lvr')
    .constant('constants', {
        pathToApp: pathToApp,

        // Urls that will be sent to the server via ajax requesting json data
        api: {
            signInWithCredentials: '/authenticate/credentials',
            signUp: '/sign-up',
            allBooks: '/books',
            foldersTree: '/folders/tree',
            rootContent: '/folders/root',
            createFolder: '/folders/create',
            folderContents: function(folderId) {
                return '/folders/contents/' + folderId.toString()
            },
            upload: function(folderId) {
                return '/upload/' + folderId.toString()
            },
            bookStream: function(folderId, identifier, extension) {
                return '/books/stream/' + folderId.toString() + '/' + identifier + '/' + extension
            }
        },

        // Internal (client-side) urls
        applicationUrls: {
            login: '/',
            signUp: '/sign-up',
            cloud: '/cloud',
            cloudAll: '/all'
        },

        stateNames: {
            index: 'index',
            signUp: 'signUp',
            cloud: 'cloud',
            cloudAll: 'cloud.all'
        },

        // Application errors (the ones returned by server must have the same id as here)
        errorCodes: {
            userNotFound: 4004,
            accessDenied: 4002,
            userAlreadyExists: 4005
        }
    });
