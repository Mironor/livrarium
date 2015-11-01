angular.module('lvr.header', [])
    .directive('lvrHeader', function(constants) {
        return {
            restrict: 'A',
            templateUrl: constants.pathToApp + 'modules/header/header.html',
            controller: function($scope, $mdSidenav) {

                $scope.toggleLeftNav = function() {
                    $mdSidenav("sidenav-left").toggle()
                }
            }
        };
    });
