angular.module('lvr.cloud', [
    'lvr.header',
    'lvr.leftMenu',
    'lvr.content',
    'lvr.bookViewer'
]).controller('CloudController', function($scope, bookViewer) {
    $scope.bookViewer = bookViewer;
});
