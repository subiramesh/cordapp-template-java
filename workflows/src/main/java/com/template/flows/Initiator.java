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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Currency;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@InitiatingFlow
@StartableByRPC
public class Initiator extends FlowLogic<SignedTransaction> {
    private final ProgressTracker progressTracker = new ProgressTracker();

    private final String txnUUID;
    private final Amount<Currency> amount;
    private final String merchantAccountNumber;
    private final String receiverAccountNumber;


    public Initiator(String txnUUID, Amount<Currency> amount, String merchantAccountNumber, String receiverAccountNumber) {
        this.txnUUID = txnUUID;
        this.amount = amount;
        this.merchantAccountNumber = merchantAccountNumber;
        this.receiverAccountNumber = receiverAccountNumber;
    }

    @Override
    public ProgressTracker getProgressTracker() {
        return progressTracker;
    }

    @Suspendable
    @Override
    public SignedTransaction call() throws FlowException {
        Party notary = getServiceHub().getNetworkMapCache().getNotaryIdentities().get(0);
        Party BrainTree = getOurIdentity();

        List<Party> stakeholders = getServiceHub().getNetworkMapCache().getAllNodes().stream()
                .map(nodeInfo -> nodeInfo.getLegalIdentities().get(0))
                .collect(Collectors.toList());
        stakeholders.remove(BrainTree);
        stakeholders.remove(notary);

        TransactionState transactionState = new TransactionState(txnUUID,amount,merchantAccountNumber,receiverAccountNumber,stakeholders,BrainTree);
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
