package com.template.flows;

import co.paralleluniverse.fibers.Suspendable;
import net.corda.core.flows.FlowException;
import net.corda.core.flows.FlowLogic;
import net.corda.core.flows.FlowSession;
import net.corda.core.flows.InitiatedBy;
import net.corda.core.flows.ReceiveFinalityFlow;

/**
 * @author suramesh
 * Implements the logic that takes place once the flow is iniated by the Helper Tenant and passed to Source Tenant for signing
 */
@InitiatedBy(StartReconciliation.class)
public class CheckTransaction extends FlowLogic<Void> {
    private FlowSession counterpartySession;

    public CheckTransaction(FlowSession counterpartySession) {
        this.counterpartySession = counterpartySession;
    }

    @Suspendable
    @Override
    public Void call() throws FlowException {
        // Responder flow logic goes here.
        subFlow(new ReceiveFinalityFlow(counterpartySession));
        return null;
    }
}
