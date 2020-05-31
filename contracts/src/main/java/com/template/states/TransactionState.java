package com.template.states;

import com.template.contracts.TemplateContract;
import com.template.contracts.TransactionContract;
import net.corda.core.contracts.Amount;
import net.corda.core.contracts.BelongsToContract;
import net.corda.core.contracts.ContractState;
import net.corda.core.identity.AbstractParty;
import net.corda.core.identity.Party;

import java.util.Arrays;
import java.util.Currency;
import java.util.List;

/**
 * @author suramesh
 * Modelling the Transaction that will be sent across tenants to be validated by them
 */
@BelongsToContract(TransactionContract.class)
public class TransactionState implements ContractState {

    private String transactionID;
    private Amount<Currency> amount;
    private Party sourceTenant;
    private Party helperTenant;

    public TransactionState(String transactionID, Amount<Currency> amount, Party sourceTenant, Party helperTenant) {
        this.transactionID = transactionID;
        this.amount = amount;
        this.sourceTenant = sourceTenant;
        this.helperTenant = helperTenant;
    }

    @Override
    public List<AbstractParty> getParticipants() {
        return Arrays.asList(sourceTenant,helperTenant);
    }


    public String getTransactionID() {
        return transactionID;
    }

    public Amount<Currency> getAmount() {
        return amount;
    }

    public Party getSourceTenant() {
        return sourceTenant;
    }

    public Party getHelperTenant() {
        return helperTenant;
    }
}