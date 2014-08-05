angular.module('lvr.folderTree', ['RecursionHelper'])
    .controller('lvrFolderTreeCtrl', function ($scope) {
        $scope.folders = [
            {
                label: "Furniture", children: [
                { label: "Tables & Chairs" },
                { label: "Sofas" },
                { label: "Occasional Furniture" }
            ]
            },
            {
                label: "Decor", children: [
                { label: "Bed Linen" },
                { label: "Curtains & Blinds" },
                { label: "Carpets" }
            ]
            },
            {
                label: "Storage", children: [
                { label: "Wall Shelving" },
                { label: "Floor Shelving" },
                { label: "Kids Storage" }
            ]
            },
            {
                label: "Lights", children: [
                { label: "Ceiling" },
                { label: "Table" },
                { label: "Floor" }
            ]
            }

        ];

        $scope.expanded = false;

        $scope.toggleExpand = function () {
            $scope.expanded = !$scope.expanded;
        };

    })
    .directive('lvrFolderTree', function (RecursionHelper, constants) {
        return {
            restrict: 'A',
            scope: {children: '='},
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
