angular.module('lvr')
    .factory('folders', function($http, constants, i18nEn, Folder) {

        var FoldersService = {
            // These public fields are only for angular binding, otherwise use helper functions instead
            rootFolder: new Folder(),

            currentFolder: undefined,

            fetchFolderTree: function() {
                return $http.get(constants.api.foldersTree)
                    .success(function(data) {
                        _(data.children).forEach(function(folderData) {
                            this.rootFolder.children.push(new Folder(folderData));
                        }.bind(this));
                    }.bind(this));
            },

            fetchFolderContents: function(folderId) {
                return $http.get(constants.api.folderContents(folderId))
                    .success(function(data) {
                        _(data.folders).forEach(function(folder) {
                            this.currentFolder.contents.folders.push(new Folder(folder));
                        }.bind(this));

                        _(data.books).forEach(function(book) {
                            this.currentFolder.contents.books.push(new Folder(book));
                        }.bind(this));
                    }.bind(this));
            },

            getNewFolderNameInCurrentFolder: function() {
                var subFolders = this.currentFolder.contents.folders,
                    expectedNumber = 1;

                subFolders.forEach(function(subFolder) {
                    if (_.str.startsWith(subFolder.label, i18nEn['content.folders.newFolderName'])) {
                        expectedNumber++;
                    }
                });

                var postfix = ((expectedNumber > 1) ? i18nEn['content.numberSign'] + expectedNumber : '');
                return i18nEn['content.folders.newFolderName'] + postfix;
            },

            createFolder: function(parentId, name) {
                return $http.post(constants.api.createFolder, {
                    parentId: parentId,
                    name: name
                })
            }
        };

        FoldersService.currentFolder = FoldersService.rootFolder;

        return FoldersService;


        // Legacy
        /*

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
    });
