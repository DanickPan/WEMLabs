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
import java.util.Arrays;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// jsoup section

/**
 * @author Yasser Ganjisaffar
 */
public class BasicCrawlerEcurrencyCoinBlock extends WebCrawler {

    private static final Pattern IMAGE_EXTENSIONS = Pattern.compile(".*\\.(bmp|gif|jpg|png)$");
    final String solrUrl = "http://localhost:8983/solr/mytestcore/";


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
        /*logger.debug("ARRAY:");
        logger.debug(Arrays.toString(array));*/
        String hash_block = array[1].toString();
        String block_num = array[9].toString();
        String version = array[11].toString();
        String transac_merkle_root = array[15].toString();
        String timestamp = array[17].toString();
        String datetime = (array[18].toString() + " " + array[19].toString())
                          .replace("(", "")
                          .replace(")", "");
        String difficulty = array[21].toString() + array[22].toString();
        String cumulative_difficulty = array[27].toString() + array[28].toString() + array[29].toString();
        String nonce = array[31].toString();
        String how_much_transaction = array[33].toString();
        String value_out = array[36].toString();
        String block_fee = array[39].toString();
        String avg_coin_age = array[43].toString();
        String coin_days_destroyed = array[47].toString();
        String cumulative_coin_days_destroyed = array[51].toString();

        SolrInputDocument solr_doc = new SolrInputDocument();
        solr_doc.setField("hash", hash_block);
        solr_doc.setField("block_num", block_num);
        solr_doc.setField("version", version);
        solr_doc.setField("transaction_merkle_root", transac_merkle_root);
        solr_doc.setField("timestamps", timestamp);
        solr_doc.setField("datetime", datetime);
        solr_doc.setField("difficulty", difficulty);
        solr_doc.setField("cumulative_difficulty", cumulative_difficulty);
        solr_doc.setField("nonce", nonce);
        solr_doc.setField("how_much_transaction", how_much_transaction);
        solr_doc.setField("value_out", cumulative_difficulty);
        solr_doc.setField("block_fee", block_fee);
        solr_doc.setField("avg_coin_out", avg_coin_age);
        solr_doc.setField("coin_days_destroyed", coin_days_destroyed);
        solr_doc.setField("cumulative_coin_days_destroyed", cumulative_coin_days_destroyed);




        try {
            client.add(solr_doc);
            client.commit();
        } catch (SolrServerException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // Get the transactions
        Elements transactions = doc.select("tr");

        // logger.info("DATA INFO: " + transactions.toString());

        logger.info("\n\n============================== Block Informations ======================================\n");
        logger.info("\tHash code: " + hash_block);
        logger.info("\tBlock number: " + block_num);
        logger.info("\tVersion: " + version);
        logger.info("\tTransaction merkle root: " + transac_merkle_root);
        logger.info("\tTimestamp: " + timestamp);
        logger.info("\tDatetime: " + datetime);
        logger.info("\tDifficulty: " + difficulty);
        logger.info("\tCumulative Difficulty: " + cumulative_difficulty);
        logger.info("\tNonce: " + nonce);
        logger.info("\tHow many transaction for the block: " + how_much_transaction);
        logger.info("\tValue out: " + value_out);
        logger.info("\tTransaction fee: " + block_fee);
        logger.info("\tAverage coin age: " + avg_coin_age);
        logger.info("\tAverage coin days destroyed: " + coin_days_destroyed);
        logger.info("\tCumulative average coin days destroyed: " + cumulative_coin_days_destroyed);
        logger.info("============================= Block transactions start =================================");
        int transaction_co = 0;
        for (Element transaction : transactions) {
            if (transaction_co == 0) {
                transaction_co++;
                continue;
            }
            // Get transaction ID
            String trans_id_split = transaction.selectFirst("tr > td > a[href]").toString();
            // logger.info("TRANS SPLIT ID: " + trans_id_split);
            String transaction_id = trans_id_split.split("/")[2].split("\"")[0];
            // Get transaction fee
            String transaction_fee = transaction.selectFirst("tr > td + td").text().toString();
            // Get size
            String transaction_size_kb = transaction.selectFirst("tr > td + td + td").text().toString();

            String from_amout = "";
            String to_amount = "";
            String to_amount_txt = "";
            // Get from informations
            //logger.info("TRANSACTION: " + transaction.toString());
            // logger.info("FROM AMOUNT: " + transaction.select("tr > td + td + td + td").toString());
            if (transaction.selectFirst("tr > td + td + td + td").toString().contains("href")) {
                from_amout = transaction.selectFirst("tr > td + td + td + td > a[href]").toString();
                from_amout = from_amout.split("/")[2].split("\"")[0].toString();
            } else {
                from_amout = transaction.selectFirst("tr > td + td + td + td").text().toString();
            }
            // Get To transaction informations
            // Store the values in list
            ArrayList<Float> transaction_to_values = new ArrayList<>();
            ArrayList<String> transaction_to_hash = new ArrayList<>();
            // logger.info("TO AMOUNT: " + transaction.selectFirst("tr > td + td + td + td + td").toString());
            if (transaction.selectFirst("tr > td + td + td + td + td").toString().contains("href")) {
                to_amount = transaction.select("tr > td + td + td + td + td > a[href]").toString();
                to_amount_txt = transaction.selectFirst("tr > td + td + td + td + td").text().toString();
                logger.info("AMOUNT TO TXT: " + transaction.selectFirst("tr > td + td + td + td + td").text().toString());
                /*
                // Create regex to get in depth informations related to transactions
                Pattern float_pattern = Pattern.compile("([+-]?\\d*\\.?\\d*?)$");
                Pattern hash_pattern = Pattern.compile("[a-z0-9A-Z]{34}$");
                // Find sub-string that correspond to the defined pattern
                Matcher float_matcher = float_pattern.matcher(to_amount_txt);
                Matcher hash_matcher = hash_pattern.matcher(to_amount_txt);

                // Store float values
                while(float_matcher.find()){
                    logger.info("FLOAT: " + Float.parseFloat(float_matcher.group()));
                    transaction_to_values.add(Float.parseFloat(float_matcher.group()));
                }
                // Store the found hashes
                while(hash_matcher.find()){
                    transaction_to_hash.add(hash_matcher.group());
                }
                */
                // logger.info("\n\nTO AMOUNT TXT: " + to_amount_txt + "\n");
                // logger.info("\n\nTO AMOUNT: " + to_amount + "\n");
                to_amount = to_amount.split("/")[2].split("\"")[0].toString();
            } else {
                to_amount = transaction.selectFirst("tr > td + td + td + td + td").toString();
            }
            logger.info("\t\tTransaction id: " + transaction_id);
            logger.info("\t\tTransaction fee: " + transaction_fee);
            logger.info("\t\tTransaction size kb: " + transaction_size_kb);
            logger.info("\t\tTransaction From: " + from_amout);
            logger.info("\t\tTransaction To: hashes:\t" + transaction_to_hash.toString() + "\tvalues: " +
                        transaction_to_values.toString()+ "\n\n");
        }
        logger.info("============================= Block transactions end =================================");
        logger.info("\n=============================== End Block Information ======================================\n");
    }
    
}
