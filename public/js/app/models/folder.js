angular.module('lvr')
    .factory('Folder', function () {
        var folderModel = {
            id: -1,
            name: "",
            children: []
        };

        var Folder = function (data){
            angular.extend(this, data);

            // Children should be also passed as data, not as an array of Folder objects
            this.children = _.map(this.children, function(childData){
                return new Folder(childData);
            });
        };

        angular.extend(Folder.prototype, folderModel);

        return Folder;
    });
