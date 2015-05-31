angular.module('lvr')
    .factory('folders', function($http, constants, i18nEn, bootstrapData, Folder) {

        return {
            // These public fields are only for angular binding, otherwise use helper functions instead
            rootFolder: new Folder(bootstrapData.rootFolder),

            currentFolder: this.rootFolder,

            fetchRootFolder: function () {
                $http.get(constants.applicationUrls.folderTree)
                    .success(function (data) {
                        _(data).forEach(function (folderData) {
                            rootFolder.children.push(new Folder(folderData));
                        });
                    });
            },

            getCurrentFolder: function () {
                return currentFolder;
            },

            getCurrentSubFolders: function() {
                return currentFolder.children;
            },

            getRootFolder: function() {
                return rootFolder;
            }

            // Legacy
            /*
             saveRootFolder: function () {
             $http.post(constants.applicationUrls.folders, this.getRootFolder().children);
             },

             getNewFolderNameInCurrentFolder: function () {
             var currentFolderChildren = this.getCurrentFolder().children,
             expectedNumber = 1;

             _(currentFolderChildren).forEach(function (child) {
             if (_.str.startsWith(child.label, i18nEn['content.folders.newFolderName'])) {
             expectedNumber++;
             }
             });

             var postfix = ((expectedNumber > 1) ? i18nEn['content.numberSign'] + expectedNumber : '');
             return i18nEn['content.folders.newFolderName'] + postfix;
             }
             */
        }
    });
