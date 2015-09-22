var gulp = require('gulp'),
    concat = require('gulp-concat'),
    rename = require('gulp-rename'),
    less = require('gulp-less');

var paths = {
    styles : 'public/styles/less/cloud.less'
};

gulp.task('deps_dev', function () {
    gulp.src([
        'node_modules/pdfjs-dist/build/pdf.combined.js',
        'bower_dependencies/angular/angular.js',
        'bower_dependencies/angular-aria/angular-aria.js',
        'bower_dependencies/angular-animate/angular-animate.js',
        'bower_dependencies/angular-material/angular-material.js',
        'bower_dependencies/angular-ui-router/release/angular-ui-router.js',
        'bower_dependencies/angular-translate/angular-translate.js',
        'bower_dependencies/angular-recursion/angular-recursion.js',
        'bower_dependencies/ng-file-upload/ng-file-upload.js',
        'bower_dependencies/lodash/lodash.js',
        'bower_dependencies/underscore.string/dist/underscore.string.js'
    ])
        .pipe(concat('lvr-vendor.js'))
        .pipe(gulp.dest('public/js/'))
});

gulp.task('deps_test', function () {
    gulp.src([
        'bower_dependencies/jquery/dist/jquery.js',

        'bower_dependencies/angular/angular.js',
        'bower_dependencies/angular-animate/angular-animate.js',
        'bower_dependencies/angular-aria/angular-aria.js',
        'bower_dependencies/angular-material/angular-material.js',
        'bower_dependencies/angular-mocks/angular-mocks.js',
        'bower_dependencies/angular-translate/angular-translate.js',
        'bower_dependencies/angular-ui-router/release/angular-ui-router.js',
        'bower_dependencies/angular-recursion/angular-recursion.js',
        'bower_dependencies/ng-file-upload/ng-file-upload.js',
        'bower_dependencies/lodash/lodash.js',
        'bower_dependencies/underscore.string/dist/underscore.string.js',

        'bower_dependencies/jasmine-jquery/lib/jasmine-jquery.js',
        'node_modules/phantomjs-polyfill/bind-polyfill.js'
    ])
        .pipe(concat('lvr-vendor-test.js'))
        .pipe(gulp.dest('public/js/'))
});

gulp.task('less', function () {
    gulp.src(paths.styles)
        .pipe(less())
        .pipe(rename('lvr-cloud.css'))
        .pipe(gulp.dest('public/styles/'))
});

gulp.task('watch', function () {
    gulp.watch(paths.styles, ['less'])
});