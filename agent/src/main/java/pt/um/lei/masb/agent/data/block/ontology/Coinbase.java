package pt.um.lei.masb.agent.data.block.ontology;

import jade.content.Concept;
import pt.um.lei.masb.blockchain.TransactionOutput;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Set;

public final class Coinbase implements Concept {
    private Set<TransactionOutput> payoutTXO;
    private String coinbase;
    private String hashId;


    public Coinbase(@NotNull Set<TransactionOutput> payoutTXO,
                    @NotEmpty String coinbase,
                    @NotEmpty String hashId) {
        this.payoutTXO = payoutTXO;
        this.coinbase = coinbase;
        this.hashId = hashId;
    }

    public @NotNull Set<TransactionOutput> getPayoutTXO() {
        return payoutTXO;
    }

    public void setPayoutTXO(@NotNull Set<TransactionOutput> payoutTXO) {
        this.payoutTXO = payoutTXO;
    }

    public @NotEmpty String getCoinbase() {
        return coinbase;
    }

    public void setCoinbase(@NotEmpty String coinbase) {
        this.coinbase = coinbase;
    }

    public @NotEmpty String getHashId() {
        return hashId;
    }

    public void setHashId(@NotEmpty String hashId) {
        this.hashId = hashId;
    }


}
