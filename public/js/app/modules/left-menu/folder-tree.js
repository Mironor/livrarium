angular.module('lvr.folderTree', ['RecursionHelper'])
    .controller('lvrFolderTreeCtrl', function ($scope, folders) {
        // this fetch will fill up root folder's children array as soon as ajax request is completed
        folders.fetchFolderTree();
        $scope.rootFolder = folders.rootFolder;

        $scope.expanded = false;

        $scope.toggleExpand = function () {
            $scope.expanded = !$scope.expanded;
        };

    })
    .directive('lvrFolderTree', function (RecursionHelper, constants) {
        return {
            restrict: 'A',
            scope: {
                children: '='
            },
            templateUrl: constants.pathToApp + 'modules/left-menu/folder-tree.html',
            compile: function (element) {
                return RecursionHelper.compile(element, function (scope, iElement, iAttrs, controller, transcludeFn) {
                    scope.collapse = function (node) {
                        node.expanded = false;
                    };

                    scope.expand = function (node) {
                        node.expanded = true;
                    };
                });
            }
        };
    });
