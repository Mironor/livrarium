angular.module('lvr.leftMenu', [
    'lvr.folderTree',
    'lvr.uploadButton'
]).directive('lvrLeftMenu', function (constants) {
    return {
        restrict: 'E',
        templateUrl: constants.pathToApp + 'modules/left-menu/left-menu.html'
    };
});
