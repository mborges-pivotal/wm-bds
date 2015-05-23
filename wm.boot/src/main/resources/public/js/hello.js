/**
 * 
 */

var app = angular.module('myApp', [ 'ngGrid' ]);

app.controller('homeCtrl', function($scope, $http) {

	$http.get('/tables/').success(function(data) {
		$scope.tables = data;
		$scope.displayTables = [].concat($scope.tables);

	})

	$scope.mySelections = [];

	$scope.gridOptions = {
		data : 'displayTables',
		selectedItems : $scope.mySelections,
		multiSelect : false,
		showFilter : true,
		columnDefs : [ {
			field : 'name',
			displayName : 'Name',
			width : '200px'
		} ],
		afterSelectionChange : function() {
			$scope.selectedIDs = [];
			angular.forEach($scope.mySelections, function(item) {
				// $scope.selectedIDs.push( item.id )
				console.log(item.name);
				$http({url: '/batch', method: 'GET', params: {table:item.name, prior:1, curr:2}}).success(function(data) {
					console.log(data)
				})
			});
		}
	};

})
