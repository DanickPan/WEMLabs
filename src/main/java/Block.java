public class Block
{
    private String hash_block;
    private String block_num;
    private String version;
    private String transac_merkle_root;
    private String timestamp;
    private String datetime;
    private String difficulty;
    private String cumulative_difficulty;
    private String nonce;
    private String how_much_transaction;
    private String value_out;
    private String block_fee;
    private String avg_coin_age;
    private String coin_days_destroyed;
    private String cumulative_coin_days_destroyed;

    public Block()
    {

    }

    public String getHash_block() {
        return hash_block;
    }

    public void setHash_block(String hash_block) {
        this.hash_block = hash_block;
    }

    public String getBlock_num() {
        return block_num;
    }

    public void setBlock_num(String block_num) {
        this.block_num = block_num;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getTransac_merkle_root() {
        return transac_merkle_root;
    }

    public void setTransac_merkle_root(String transac_merkle_root) {
        this.transac_merkle_root = transac_merkle_root;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getDatetime() {
        return datetime;
    }

    public void setDatetime(String datetime) {
        this.datetime = datetime;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public String getCumulative_difficulty() {
        return cumulative_difficulty;
    }

    public void setCumulative_difficulty(String cumulative_difficulty) {
        this.cumulative_difficulty = cumulative_difficulty;
    }

    public String getNonce() {
        return nonce;
    }

    public void setNonce(String nonce) {
        this.nonce = nonce;
    }

    public String getHow_much_transaction() {
        return how_much_transaction;
    }

    public void setHow_much_transaction(String how_much_transaction) {
        this.how_much_transaction = how_much_transaction;
    }

    public String getValue_out() {
        return value_out;
    }

    public void setValue_out(String value_out) {
        this.value_out = value_out;
    }

    public String getBlock_fee() {
        return block_fee;
    }

    public void setBlock_fee(String block_fee) {
        this.block_fee = block_fee;
    }

    public String getAvg_coin_age() {
        return avg_coin_age;
    }

    public void setAvg_coin_age(String avg_coin_age) {
        this.avg_coin_age = avg_coin_age;
    }

    public String getCoin_days_destroyed() {
        return coin_days_destroyed;
    }

    public void setCoin_days_destroyed(String coin_days_destroyed) {
        this.coin_days_destroyed = coin_days_destroyed;
    }

    public String getCumulative_coin_days_destroyed() {
        return cumulative_coin_days_destroyed;
    }

    public void setCumulative_coin_days_destroyed(String cumulative_coin_days_destroyed) {
        this.cumulative_coin_days_destroyed = cumulative_coin_days_destroyed;
    }
}
