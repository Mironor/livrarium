angular.module('lvr')
    .factory('books', function (Restangular, Book) {
       Restangular.extendModel('books', function (obj) {
           return angular.extend(obj, Book);
       })
    });
