angular.module('lvr.sidenav', [
    'lvr.folderTree',
    'lvr.uploadButton'
]).factory('sidenav', function() {
    return {}
}).directive('lvrSidenav', function(constants) {
    return {
        restrict: 'A',
        templateUrl: constants.pathToApp + 'modules/sidenav/sidenav.html',
        controller: function() {
        }
    };
});
