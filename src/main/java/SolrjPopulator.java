import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;

import java.io.IOException;

public class SolrjPopulator
{
    private SolrClient client;

    private SolrjPopulator()
    {
        //client !!Warning : core must be created !!
        String solrUrl = "http://localhost:8983/solr/core3/";
        client = new HttpSolrClient.Builder(solrUrl).withConnectionTimeout(10000).withSocketTimeout(60000).build();
    }

    //unique instance
    private static SolrjPopulator instance = new SolrjPopulator();

    //access to instance
    public static SolrjPopulator getInstance()
    {   return instance;
    }


    public void addTransaction(Transaction trans)
    {
        //add transaction to client
        SolrInputDocument solr_doc = new SolrInputDocument();
        solr_doc.setField("type","Transaction");
        solr_doc.setField("hashBlock",trans.getHashBlock());
        solr_doc.setField("trans_id_split",trans.getTrans_id_split());
        solr_doc.setField("transaction_id",trans.getTransaction_id());
        solr_doc.setField("transaction_fee",trans.getTransaction_fee());
        solr_doc.setField("transaction_size_kb",trans.getTransaction_size_kb());
        solr_doc.setField("from_amout",trans.getFrom_amout());
        solr_doc.setField("to_amount",trans.getTo_amount());

        try {
            client.add(solr_doc);
        } catch (SolrServerException | IOException e) {
            e.printStackTrace();
        }

        commit();

    }
    public void addBlock(Block block)
    {
        //add block to client
        SolrInputDocument solr_doc = new SolrInputDocument();
        solr_doc.setField("type","Block");
        solr_doc.setField("hash", block.getHash_block());
        solr_doc.setField("block_num", block.getBlock_num());
        solr_doc.setField("version", block.getVersion());
        solr_doc.setField("transaction_merkle_root", block.getTransac_merkle_root());
        solr_doc.setField("timestamps", block.getTimestamp());
        solr_doc.setField("datetime", block.getDatetime());
        solr_doc.setField("difficulty", block.getDifficulty());
        solr_doc.setField("cumulative_difficulty", block.getCumulative_difficulty());
        solr_doc.setField("nonce", block.getNonce());
        solr_doc.setField("how_much_transaction", block.getHow_much_transaction());
        solr_doc.setField("value_out", block.getValue_out());
        solr_doc.setField("block_fee", block.getBlock_fee());
        solr_doc.setField("avg_coin_out", block.getAvg_coin_age());
        solr_doc.setField("coin_days_destroyed", block.getCoin_days_destroyed());
        solr_doc.setField("cumulative_coin_days_destroyed", block.getCumulative_coin_days_destroyed());

        try {
            client.add(solr_doc);
        } catch (SolrServerException | IOException e) {
            e.printStackTrace();
        }

        commit();
    }

    private void commit()
    {
        //apply client changes
        try {
            client.commit();
        } catch (SolrServerException | IOException e) {
            e.printStackTrace();
        }
    }
    public SolrDocumentList query(String query)
    {
        //sends a query
        final SolrQuery params = new SolrQuery(query);

        QueryResponse response = null;
        try {
            response = client.query(params);
        } catch (SolrServerException | IOException e) {
            e.printStackTrace();
        }
        assert response != null;
        return response.getResults();
    }
}
