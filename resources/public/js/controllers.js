var controllers = angular.module('clojure-webapp.controllers', []);

controllers.controller('mainController', function($window, $scope, $location, $modal, ImagesService) {

	$scope.isActive = function (viewLocation) { 
		return viewLocation === $location.path();
	};
	
	$scope.logout = function logout() {
		delete $window.sessionStorage.loggedIn;
		return false;
	};
	
	$scope.RefreshData = function() {
		$scope.images = ImagesService.query();
	};
	
	$scope.addImage = function() {
	
		var image = new ImagesService({
			src: $scope.addSrcText,
			name: $scope.addNameText,
	
		});
	
		image.$save({}, function() {
			$scope.images = ImagesService.query();
		});
	
		$scope.addSrcText = "";
		$scope.addNameText = "";
	};
	
	ImagesService.query(function(result) {
		console.log(result);
		$scope.images = result;
		$scope.error = '';
	}, function(error) {
		if (error.status === 404) {
			$scope.error = 'You have no images saved';
		}
		console.log(error);
	});

});

controllers.controller('LoginCtrl', [
		'$window',
		'$location',
		'$scope',
		'$http',
		'LoginService',
		'UsersService',
		function($window, $location, $scope, http, LoginService,
				UsersService) {
			
			
			$scope.credentials = {};
			$scope.user = {};
			$scope.status = '';
			
			
			$scope.login = function login() {
			
				LoginService.save($scope.credentials, function (result) {
					$window.sessionStorage.loggedIn = true;
					//$window.sessionStorage.userId = result.user_id;
					console.log($scope.credentials);
					
					$scope.status = '';
					$location.path('/');
				}, function (error) {
					$scope.status = error.data.message;
					$scope.credentials.login = '';
					$scope.credentials.password = '';
				});
			};
			
			$scope.register = function register() {
				UsersService.save($scope.user, function (result) {
					$scope.user = {};
					$scope.status = 'You have registered successfully, please log in.';
				}, function (error) {
					console.log(error);
					$scope.status = error.data.message;
				});
			};
		} ]);



