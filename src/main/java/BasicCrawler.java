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

import java.util.Set;
import java.util.regex.Pattern;

import org.apache.http.Header;

import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.parser.HtmlParseData;
import edu.uci.ics.crawler4j.url.WebURL;

// jsoup section

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;

import java.io.IOException;

/**
 * @author Yasser Ganjisaffar
 */
public class BasicCrawler extends WebCrawler {

    private static final Pattern IMAGE_EXTENSIONS = Pattern.compile(".*\\.(bmp|gif|jpg|png)$");


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
        return href.startsWith("https://cryptobe.com/block/");
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
        /*logger.debug("Sub-domain: '{}'", subDomain);
        logger.debug("Path: '{}'", path);
        logger.debug("Parent page: {}", parentUrl);
        logger.debug("Anchor text: {}", anchor);*/

        if (page.getParseData() instanceof HtmlParseData) {
            HtmlParseData htmlParseData = (HtmlParseData) page.getParseData();
            String text = htmlParseData.getText();
            String html = htmlParseData.getHtml();
            Set<WebURL> links = htmlParseData.getOutgoingUrls();

            // logger.debug("Text length: {}", text.length());
            // logger.debug("Html length: {}", html.length());
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

        logger.info(doc.title());

        // Get block number
        doc.select("h2[style='margin-top:0']").select("span").remove();
        String block_num = doc.selectFirst("h2[style='margin-top:0']").unwrap().toString();
        block_num = block_num.substring(block_num.length() -1, block_num.length());
        // Get hash
        String hash = doc.selectFirst("td > code").unwrap().toString();
        // Get datetime
        String datetime = doc.selectFirst("td:matches((\\d{4})-(\\d{2})-(\\d{2}) (\\d{2}):(\\d{2}):(\\d{2}))")
                          .unwrap().toString();
        // Get Transaction number to check
        doc.selectFirst("td:contains(Transactions) + td").selectFirst("span").remove();
        String transaction_count = doc.selectFirst("td:contains(Transactions) + td").unwrap().toString();
        // Get value out
        doc.selectFirst("td:contains(Value Out) + td.amount").selectFirst("small").remove();
        String value_out = doc.selectFirst("td:contains(Value Out) + td.amount").unwrap().toString().replace(",", "");
        // Get difficulty
        String difficulty = doc.selectFirst("td:contains(Difficulty) + td").unwrap().toString();
        // Get Outstanding
        doc.selectFirst("td:contains(Outstanding) + td.amount").selectFirst("small").remove();
        String outstanding = doc.selectFirst("td:contains(Outstanding) + td.amount").unwrap().toString().replace(",", "");
        // Get created
        doc.selectFirst("td:has(b) + td").selectFirst("small").remove();
        String created = doc.selectFirst("td:has(b) + td").unwrap().toString().replace(",", "");

        // Start to parse all transaction for a given block
        // Elements block_transactions = doc.select("div.active > tbody > tr[tx-id]");
        Elements block_transactions = doc.select("div");

        logger.info("DATA INFO: " + block_transactions.toString());
        for (Element transaction : block_transactions) {
            String transaction_hash = transaction.selectFirst("a[href] > code").toString();
            logger.info("Transaction hash: " + transaction_hash);
        }

        logger.info("Block number: " + block_num);
        logger.info("Hash code: " + hash);
        logger.info("Datetime: " + datetime);
        logger.info("Transaction counter: " + transaction_count);
        logger.info("Value out: " + value_out);
        logger.info("Difficulty: " + difficulty);
        logger.info("Outstanding value: " + outstanding);
        logger.info("Created on : " + created);

        logger.debug("======================================================================\n\n");
    }
    
}
