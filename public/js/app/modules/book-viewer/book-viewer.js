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
                var url, pdf, pageScale, pageWidth, pageHeight,
                    currentPage = bookViewer.openedBook.model.currentPage,
                    totalPages = bookViewer.openedBook.model.pages,
                    alreadyRenderedPages = [],

                    container = document.getElementById('pdf-viewer'),
                    pageIdBase = "pdf-viewer-page-",
                    pageWidthPercentageOfWindow = 0.7,
                    pageRangeRender = 2; // n future/previous pages (in relation to the current page) will be rendered


                function renderPDF() {
                    if (url) {
                        PDFJS.getDocument(url, null, null, scope.onProgress).then(function(_pdf) {
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
                    initPageParams(page);

                    insertPageContainers();

                    renderPage(currentPage)(page);

                    renderNeighbourPages(currentPage);
                }

                function initPageParams(page) {
                    var initialViewport = page.getViewport(1);

                    pageScale = $window.innerWidth * pageWidthPercentageOfWindow / initialViewport.width;

                    var viewport = page.getViewport(pageScale);
                    pageWidth = viewport.width;
                    pageHeight = viewport.height;
                }

                function insertPageContainers() {
                    _.range(1, totalPages + 1).forEach(insertPageContainer)
                }

                function insertPageContainer(pageNum) {
                    var canvas = document.createElement('canvas');
                    canvas.id = pageIdBase + pageNum;
                    canvas.width = pageWidth;
                    canvas.height = pageHeight;

                    container.appendChild(canvas);
                }

                function renderPage(pageNum) {
                    return function(pageObj) {
                        //_.inRange(pageNum, 1, totalPages)
                        if (pageNum >= 1 && pageNum <= totalPages && !_.include(alreadyRenderedPages, pageNum)) {
                            var viewport = pageObj.getViewport(pageScale);

                            var canvas = document.getElementById(pageIdBase + pageNum);

                            var context = canvas.getContext('2d');

                            pageObj.render({
                                canvasContext: context,
                                viewport: viewport
                            });

                            console.log("Rendering", pageNum);

                            alreadyRenderedPages.push(pageNum);
                        }
                    }
                }

                function renderNeighbourPages(pageNum) {
                    var minPreviousPageNum = Math.max(1, pageNum - pageRangeRender);
                    var maxNextPageNum = Math.min(totalPages, pageNum + pageRangeRender);
                    for (var i = minPreviousPageNum; i <= maxNextPageNum; i++) {
                        pdf.getPage(i).then(renderPage(i));
                    }
                }

                angular.element($window).bind('scroll', _.debounce(function() {
                    var currentlyViewedPage = Math.round((this.pageYOffset + this.innerHeight) / pageHeight);
                    renderNeighbourPages(currentlyViewedPage);
                }, 50));

                // This should be at the end of the directive
                scope.$watch('url', function(newUrl) {
                    url = newUrl;
                    renderPDF();
                });

            }
        };
    });
