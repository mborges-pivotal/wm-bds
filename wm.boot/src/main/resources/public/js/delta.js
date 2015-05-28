
var app = angular.module('myApp', [ 'ngGrid','ui.bootstrap']);

/**
 * homeCtrl
 */
app.controller('homeCtrl', function($scope, $http) {

	$http.get('/tables/').success(function(data) {
		$scope.tables = data;
		$scope.displayTables = [].concat($scope.tables);

	})

	$scope.mySelections = [];

	$scope.gridTable = {
		data : 'displayTables',
		selectedItems : $scope.mySelections,
		multiSelect : false,
		showFilter : true,
		cellFilter: 'ao',
		columnDefs : [ {
			field : 'name',
			displayName : 'Name',
			width : '200px'
		} ],
		afterSelectionChange : function() {
			$scope.selectedIDs = [];
			angular.forEach($scope.mySelections, function(item) {
				// $scope.selectedIDs.push( item.id )
				$scope.tableName = item.name;
				console.log(item.name);
				//$http({url: '/batch', method: 'GET', params: {table:item.name, prior:1, curr:2}}).success(function(data) {
				$http({url: '/batches', method: 'GET', params: {table:item.name}}).success(function(data) {
					console.log(data)
					$scope.batches = data;
				})
			});
		}
	}; //gridTables
	
	$scope.batchSelections = [];

	$scope.gridBatches = {
			data : 'batches',
			selectedItems : $scope.batchSelections,
			multiSelect : false,
			//showFilter : true,
			enableSorting: true,
			sortInfo: {fields:['id'], directions: ['desc']},
			columnDefs : [ {field : 'id', displayName : 'Id', width : 50},
			               {field : 'date', displayName : 'Date', width : 100},
			               {field : 'count', displayName : 'Count', width : 80}],
			afterSelectionChange : function() {
				angular.forEach($scope.batchSelections, function(item) {
				console.log("batch:"+item.id);
				$http({url: '/batch', method: 'GET', params: {table:$scope.tableName, prior:1, curr:2}}).success(function(data) {
					console.log(data)
					$scope.batch = data;
					
				})
			});
		}

	}; //gridBatches
	

}) // app.controller

/**
 * TabsDemoCtrl
 */
app.controller('TabsDemoCtrl', function ($scope, $window, $http) {
		
	$http.get('/errors/').success(function(data) {
		$scope.loadErrors = data;
		console.log(data);
	})
	
	
	$scope.gridErrors = {
			data : 'loadErrors',
			multiSelect : false,
			showFilter : true,
			enableSorting: true,
			columnDefs : [ {field : 'cmdtime', displayName : 'Cmd Time', width : 150},
			               {field : 'count', displayName : 'Count', width : 80},
			               {field : 'filename', displayName : 'Source', width : 700}]
	}; //gridOptions2
	
  $scope.tabs = [
    { title:'Batch Differences', content:'Dynamic content 1' },
  ];

  $scope.alertMe = function() {
    setTimeout(function() {
      $window.alert('You\'ve selected the alert tab!');
    });
  };
});

