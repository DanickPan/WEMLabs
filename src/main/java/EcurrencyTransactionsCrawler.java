import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.parser.HtmlParseData;
import edu.uci.ics.crawler4j.url.WebURL;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.Set;
import java.util.regex.Pattern;

public class EcurrencyTransactionsCrawler extends WebCrawler
{
    private static final Pattern IMAGE_EXTENSIONS = Pattern.compile(".*\\.(bmp|gif|jpg|png)$");

    @Override
    public boolean shouldVisit(Page referringPage, WebURL url) {
        String href = url.getURL().toLowerCase();
        // Ignore the url if it has an extension that matches our defined set of image extensions.
        if (IMAGE_EXTENSIONS.matcher(href).matches()) {
            return false;
        }

        // Only accept the url if it is in the "https://cryptobe.com/block" domain and protocol is "http".
        return href.startsWith("https://cryptobe.com/block");
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

        logger.debug("Docid: {}", docid);
        logger.info("URL: {}", url);
        logger.debug("Domain: '{}'", domain);

        if (page.getParseData() instanceof HtmlParseData) {
            HtmlParseData htmlParseData = (HtmlParseData) page.getParseData();
            Set<WebURL> links = htmlParseData.getOutgoingUrls();

            logger.debug("Number of outgoing links: {}", links.size());
        }

        // ---------------------------------- jSOup section to parse the data --------------------------------------- //
        Document doc = null;
        try {
            doc = Jsoup.connect(url).get();
        } catch (IOException e) {
            e.printStackTrace();
        }

        assert doc != null;
        logger.info(doc.title());

        // Get informations html
        String page_content_infos = doc.select("p.information").text();


        String array[] = page_content_infos.split(" ");

        //create a new Block
        Block block = new Block();

        block.setHash_block(array[1]);
        block.setBlock_num(array[9]);
        block.setVersion(array[11]);
        block.setTransac_merkle_root(array[15]);
        block.setTimestamp(array[17]);
        block.setDatetime((array[18] + " " + array[19])
                .replace("(", "")
                .replace(")", ""));
        block.setDifficulty(array[21] + array[22]);
        block.setCumulative_difficulty(array[27] + array[28] + array[29]);
        block.setNonce(array[31]);
        block.setHow_much_transaction(array[33]);
        block.setValue_out(array[36]);
        block.setBlock_fee(array[39]);
        block.setAvg_coin_age(array[43]);
        block.setCoin_days_destroyed(array[47]);
        block.setCumulative_coin_days_destroyed(array[51]);

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
            trans.setTransaction_fee(transaction.selectFirst("tr > td + td").text());
            trans.setTransaction_size_kb(transaction.selectFirst("tr > td + td + td").text());

            // Get from informations
            if (transaction.selectFirst("tr > td + td + td + td").toString().contains("href")) {
                trans.setFrom_amout(transaction.selectFirst("tr > td + td + td + td > a[href]").
                        toString().split("/")[2].split("\"")[0]);
            } else {
                trans.setFrom_amout(transaction.selectFirst("tr > td + td + td + td").text());
            }
            // Get To transaction informations
            if (transaction.selectFirst("tr > td + td + td + td + td").toString().contains("href")) {
                trans.setTo_amount(transaction.select("tr > td + td + td + td + td > a[href]")
                        .toString().split("/")[2].split("\"")[0]);
            } else
                {
                trans.setTo_amount(transaction.selectFirst("tr > td + td + td + td + td").toString());
            }

            solrjPopulator.addTransaction(trans);
        }
    }
}
