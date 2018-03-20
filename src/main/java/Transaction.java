public class Transaction
{
    private String hashBlock;
    private String trans_id_split;
    private String transaction_id;
    private String transaction_fee;
    private String transaction_size_kb;
    private String from_amout;
    private String to_amount;

    public Transaction(String hashBlock)
    {
        this.hashBlock = hashBlock;
    }
    public String getHashBlock()
    {
        return hashBlock;
    }
    public String getTrans_id_split() {
        return trans_id_split;
    }

    public void setTrans_id_split(String trans_id_split) {
        this.trans_id_split = trans_id_split;
    }

    public String getTransaction_id() {
        return transaction_id;
    }

    public void setTransaction_id(String transaction_id) {
        this.transaction_id = transaction_id;
    }

    public String getTransaction_fee() {
        return transaction_fee;
    }

    public void setTransaction_fee(String transaction_fee) {
        this.transaction_fee = transaction_fee;
    }

    public String getTransaction_size_kb() {
        return transaction_size_kb;
    }

    public void setTransaction_size_kb(String transaction_size_kb) {
        this.transaction_size_kb = transaction_size_kb;
    }

    public String getFrom_amout() {
        return from_amout;
    }

    public void setFrom_amout(String from_amout) {
        this.from_amout = from_amout;
    }

    public String getTo_amount() {
        return to_amount;
    }

    public void setTo_amount(String to_amount) {
        this.to_amount = to_amount;
    }
}
