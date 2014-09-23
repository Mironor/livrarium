angular.module('lvr')
    .factory('folders', function ($http, constants, i18nEn, Folder) {

        var rootFolder = new Folder({
                name: 'root',
                children: []
            }),

            currentFolder = rootFolder;

        return {
            initRootFolder: function () {
                $http.get(constants.applicationUrls.folders)
                    .success(function (data) {
                        _(data).forEach(function (folderData) {
                            rootFolder.children.push(new Folder(folderData));
                        });
                    });
            },
            getRootFolder: function () {
                return rootFolder;
            },
            saveRootFolder: function () {
                $http.post(constants.applicationUrls.folders, this.getRootFolder().children);
            },
            getCurrentFolder: function () {
                return currentFolder;
            },
            getCurrentPath: function(){

            },
            getNewFolderNameInCurrentFolder: function () {
                var currentFolderChildren = this.getCurrentFolder().children,
                    expectedNumber = 1;

                _(currentFolderChildren).forEach(function (child) {
                    if (_.str.startsWith(child.label, i18nEn['content.folders.newFolderName'])){
                        expectedNumber++;
                    }
                });

                var postfix = ((expectedNumber > 1) ? i18nEn['content.numberSign'] + expectedNumber : '');
                return i18nEn['content.folders.newFolderName'] + postfix;
            }
        }
    });
