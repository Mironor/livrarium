angular.module('lvr.bookViewer', [])
    .factory('bookViewer', function(constants, folders, Book) {
        return {
            show: false,

            openedBook: {
                model: new Book({
                    identifier: '',
                    name: ''
                }),
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

                $scope.pdfName = bookViewer.openedBook.model.name;
                $scope.pdfUrl = bookViewer.openedBook.getStreamUrl();
                $scope.scroll = 0;
                $scope.loading = 'loading';

                $scope.close = function() {
                    bookViewer.show = false;
                };

                $scope.getNavStyle = function(scroll) {
                    if (scroll > 100) return 'pdf-controls fixed';
                    else return 'pdf-controls';
                };

                $scope.onError = function(error) {
                    console.log(error);
                };

                $scope.onLoad = function() {
                    $scope.loading = '';
                };

                $scope.onProgress = function(progress) {
                    console.log(progress);
                };
            },

            link: function(scope, element, attrs) {
                var url = scope.url,
                    pdfDoc,
                    pageNum = attrs.page || 1,
                    scale = attrs.scale > 0 ? attrs.scale : 1,
                    canvas = document.getElementById('pdf-viewer'),
                    ctx = canvas.getContext('2d'),
                    windowEl = angular.element($window);

                windowEl.on('scroll', function() {
                    scope.$apply(function() {
                        scope.scroll = windowEl[0].scrollY;
                    });
                });

                PDFJS.disableWorker = true;
                scope.pageNum = pageNum;

                scope.renderPage = function(num) {
                    pdfDoc.getPage(num).then(function(page) {
                        var viewport,
                            pageWidthScale,
                            pageHeightScale,
                            renderContext = {},
                            pageRendering;

                        if (attrs.scale === 'page-fit' && !scale) {
                            viewport = page.getViewport(1);
                            pageWidthScale = element[0].clientWidth / viewport.width;
                            pageHeightScale = element[0].clientHeight / viewport.height;
                            scale = Math.min(pageWidthScale, pageHeightScale);
                        } else {
                            viewport = page.getViewport(scale)
                        }

                        canvas.height = viewport.height;
                        canvas.width = viewport.width;

                        renderContext = {
                            canvasContext: ctx,
                            viewport: viewport
                        };

                        page.render(renderContext).promise.then(function() {
                            if (typeof scope.onPageRender === 'function') {
                                scope.onPageRender();
                            }
                        });
                    });
                };

                scope.goPrevious = function() {
                    if (scope.pageToDisplay <= 1) {
                        return;
                    }
                    scope.pageNum = parseInt(scope.pageNum) - 1;
                };

                scope.goNext = function() {
                    if (scope.pageToDisplay >= pdfDoc.numPages) {
                        return;
                    }
                    scope.pageNum = parseInt(scope.pageNum) + 1;
                };

                scope.zoomIn = function() {
                    scale = parseFloat(scale) + 0.2;
                    scope.renderPage(scope.pageToDisplay);
                    return scale;
                };

                scope.zoomOut = function() {
                    scale = parseFloat(scale) - 0.2;
                    scope.renderPage(scope.pageToDisplay);
                    return scale;
                };

                scope.changePage = function() {
                    scope.renderPage(scope.pageToDisplay);
                };

                scope.rotate = function() {
                    if (canvas.getAttribute('class') === 'rotate0') {
                        canvas.setAttribute('class', 'rotate90');
                    } else if (canvas.getAttribute('class') === 'rotate90') {
                        canvas.setAttribute('class', 'rotate180');
                    } else if (canvas.getAttribute('class') === 'rotate180') {
                        canvas.setAttribute('class', 'rotate270');
                    } else {
                        canvas.setAttribute('class', 'rotate0');
                    }
                };

                function renderPDF() {
                    if (url) {
                        PDFJS.getDocument(url, null, null, scope.onProgress).then(
                            function(_pdfDoc) {
                                if (typeof scope.onLoad === 'function') {
                                    scope.onLoad();
                                }

                                pdfDoc = _pdfDoc;
                                scope.renderPage(scope.pageToDisplay);

                                scope.$apply(function() {
                                    scope.pageCount = _pdfDoc.numPages;
                                });
                            }, function(error) {
                                if (error) {
                                    if (typeof scope.onError === 'function') {
                                        scope.onError(error);
                                    }
                                }
                            }
                        );
                    }
                }

                scope.$watch('pageNum', function(newVal) {
                    scope.pageToDisplay = parseInt(newVal);
                    if (pdfDoc !== null) {
                        scope.renderPage(scope.pageToDisplay);
                    }
                });

                scope.$watch('pdfUrl', function(newVal) {
                    if (newVal !== '') {
                        console.log('pdfUrl value change detected: ', scope.pdfUrl);
                        url = newVal;
                        scope.pageToDisplay = 1;
                        renderPDF();
                    }
                });

            }
        };
    });
