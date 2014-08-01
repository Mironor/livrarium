angular.module('lvr.content', [])
    .directive('lvrContent', function (constants) {
        return {
            restrict: 'E',
            templateUrl: constants.pathToApp + 'modules/content/content.html'
        };
    });
