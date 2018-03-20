import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.common.SolrInputDocument;

import java.io.IOException;

public class SolrjPopulator {

    static void populate(){
        SolrClient client = new HttpSolrClient.Builder("http://localhost:8983/solr/gettingstarted").build();

        for(int i=0;i<1000;++i) {
            SolrInputDocument doc = new SolrInputDocument();
            doc.addField("cat", "book");
            doc.addField("id", "book-" + i);
            doc.addField("name", "The Legend of the Hobbit part " + i);
            try {
                client.add(doc);
            } catch (SolrServerException | IOException e) {
                e.printStackTrace();
            }
            if(i%100==0) {
                try {
                    client.commit();  // periodically flush
                } catch (SolrServerException | IOException e) {
                    e.printStackTrace();
                }
            }
        }
        try {
            client.commit();
        } catch (SolrServerException | IOException e) {
            e.printStackTrace();
        }
    }
}
