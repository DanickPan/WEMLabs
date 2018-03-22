import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtConfig;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;

public class Controller
{
    private static SolrjPopulator solrjPop = SolrjPopulator.getInstance();
    private static final String storageFolder = "results";
    private static final String baseCrawlingUrl = "https://cryptobe.com/block/3245c98e04c8010ce5313ad58bc62130cf16ed16c8061955d95315a59dca88cf";
    private static final int numberOfCrawlers = 1;

    private static final int maxPagesToFetch = 500;

    public static void main(String[] args) throws Exception {

        //basic crawler configurations
        CrawlConfig config = new CrawlConfig();
        config.setCrawlStorageFolder(storageFolder);
        config.setUserAgentString("crawler4j/WEM/2018");
        config.setMaxConnectionsPerHost(10);
        config.setConnectionTimeout(4000);
        config.setSocketTimeout(5000);
        config.setIncludeHttpsPages(true);
        config.setPolitenessDelay(250);
        config.setMaxDepthOfCrawling(10);
        config.setMaxPagesToFetch(maxPagesToFetch);
        config.setIncludeBinaryContentInCrawling(false);
        config.setResumableCrawling(false);

        //robots configuration
        PageFetcher pageFetcher = new PageFetcher(config);
        RobotstxtConfig robotstxtConfig = new RobotstxtConfig();
        RobotstxtServer robotstxtServer = new RobotstxtServer(robotstxtConfig, pageFetcher);
        CrawlController controller = new CrawlController(config, pageFetcher, robotstxtServer);
        robotstxtConfig.setEnabled(false); //

        //base url
        controller.addSeed(baseCrawlingUrl);

        /*
         * Start the crawl. This is a blocking operation, meaning that your code
         * will reach the line after this only when crawling is finished.
         */
        controller.start(EcurrencyTransactionsCrawler.class, numberOfCrawlers);

        // Perform queries
        // List all transactions with an amount greater than 1'000'000
        SolrDocumentList allDatas = solrjPop.query("fq=from_amout:[1000000 TO *]");
        for (SolrDocument allData : allDatas) {
            System.out.println(allData);
        }

        // List all records of type transaction
        SolrDocumentList datas = solrjPop.query("type:Transaction");
        for (SolrDocument data : datas) {
            System.out.println(data);
        }
    }
}
