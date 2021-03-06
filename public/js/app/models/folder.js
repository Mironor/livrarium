angular.module('lvr')
    .factory('Folder', function () {
        var folderModel = {
            id: undefined,
            name: "",
            children: [], // used in folder's tree
            contents: {
                folders: [], // do not have any subfolders
                books: []
            }
        };

        var Folder = function (data){
            var folderData = data || {};

            angular.extend(this, folderData);

            // Children should be also passed as data, not as an array of Folder objects
            this.children = this.children.map(function(childData){
                return new Folder(childData);
            });
        };

        angular.extend(Folder.prototype, folderModel);

        return Folder;
    });
