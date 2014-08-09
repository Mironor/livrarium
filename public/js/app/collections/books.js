angular.module('lvr')
    .factory('books', function (Restangular, book) {
       Restangular.extendModel('books', function (obj) {
           return angular.extend(obj, book);
       }) 
    });
