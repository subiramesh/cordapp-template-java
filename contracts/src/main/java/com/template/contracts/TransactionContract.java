package com.template.contracts;

import com.template.states.TransactionState;
import net.corda.core.contracts.CommandData;
import net.corda.core.contracts.CommandWithParties;
import net.corda.core.contracts.Contract;
import net.corda.core.contracts.ContractState;
import net.corda.core.identity.CordaX500Name;
import net.corda.core.transactions.LedgerTransaction;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static net.corda.core.contracts.ContractsDSL.requireSingleCommand;
import static net.corda.core.contracts.ContractsDSL.requireThat;

/**
 * @author suramesh
 * Contract for transactions with TransactionState
 * Primary function is to verify the transactionState that is passed across tenants
 */
public class TransactionContract implements Contract {

    // This is used to identify our contract when building a transaction.
    public static final String ID = "com.template.contracts.TransactionContract";
    private CordaX500Name PayPal = new CordaX500Name("PayPal","London","GB");
    private CordaX500Name BrainTree = new CordaX500Name("BrainTree","London","GB");
    Collection<CordaX500Name> recognizedTenants = Arrays.asList(PayPal, BrainTree);



    // A transaction is valid if the verify() function of the contract of all the transaction's input and output states
    // does not throw an exception.
    @Override
    public void verify(LedgerTransaction tx) {
        final CommandWithParties<Commands> command = requireSingleCommand(tx.getCommands(), Commands.class);
        final Commands commandData = command.getValue();
        List<ContractState> inputs = tx.getInputStates();
        List<ContractState> outputs = tx.getOutputStates();
        if (commandData instanceof TransactionContract.Commands.Reconcile) {
            requireThat(req -> {
                req.using("Transaction must have no input states.", inputs.isEmpty());
                req.using("Transaction must have exactly one output.", outputs.size() == 1);
                req.using("Output must be a TransactionState.", outputs.get(0) instanceof TransactionState);
                TransactionState output = (TransactionState)outputs.get(0);
                req.using("Source Tenant and Helper Tenant cannot be the same",!output.getHelperTenant().equals(output.getSourceTenant()));

                return null;
            });
        } else {
            throw new IllegalArgumentException("Unrecognized command");
        }
    }

    // Used to indicate the transaction's intent.
    public interface Commands extends CommandData {
        class Reconcile implements Commands {}
    }
}