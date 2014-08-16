angular.module('lvr')
    .factory('folders', function (Folder) {
        var rootFolder = new Folder({
            name: 'root',
            children: []
        });


        rootFolder = new Folder({
            name: 'root',
            children: [
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
        ]
        });

        var currentFolder = rootFolder;


        return {
            getRootFolder: function () {
                return rootFolder;
            },
            getCurrentFolder: function () {
                return currentFolder;
            }
        }
    });
