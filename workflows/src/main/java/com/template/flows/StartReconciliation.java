/*
package com.template.flows;

import co.paralleluniverse.fibers.Suspendable;
import com.template.contracts.TransactionContract;
import com.template.states.TransactionState;
import net.corda.core.contracts.Amount;
import net.corda.core.contracts.Command;
import net.corda.core.flows.FinalityFlow;
import net.corda.core.flows.FlowException;
import net.corda.core.flows.FlowLogic;
import net.corda.core.flows.FlowSession;
import net.corda.core.flows.InitiatedBy;
import net.corda.core.flows.InitiatingFlow;
import net.corda.core.flows.ReceiveFinalityFlow;
import net.corda.core.flows.StartableByRPC;
import net.corda.core.identity.Party;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.transactions.TransactionBuilder;
import net.corda.core.utilities.ProgressTracker;

import java.util.ArrayList;
import java.util.Currency;
import java.util.List;
import java.util.stream.Collectors;

*/
/**
 * @author suramesh
 * Implements the logic that takes place behind flow initiation
 * HelperTenant initiates the flow
 * SourceTenant has to sign it
 * Helper and Source are with respect to real time multi tenant transactions
 * SourceTenant(for instance BrainTree) hands over a transaction to be completed by PayPal(HelperTenant)
 * HelperTenant carries out the transaction and reconciles it with SourceTenant
 *//*

public class StartReconciliation{
    private StartReconciliation(){
    }

    @InitiatingFlow
    @StartableByRPC
    public class Initiator extends FlowLogic<SignedTransaction>{
        private final ProgressTracker progressTracker = new ProgressTracker();

        private final Party otherParty;
        private final Amount<Currency> amount;
        private final String transactionId;

        public Initiator(Party otherParty, Amount<Currency> amount, String transactionId) {
            this.otherParty = otherParty;
            this.amount = amount;
            this.transactionId = transactionId;
        }

        @Override
        public ProgressTracker getProgressTracker() {
            return progressTracker;
        }

        @Suspendable

        @Override
        public SignedTransaction call() throws FlowException {
            Party notary = getServiceHub().getNetworkMapCache().getNotaryIdentities().get(0);
            Party customer = getOurIdentity();

            List<Party> stakeholders = getServiceHub().getNetworkMapCache().getAllNodes().stream()
                    .map(nodeInfo -> nodeInfo.getLegalIdentities().get(0))
                    .collect(Collectors.toList());
            stakeholders.remove(customer);
            stakeholders.remove(notary);


            TransactionState transactionState = new TransactionState(transactionId,amount,otherParty,getOurIdentity());
            Command command = new Command<>(new TransactionContract.Commands.Reconcile(), getOurIdentity().getOwningKey());
            TransactionBuilder txBuilder = new TransactionBuilder(notary)
                    .addOutputState(transactionState, TransactionContract.ID)
                    .addCommand(command);

            txBuilder.verify(getServiceHub());

            SignedTransaction selfSignedTransaction = getServiceHub().signInitialTransaction(txBuilder);

            List<FlowSession> stakeHolderSessions = new ArrayList<>();
            for(Party stakeholder:stakeholders)
                stakeHolderSessions.add(initiateFlow(stakeholder));

            return subFlow(new FinalityFlow(selfSignedTransaction, stakeHolderSessions));

        }
    }

    @InitiatedBy(Initiator.class)
    public static class Responder extends FlowLogic<SignedTransaction>{

        private FlowSession counterPartySession;

        public Responder(FlowSession counterPartySession) {
            this.counterPartySession = counterPartySession;
        }

        @Override
        @Suspendable
        public SignedTransaction call() throws FlowException {
            return subFlow(new ReceiveFinalityFlow(counterPartySession));
        }


    }


}
*/
