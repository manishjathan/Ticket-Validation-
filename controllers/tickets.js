var myApp = angular.module('myApp');

myApp.controller('TicketsController', ['$scope', '$http', '$location', '$routeParams', function($scope, $http, $location, $routeParams){
	console.log('TicketsController loaded...');

	$scope.getTickets = function(){
		$http.get('/api/tickets').success(function(response){
			$scope.tickets = response;
		});
	}

	$scope.getTicket = function(){
		var id = $routeParams.id;
		$http.get('/api/tickets/'+id).success(function(response){
			$scope.ticket = response;
		});
	}

	$scope.addTicket = function(){
		console.log($scope.ticket);
		$http.post('/api/tickets/', $scope.ticket).success(function(response){
			window.location.href='#/tickets';
		});
	}

	$scope.updateTicket = function(){
		var id = $routeParams.id;
		$http.put('/api/tickets/'+id, $scope.ticket).success(function(response){
			window.location.href='#/tickets';
		});
	}

	$scope.removeTicket = function(id){
		$http.delete('/api/tickets/'+id).success(function(response){
			window.location.href='#/tickets';
		});
	}
}]);