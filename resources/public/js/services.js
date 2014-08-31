var serviceModule = angular.module('clojure-webapp.services', [ 'ngResource' ]);

serviceModule.factory('ImagesService',
		function($resource) {
			return $resource('api/images/:id', {id: "@id"}, {
			});
		});

serviceModule.factory('LoginService',
		function($resource) {
			return $resource('login', {}, {
			});
		});

serviceModule.factory('UsersService',
		function($resource) {
			return $resource('api/users/:id', {id: "@id"}, {
			});
		});