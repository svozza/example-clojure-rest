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
		publicAccess: true
	});
});

clojApp.run(function($window, $rootScope, $location, $route) {

	var routesOpenToPublic = [];
	angular.forEach($route.routes, function(route, path) {
		// push route onto routesOpenToPublic if it has a truthy publicAccess value
		route.publicAccess && (routesOpenToPublic.push(path));
	});

	$rootScope.$on('$routeChangeStart', function(event, nextLoc, currentLoc) {
		var closedToPublic = (-1 === routesOpenToPublic.indexOf($location.path()));
		if (closedToPublic && !window.sessionStorage.loggedIn) {
			$location.path('/login');
		}
	});
});