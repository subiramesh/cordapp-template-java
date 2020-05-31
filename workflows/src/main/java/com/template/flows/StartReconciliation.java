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
import net.corda.core.flows.InitiatingFlow;
import net.corda.core.flows.StartableByRPC;
import net.corda.core.identity.Party;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.transactions.TransactionBuilder;
import net.corda.core.utilities.ProgressTracker;

import java.util.Currency;

/**
 * @author suramesh
 * Implements the logic that takes place behind flow initiation
 * HelperTenant initiates the flow
 * SourceTenant has to sign it
 * Helper and Source are with respect to real time multi tenant transactions
 * SourceTenant(for instance BrainTree) hands over a transaction to be completed by PayPal(HelperTenant)
 * HelperTenant carries out the transaction and reconciles it with SourceTenant
 */
@InitiatingFlow
@StartableByRPC
public class StartReconciliation extends FlowLogic<Void> {
    private final ProgressTracker progressTracker = new ProgressTracker();

    private final Party otherParty;
    private final Amount<Currency> amount;
    private final String transactionId;

    public StartReconciliation(Party otherParty, Amount<Currency> amount, String transactionId) {
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
    public Void call() throws FlowException {
        // Initiator flow logic goes here.
        Party notary = getServiceHub().getNetworkMapCache().getNotaryIdentities().get(0);

        TransactionState transactionState = new TransactionState(transactionId,amount,otherParty,getOurIdentity());
        Command command = new Command<>(new TransactionContract.Commands.Reconcile(), getOurIdentity().getOwningKey());
        TransactionBuilder txBuilder = new TransactionBuilder(notary)
                .addOutputState(transactionState, TransactionContract.ID)
                .addCommand(command);
        SignedTransaction signedTx = getServiceHub().signInitialTransaction(txBuilder);
        FlowSession otherPartySession = initiateFlow(otherParty);
        subFlow(new FinalityFlow(signedTx, otherPartySession));

        return null;
    }
}
