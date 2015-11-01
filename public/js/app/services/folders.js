angular.module('lvr')
    .factory('folders', function($http, constants, i18nEn, Folder, Book) {

        var FoldersService = {
            // These public fields are only for angular binding, otherwise use helper functions instead
            rootFolder: new Folder(),

            currentFolder: undefined,

            /**
             * Fetchs user's folders in a form of a tree structure
             * @returns root folder (that has children that have children and so on)
             */
            fetchFolderTree: function() {
                return $http.get(constants.api.foldersTree)
                    .success(function(data) {
                        data.children.forEach(function(folderData) {
                            this.rootFolder.children.push(new Folder(folderData));
                        }.bind(this));
                    }.bind(this));
            },

            /**
             * Fetchs and fills the content of currently opened folder
             */
            fetchCurrentFolderContents: function() {
                if (this.currentFolder.id === undefined) this._fetchRootContents();
                else this._fetchFolderContents(this.currentFolder.id);
            },

            _fetchFolderContents: function(folderId) {
                return this._fetchFolderContentsByUrl(constants.api.folderContents(folderId))
            },

            _fetchRootContents: function() {
                return this._fetchFolderContentsByUrl(constants.api.rootContent)
                    .success(function(data) {
                        if (this.currentFolder.id === undefined) this.currentFolder.id = data.id;
                    }.bind(this))
            },

            _fetchFolderContentsByUrl: function(apiUrl) {
                return $http.get(apiUrl)
                    .success(function(data) {
                        data.folders.forEach(function(folder) {
                            this.currentFolder.contents.folders.push(new Folder(folder));
                        }.bind(this));

                        data.books.forEach(function(book) {
                            this.currentFolder.contents.books.push(new Book(book));
                        }.bind(this));
                    }.bind(this));

            },

            /**
             * Generates a semantically correct new name for a folder (if a folder with that name already
             * exists, appends a unique prefix to the name)
             * @returns {*}
             */
            getNewFolderNameInCurrentFolder: function() {
                var subFolders = this.currentFolder.contents.folders,
                    expectedNumber = 1;

                subFolders.forEach(function(subFolder) {
                    if (s.startsWith(subFolder.name, i18nEn['content.folders.newFolderName'])) {
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
    });
