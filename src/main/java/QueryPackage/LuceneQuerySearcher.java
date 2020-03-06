package QueryPackage;

import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.highlight.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.nio.file.Paths;

/**
 * This class is for searching the Lucene index files for a given query.
 *
 * References for creating this indexer:
 * https://lucene.apache.org/core/8_4_1/core/index.html
 * http://web.cs.ucla.edu/classes/winter15/cs144/projects/lucene/index.html
 * https://howtodoinjava.com/lucene/lucene-search-highlight-example/
 *
 *  @author Lisa Chen, Nikhil Gowda, Poorvaja Sundar, Edward Zabrensky, and Jason Zellmer
 *  @version 1.0
 *  @since Mar 03, 2020
 */
public class LuceneQuerySearcher {
    private IndexSearcher searcher;
    private MultiFieldQueryParser parser;
    private IndexReader reader;
    private StandardAnalyzer analyzer;
    private final String INDEX_DIR = "Index_Files";
    private static final String[] JSON_KEYS = {"text", "title", "url"};
    private final String[] STOP_WORDS = {"a", "an", "and", "are", "as", "at", "be", "but",
            "by", "for", "if", "in", "into", "is", "it", "no", "not", "of", "on", "or",
            "such", "that", "the", "their", "then", "there", "these", "they", "this",
            "to", "was", "will", "with"};
    private final int MIN_FRAGMENT_LENGTH = 30;
    private final String FRAGMENT_SEPARATOR = "...";
    private final int MAX_NUM_FRAGMENTS = 5;

    /** Instantiates the query searcher. */
    public LuceneQuerySearcher() throws IOException {
        Directory indexDirectory = FSDirectory.open(Paths.get(INDEX_DIR));
        reader = DirectoryReader.open(indexDirectory);
        analyzer = new StandardAnalyzer(initializeStopWords());
        searcher = new IndexSearcher(reader);
        parser = new MultiFieldQueryParser(new String[] {JSON_KEYS[0], JSON_KEYS[1]},
                analyzer);
    }

    /**
     * Initializes the stop words for the indexer. The words chosen are words that have
     * been defined in previous versions of Lucene as common English words that are not
     * preferred in searches.
     * @return The set of stop words in CharArraySet form for Lucene.
     */
    private CharArraySet initializeStopWords() {
        int numStopWords = STOP_WORDS.length;
        CharArraySet stopSet = new CharArraySet(numStopWords,true);
        for (String word : STOP_WORDS)
            stopSet.add(word);
        return stopSet;
    }

    /**
     * Performs the search of the index files for a given query and number of results to
     * return.
     * @param queryString The query to search
     * @param numHits The number of results to return
     * @return The top results, restricted to numHits quantity
     * @throws IOException
     * @throws ParseException
     */
    public TopDocs performSearch(String queryString, int numHits)
            throws IOException, ParseException {
        Query query = parser.parse(queryString);
        return searcher.search(query, numHits);
    }

    /**
     * Retrieves the document for a given document ID.
     * @param docId The ID of the document to obtain
     * @return The document of interest
     * @throws IOException
     */
    public Document getDocument(int docId) throws IOException {
        return searcher.doc(docId);
    }

    /**
     * Retrieves the top hits for a given query and number of hits at most to return.
     * @param query The query to search
     * @param numHits The number of hits that the search should return at maximum
     * @return A matrix of results with each row as a result and each column as the fields
     * @throws IOException
     * @throws ParseException
     */
    public JSONArray retrieveTopHits(String query, int numHits) throws IOException,
            ParseException, InvalidTokenOffsetsException {
        final int FRAGMENT_LENGTH = getFragmentLength(query);

        TopDocs topDocs = performSearch(query, numHits);
        ScoreDoc[] hits = topDocs.scoreDocs;

        int numResults = hits.length;
        int numKeys = JSON_KEYS.length;
        JSONArray results = new JSONArray();

        //setup fragmenter/highlighter
        Query queryObj = parser.parse(query);
        Formatter formatter = new SimpleHTMLFormatter();
        QueryScorer scorer = new QueryScorer(queryObj);
        Highlighter highlighter = new Highlighter(formatter, scorer);
        Fragmenter fragmenter = new SimpleSpanFragmenter(scorer, FRAGMENT_LENGTH);
        highlighter.setTextFragmenter(fragmenter);

        //creates the matrix of all results and their information
        for (int hit = 0; hit < numResults; hit++) {
            int docID = hits[hit].doc;
            Document doc = getDocument(docID);
            JSONObject result = new JSONObject();

            for (int key = 0; key < numKeys; key++) {
                String text = doc.get(JSON_KEYS[key]);

                //if adding the website's body, save fragment to results
                if (key == 0) {
                    TokenStream stream = TokenSources.getAnyTokenStream(reader, docID,
                            JSON_KEYS[key], analyzer);
                    text = highlighter.getBestFragments(stream, text,
                            MAX_NUM_FRAGMENTS, FRAGMENT_SEPARATOR);
//                    String[] fragments = highlighter.getBestFragments(stream, text,
//                            MAX_NUM_FRAGMENTS);
//                    text = "";
//                    for (String fragment : fragments) {
//                        text += fragment;
//                    }
                }
//                System.out.println(text);
                result.put(JSON_KEYS[key], text);
            }
            result.put("score", hits[hit].score);
            results.add(result.toString());
        }
        return results;
    }

    /**
     * Retrieves the fragment length that is recommended for the highlighter. The fragment
     * length is based on the longest word in the query (multiplied by a multiplier) or
     * the defined minimum fragment length, whichever is longer.
     * @param query The given query
     * @return The fragment length to use for the highlighter/fragmenter
     */
    private int getFragmentLength(String query) {
        double multiplier = 5;
        int multipliedLength = (int) (getLongestStringLength(query) * multiplier);
        return (multipliedLength < MIN_FRAGMENT_LENGTH) ? MIN_FRAGMENT_LENGTH :
                multipliedLength;

    }

    /**
     * Gets the longest string length for an array of strings.
     * @param words The array of strings to check for length
     * @return The longest length for the array of strings
     */
    private int getLongestStringLength(String words) {
        String[] separated = words.split(" ");
        int max = 0;
        for (String word : separated) {
            int length = word.length();
            if (length > max)
                max = length;
        }
        return max;
    }

    /* For testing purposes */
    public static void main(String[] args) throws IOException, ParseException,
            InvalidTokenOffsetsException {
        LuceneQuerySearcher qs = new LuceneQuerySearcher();
//        String query = "UC Riverside information retrieval";
        String query = "university Riverside machine learning";
        int numHits = 10;

        JSONArray results = qs.retrieveTopHits(query, numHits);
        int numResults = results.size();
        for (int i = 0; i < numResults; i++) {
            System.out.println(results.get(i) + "\n");
        }
    }
}
