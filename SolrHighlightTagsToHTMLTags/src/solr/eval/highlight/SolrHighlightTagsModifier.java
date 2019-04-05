package solr.eval.highlight;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.params.ModifiableSolrParams;

public class SolrHighlightTagsModifier {

	public static void main(String[] args) throws SolrServerException, IOException {

		String baseSolrUrl = "http://localhost:8983/solr/techproducts/";
		HttpSolrClient solrClient = new HttpSolrClient.Builder(baseSolrUrl).build();

		SolrQuery query = new SolrQuery();
		query.setQuery("epic");
		query.setHighlight(true);
		query.addHighlightField("name");
		query.addHighlightField("manu");
		query.setHighlightFragsize(10000);

		/*
		 * ModifiableSolrParams params = new ModifiableSolrParams(); params.add("q",
		 * "epic"); params.add("hl", "true"); params.add("hl.fl", "name");
		 */

		QueryResponse response = solrClient.query(query);
		SolrDocumentList results = response.getResults();
		Map<String, Map<String, List<String>>> highlightResponse = response.getHighlighting();

		System.out.println("highlight resp --> \n" + highlightResponse);

		readHighlightResponse(results, highlightResponse);

	}

	private static void readHighlightResponse(SolrDocumentList results,
			Map<String, Map<String, List<String>>> highlightResponse) {

		for (SolrDocument doc : results) {
			if (doc.containsKey("id") && highlightResponse.containsKey(doc.get("id"))) {
				Map<String, List<String>> text = highlightResponse.get(doc.get("id"));
				System.out.println("Map of highlight field:value --->" + text);

				for (Entry<String, List<String>> entry : text.entrySet()) {
					List<String> texts = entry.getValue();
					for (String individualText : texts) {
						System.out.println("Value of each highlight -- " + individualText);
						// calling method to transform the tags
						transformTags(individualText);
					}
				}
			}
		}

	}

	private static void transformTags(String individualText) {
		int emCount = StringUtils.countMatches(individualText, "<em>");
		System.out.println("count : " + emCount);
		String preEMTag = "<em>";
		String postEMTag = "</em>";
		String preHrefTag = "<a id = \"%s\" href = \"#%s\">";
		String postHrefTag = "</a>";
		individualText = individualText.replaceAll(postEMTag, postHrefTag);

		for (int i = 0; i <= emCount; i++) {
			String formattedPreHrefString = String.format(preHrefTag, i, i + 1);
			System.out.println("href ------ " + formattedPreHrefString);
			individualText = individualText.replaceFirst(preEMTag, formattedPreHrefString);
		}
		System.out.println("\n highlight text transformed - chaining of tags:" + individualText);
	}

}
