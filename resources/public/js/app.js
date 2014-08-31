var clojApp = angular
	.module('clojure-webapp', ['ngRoute', 'ngAnimate', 'wu.masonry', 'ui.bootstrap', 'clojure-webapp.services', 'clojure-webapp.controllers']);

clojApp.config(function($routeProvider) {
	$routeProvider

		.when('/', {
		templateUrl: 'pages/images.html',
		controller: 'mainController'
	})

	.when('/login', {
		templateUrl: 'pages/login.html',
		controller: 'LoginCtrl',
	});
});

clojApp.run(function($window, $rootScope, $location, $route) {

	$rootScope.$on('$routeChangeStart', function(event, nextLoc, currentLoc) {
		if (!window.sessionStorage.loggedIn) {
			$location.path('/login');
		}
	});
});