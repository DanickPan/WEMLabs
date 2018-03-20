import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.parser.HtmlParseData;
import edu.uci.ics.crawler4j.url.WebURL;
import org.apache.http.Header;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.common.SolrInputDocument;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;
import java.util.regex.Pattern;

public class EcurrencyTransactionsCrawler extends WebCrawler
{
    private static final Pattern IMAGE_EXTENSIONS = Pattern.compile(".*\\.(bmp|gif|jpg|png)$");
    final String solrUrl = "http://localhost:8983/solr/mytestcore/";

    @Override
    public boolean shouldVisit(Page referringPage, WebURL url) {
        String href = url.getURL().toLowerCase();
        // Ignore the url if it has an extension that matches our defined set of image extensions.
        if (IMAGE_EXTENSIONS.matcher(href).matches()) {
            return false;
        }

        // Only accept the url if it is in the "www.ics.uci.edu" domain and protocol is "http".
        return href.startsWith("https://cryptobe.com/block");
        //return href.startsWith("https://cryptobe.com/tx");
    }

    /**
     * This function is called when a page is fetched and ready to be processed
     * by your program.
     */
    @Override
    public void visit(Page page) {

        // We're probably remove this section because we're using jSoup
        int docid = page.getWebURL().getDocid();
        String url = page.getWebURL().getURL();
        String domain = page.getWebURL().getDomain();
        String path = page.getWebURL().getPath();
        String subDomain = page.getWebURL().getSubDomain();
        String parentUrl = page.getWebURL().getParentUrl();
        String anchor = page.getWebURL().getAnchor();

        logger.debug("Docid: {}", docid);
        logger.info("URL: {}", url);
        logger.debug("Domain: '{}'", domain);

        if (page.getParseData() instanceof HtmlParseData) {
            HtmlParseData htmlParseData = (HtmlParseData) page.getParseData();
            String text = htmlParseData.getText();
            String html = htmlParseData.getHtml();
            Set<WebURL> links = htmlParseData.getOutgoingUrls();

            logger.debug("Number of outgoing links: {}", links.size());
        }

        Header[] responseHeaders = page.getFetchResponseHeaders();
        if (responseHeaders != null) {
            /*logger.debug("Response headers:");
            for (Header header : responseHeaders) {
                logger.debug("\t{}: {}", header.getName(), header.getValue());
            }*/
        }

        // ---------------------------------- jSOup section to parse the data --------------------------------------- //
        Document doc = null;
        try {
            doc = Jsoup.connect(url).get();
        } catch (IOException e) {
            e.printStackTrace();
        }

        SolrClient client = new HttpSolrClient.Builder(solrUrl).withConnectionTimeout(10000).withSocketTimeout(60000).build();


        logger.info(doc.title());

        // Get informations html
        String page_content_infos = doc.select("p.information").text().toString();
        //String content = doc.getElementsByClass("information").text();


        String array[] = page_content_infos.split(" ");

        //create a new Block
        Block block = new Block();

        block.setHash_block(array[1].toString());
        block.setBlock_num(array[9].toString());
        block.setVersion(array[11].toString());
        block.setTransac_merkle_root(array[15].toString());
        block.setTimestamp(array[17].toString());
        block.setDatetime((array[18].toString() + " " + array[19].toString())
                .replace("(", "")
                .replace(")", ""));
        block.setDifficulty(array[21].toString() + array[22].toString());
        block.setCumulative_difficulty(array[27].toString() + array[28].toString() + array[29].toString());
        block.setNonce(array[31].toString());
        block.setHow_much_transaction(array[33].toString());
        block.setValue_out(array[36].toString());
        block.setBlock_fee(array[39].toString());
        block.setAvg_coin_age(array[43].toString());
        block.setCoin_days_destroyed(array[47].toString());
        block.setCumulative_coin_days_destroyed(array[51].toString());

        //get client instance and add block
        SolrjPopulator solrjPopulator = SolrjPopulator.getInstance();
        solrjPopulator.addBlock(block);

        // Get the transactions
        Elements transactions = doc.select("tr");

        int transaction_co = 0;
        for (Element transaction : transactions) {
            if (transaction_co == 0) {
                transaction_co++;
                continue;
            }
            // Get transaction ID
            Transaction trans = new Transaction(block.getHash_block());
            trans.setTrans_id_split(transaction.selectFirst("tr > td > a[href]").toString());
            trans.setTransaction_id(trans.getTrans_id_split().split("/")[2].split("\"")[0]);
            trans.setTransaction_fee(transaction.selectFirst("tr > td + td").text().toString());
            trans.setTransaction_size_kb(transaction.selectFirst("tr > td + td + td").text().toString());

            // Get from informations
            if (transaction.selectFirst("tr > td + td + td + td").toString().contains("href")) {
                trans.setFrom_amout(transaction.selectFirst("tr > td + td + td + td > a[href]").
                        toString().split("/")[2].split("\"")[0].toString());
            } else {
                trans.setFrom_amout(transaction.selectFirst("tr > td + td + td + td").text().toString());
            }
            // Get To transaction informations
            if (transaction.selectFirst("tr > td + td + td + td + td").toString().contains("href")) {
                trans.setTo_amount(transaction.select("tr > td + td + td + td + td > a[href]")
                        .toString().split("/")[2].split("\"")[0].toString());
            } else
                {
                trans.setTo_amount(transaction.selectFirst("tr > td + td + td + td + td").toString());
            }

            solrjPopulator.addTransaction(trans);
        }
    }
}
