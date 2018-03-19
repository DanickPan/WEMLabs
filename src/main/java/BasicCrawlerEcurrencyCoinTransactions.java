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


import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.parser.HtmlParseData;
import edu.uci.ics.crawler4j.url.WebURL;
import org.apache.http.Header;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Yasser Ganjisaffar
 */
    public class BasicCrawlerEcurrencyCoinTransactions extends WebCrawler {

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
            //return href.startsWith("https://cryptobe.com/block");
            return href.startsWith("https://cryptobe.com/tx");
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

            logger.info(doc.title());

            // Get informations html
            String page_content_infos = doc.select("p.information").text().toString();
            //String content = doc.getElementsByClass("information").text();


            String array[] = page_content_infos.split(" ");
            // logger.debug("ARRAY:");
            // logger.debug(Arrays.toString(array));

            String hash = array[1].toString();
            String datetime = (array[6].toString() + " " + array[7]).replace("(","")
                              .replace(")", "");
            String num_of_input = array[11].toString();
            String total_in = array[17].toString();
            String num_of_output = array[21].toString();
            String total_out = array[27].toString();
            String size = array[29].toString() + " " + array[30].toString();
            String transaction_fee = array[32].toString();

            // Get the transactions
            Elements html_tables = doc.select("table");

            // logger.info("DATA INFO: " + html_tables.toString());

            logger.info("\n\n============================== Transaction Informations ======================================\n");
            logger.info("\tHash code: " + hash);
            logger.info("\tDatetime: " + datetime);
            logger.info("\tNumber of Input: " + num_of_input);
            logger.info("\tTotal In: " + total_in);
            logger.info("\tNumber of Output: " + num_of_output);
            logger.info("\tTotal Out: " + total_out);
            logger.info("\tSize: " + size);
            logger.info("\tTransaction fee: " + transaction_fee);
            // Get all tables content
            for (Element table : html_tables) {

                // Iterate on input and output table
                int transaction_co = 0;
                Elements trs = table.select("tr");

                for (Element tr : trs) {
                    if (transaction_co == 0) {
                        transaction_co++;
                        continue;
                    }
                    String idx = tr.select("tr > td > a").text().toString();
                    String previous_output = tr.selectFirst("tr > td + td > a[href]").toString().split("\"")[1];
                    if (previous_output.contains("/")) {
                        previous_output = tr.selectFirst("tr > td + td > a[href]").toString().split("\"")[1].split("/")[2];
                    }
                    String amount = tr.selectFirst("tr > td + td + td").text().toString();
                    String from_address = tr.selectFirst("tr > td + td + td + td > a").text();
                    logger.info("============================== Transaction =========================================");
                    logger.info("\t\tTable index: " + idx);
                    logger.info("\t\tTable Previous output: " + previous_output);
                    logger.info("\t\tTable amount: " + amount);
                    logger.info("\t\tTable From Address: " + from_address);
                    logger.info("============================== End Transaction =====================================");

                }
            }
            logger.info("\n=============================== End Transaction Information ======================================\n");
    }
}
