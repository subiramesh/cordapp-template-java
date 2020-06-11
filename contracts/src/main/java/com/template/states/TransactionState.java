package com.template.states;

import com.template.contracts.TransactionContract;
import net.corda.core.contracts.Amount;
import net.corda.core.contracts.BelongsToContract;
import net.corda.core.contracts.ContractState;
import net.corda.core.identity.AbstractParty;
import net.corda.core.identity.Party;

import java.util.ArrayList;
import java.util.Currency;
import java.util.List;


/**
 * @author suramesh
 * Modelling the Transaction that will be sent across tenants to be validated by them
 */
@BelongsToContract(TransactionContract.class)
public class TransactionState implements ContractState {

    private String txnUUID;
    private Amount<Currency> amount;
    private String merchantAccountNumber;
    private String receiverAccountNumber;
    private List<Party> stakeholders;
    private Party BrainTree;


    public TransactionState(String txnUUID, Amount<Currency> amount, String merchantAccountNumber,
                            String receiverAccountNumber, List<Party> stakeholders, Party BrainTree) {
        this.txnUUID = txnUUID;
        this.amount = amount;
        this.merchantAccountNumber = merchantAccountNumber;
        this.receiverAccountNumber = receiverAccountNumber;
        this.stakeholders = stakeholders;
        this.BrainTree = BrainTree;
    }



    @Override
    public List<AbstractParty> getParticipants() {
        List<AbstractParty> allParties = new ArrayList<>(stakeholders);
        allParties.add(BrainTree);
        return allParties;
    }

    public String getTxnUUID() {
        return txnUUID;
    }

    public Amount<Currency> getAmount() {
        return amount;
    }

    public String getMerchantAccountNumber() {
        return merchantAccountNumber;
    }

    public String getReceiverAccountNumber() {
        return receiverAccountNumber;
    }

    public List<Party> getStakeholders() {
        return stakeholders;
    }

    public Party getBrainTree() {
        return BrainTree;
    }
}