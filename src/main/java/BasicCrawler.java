/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Pattern;

import org.apache.http.Header;


import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.parser.HtmlParseData;
import edu.uci.ics.crawler4j.url.WebURL;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.common.SolrInputDocument;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

//import org.apache.solr.client.solrj;

/**
 * @author Yasser Ganjisaffar
 */
public class BasicCrawler extends WebCrawler {

    private static final Pattern IMAGE_EXTENSIONS = Pattern.compile(".*\\.(bmp|gif|jpg|png)$");
    String urlString = "http://localhost:8983/solr/mycore";
    SolrClient solr = new HttpSolrClient.Builder(urlString).build();
    private List<SolrInputDocument> documentsIndexed = new CopyOnWriteArrayList<SolrInputDocument>();

    private int NO_OF_DOCUMENT_TO_COMMIT = 1;



    /**
     * You should implement this function to specify whether the given url
     * should be crawled or not (based on your crawling logic).
     */
    @Override
    public boolean shouldVisit(Page referringPage, WebURL url) {
        String href = url.getURL().toLowerCase();
        // Ignore the url if it has an extension that matches our defined set of image extensions.
        if (IMAGE_EXTENSIONS.matcher(href).matches()) {
            return false;
        }

        // Only accept the url if it is in the "www.ics.uci.edu" domain and protocol is "http".
        boolean isBlock = href.startsWith("https://chainz.cryptoid.info/ecc/block.dws");
        boolean isTx = href.startsWith("https://chainz.cryptoid.info/ecc/tx.dws");
        boolean isCryptoBEBlock = href.startsWith("https://cryptobe.com/block");
        return href.startsWith("https://cryptobe.com/tx");
    }

    /**
     * This function is called when a page is fetched and ready to be processed
     * by your program.
     */
    @Override
    public void visit(Page page) {
        int docid = page.getWebURL().getDocid();
        String url = page.getWebURL().getURL();
        String domain = page.getWebURL().getDomain();
        String path = page.getWebURL().getPath();
        String subDomain = page.getWebURL().getSubDomain();
        String parentUrl = page.getWebURL().getParentUrl();
        String anchor = page.getWebURL().getAnchor();

        logger.debug("Docid: {}", docid);
        /*logger.info("URL: {}", url);
        logger.debug("Domain: '{}'", domain);
        logger.debug("Sub-domain: '{}'", subDomain);*/
        logger.debug("Path: '{}'", path);/*
        logger.debug("Parent page: {}", parentUrl);
        logger.debug("Anchor text: {}", anchor);
*/
        if (page.getParseData() instanceof HtmlParseData) {
            HtmlParseData htmlParseData = (HtmlParseData) page.getParseData();
            String text = htmlParseData.getText();
            String html = htmlParseData.getHtml();
            Set<WebURL> links = htmlParseData.getOutgoingUrls();
            Document doc =   Jsoup.parse(html);
            SolrInputDocument doSolrInputDocument = new SolrInputDocument();

            String content = doc.getElementsByClass("information").text();

            String array[] = content.split(" ");
            logger.debug("ARRAY:");
            logger.debug(Arrays.toString(array));
            doSolrInputDocument.setField("id", page.hashCode());
            doSolrInputDocument.setField("hash", array[1]);
            logger.debug(array[1]); // hash
            doSolrInputDocument.setField("block", array[5]);
            logger.debug(array[5]); // block
            doSolrInputDocument.setField("date", array[6]);
            logger.debug(array[6]); // date
            doSolrInputDocument.setField("time", array[7]);
            logger.debug(array[7]); // time
            //doSolrInputDocument.setField("totalIn", array[17]);
            logger.debug(array[17]); // total in
            doSolrInputDocument.setField("outputs", array[21]);
            logger.debug(array[21]); // #outputs
/*            logger.debug("Text length: {}", text.length());
            logger.debug("Html length: {}", html.length());
            logger.debug("Number of outgoing links: {}", links.size());*/

            documentsIndexed.add(doSolrInputDocument);
            try {
                solr.add(doSolrInputDocument);

                solr.commit(true, true);
            } catch(Exception e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
        }

        Header[] responseHeaders = page.getFetchResponseHeaders();
        if (responseHeaders != null) {
            //logger.debug("Response headers:");
            for (Header header : responseHeaders) {
                //logger.debug("\t{}: {}", header.getName(), header.getValue());
            }
        }


        logger.debug("=============");
    }
}
