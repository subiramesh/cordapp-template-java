"use strict";

angular.module('demoAppModule').controller('switchPartyModalCtrl', function($http, $uibModalInstance, $uibModal, apiBaseURL, peers) {
    const createTransactionModal = this;

    createTransactionModal.peers = peers;

    /** Validate and create an IOU. */
    switchPartyModal.switchParty = () => {

            $uibModalInstance.close();

            const party = switchPartyModal.form.party;
            // We define the IOU creation endpoint.
            const switchPartyEndpoint =
                apiBaseURL +
                `switch-party?party=${party}`;

            // We hit the endpoint to create the IOU and handle success/failure responses.
            $http.get(startTransactionEndpoint).then(
                (result) => createTransactionModal.displayMessage(result),
                (result) => createTransactionModal.displayMessage(result)
            );

    };

    /** Displays the success/failure response from attempting to create an IOU. */
    createTransactionModal.displayMessage = (message) => {
        const createTransactionMsgModal = $uibModal.open({
            templateUrl: 'createTransactionMsgModal.html',
            controller: 'createTransactionMsgModalCtrl',
            controllerAs: 'createTransactionMsgModal',
            resolve: {
                message: () => message
            }
        });

        // No behaviour on close / dismiss.
        createTransactionMsgModal.result.then(() => {}, () => {});
    };

    /** Closes the IOU creation modal. */
    createTransactionModal.cancel = () => $uibModalInstance.dismiss();

    // Validates the IOU.
    function invalidFormInput() {
        return isNaN(createTransactionModal.form.amount) || (createTransactionModal.form.counterparty === undefined);
    }
});

// Controller for the success/fail modal.
angular.module('demoAppModule').controller('createTransactionMsgModalCtrl', function($uibModalInstance, message) {
    const createTransactionMsgModal = this;
    createTransactionMsgModal.message = message.data;
});