angular.module('lvr.fabProgress', [])
    .directive('mdFabProgress', function(constants) {
        return {
            restrict: "E",
            transclude: true,
            scope: {
                loading: "=",
                click: '&',
                fabClass: '@',
                fabAreaLabel: '@',
                progressClass: '@'
            },
            templateUrl: constants.pathToApp + 'modules/components/fab-progress/fab-progress.html'
        }
    });
