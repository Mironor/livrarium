angular.module('lvr.bookViewer', [])
    .factory('bookViewer', function(constants, folders, Book) {
        return {
            show: false,

            openedBook: {
                model: new Book(),
                getStreamUrl: function() {
                    return constants.api.bookStream(folders.currentFolder.id, this.model.identifier, 'pdf')
                }
            }
        }
    })
    .directive('lvrBookViewer', function($window, constants, bookViewer) {
        return {
            restrict: 'E',
            templateUrl: constants.pathToApp + 'modules/book-viewer/book-viewer.html',

            controller: function($scope) {
                $scope.openedBook = bookViewer.openedBook;

                $scope.url = bookViewer.openedBook.getStreamUrl();
                $scope.name = bookViewer.openedBook.model.name;
                $scope.loading = 'loading 0%';

                $scope.close = function() {
                    bookViewer.show = false;
                };

                $scope.onError = function(error) {
                    console.log("Error: " + error);
                };

                $scope.onLoad = function() {
                    console.log($scope.loading);
                    $scope.loading = '';
                    $scope.$apply();
                };

                $scope.onProgress = function(progress) {
                    $scope.loading = 'loading ' + Math.round(progress.loaded * 100 / progress.total) + '%';
                    console.log($scope.loading);
                    $scope.$apply();
                };
            },

            link: function(scope, element, attrs) {
                var url, pdf,
                    currentPage = 1,
                    totalPages = bookViewer.openedBook.model.pages,

                    container = document.getElementById('pdf-viewer');


                function renderPDF() {
                    if (url) {
                        PDFJS.getDocument(url, null, null, scope.onProgress).then(
                            function(_pdf) {
                                pdf = _pdf;
                                scope.onLoad();

                                pdf.getPage(currentPage).then(handlePages)

                            }, function(error) {
                                scope.onError(error);
                            }
                        );
                    }
                }

                function handlePages(page) {
                    var initialViewport = page.getViewport(1);

                    var widthScale = $window.innerWidth * 0.7 / initialViewport.width;
                    var viewport = page.getViewport(widthScale);

                    var canvas = document.createElement('canvas');
                    canvas.height = viewport.height;
                    canvas.width = viewport.width;

                    var context = canvas.getContext('2d');

                    page.render({
                        canvasContext: context,
                        viewport: viewport
                    });

                    container.appendChild(canvas);

                    currentPage++;
                    if (currentPage <= totalPages) {
                        pdf.getPage(currentPage).then(handlePages)
                    }
                }

                // This should be at the end of the directive
                scope.$watch('url', function(newUrl) {
                    if (newUrl !== '') {
                        console.log('pdfUrl value change detected: ', newUrl);
                        url = newUrl;
                        renderPDF();
                    }
                });

            }
        };
    });
