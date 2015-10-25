angular.module('lvr.cloud', [
    'lvr.header',
    'lvr.sidenav',
    'lvr.content',
    'lvr.bookViewer'
]).controller('CloudController', function($scope, $mdMedia, bookViewer) {
    $scope.bookViewer = bookViewer;
});
