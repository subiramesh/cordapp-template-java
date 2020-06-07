"use strict";

// Define your client-side logic here.

angular.module('demoAppModule', ['ui.bootstrap']).controller('DemoAppCtrl', function($http, $location, $uibModal) {
    const demoApp = this;

    const apiBaseURL = "/api/recon/";

    // Retrieves the identity of this and other nodes.
    let peers = [];
    $http.get(apiBaseURL + "me").then((response) => demoApp.thisNode = response.data.me);
    $http.get(apiBaseURL + "peers").then((response) => peers = response.data.peers);

    demoApp.openCreateTransactionModal = () => {
            const createTransactionModal = $uibModal.open({
                templateUrl: 'createTransactionModal.html',
                controller: 'CreateTransactionModalCtrl',
                controllerAs: 'createTransactionModal',
                resolve: {
                    apiBaseURL: () => apiBaseURL,
                    peers: () => peers
                }
            });

            // Ignores the modal result events.
            createTransactionModal.result.then(() => {}, () => {});
    };

   demoApp.refresh = () => {
        // Update the list of IOUs.
        $http.get(apiBaseURL + "transactions").then((response) => demoApp.transactions =
            Object.keys(response.data).map((key) => response.data[key].state.data));
   }

   demoApp.refresh();
});

angular.module('demoAppModule').config(['$qProvider', function($qProvider) {
    $qProvider.errorOnUnhandledRejections(false);
}]);

