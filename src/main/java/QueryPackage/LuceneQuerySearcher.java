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
import java.util.InputMismatchException;
import java.util.Scanner;

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
    @SuppressWarnings({"deprecation", "unchecked"})
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
                }
//                System.out.println(text);
                result.put(JSON_KEYS[key], text);
            }
            result.put("score", hits[hit].score);
            results.add(result);
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
        int numHits = 0;
        String query = "";
        int argsLength = args.length;

        //get input from command line: format required [numHits] [query string]
        for (int i = 0; i < argsLength; i++) {
            if (i==0) {
                try {
                    numHits = Integer.parseInt(args[i]);
                }
                catch(NumberFormatException e) {
                    System.out.println("The first input should be the " +
                            "number hits to display for the query. Please try again.\n");
                    i = argsLength; //end loop early
                    numHits = retrieveNumHits();
                    query = retrieveQuery();

                }
            }
            else
                query += args[i] + " ";
        }

        if(argsLength == 0) {
            numHits = retrieveNumHits();
            query = retrieveQuery();
        }

        //return empty array for bad input
        if (numHits <= 0)
            System.out.println("No results since requested " + numHits + " results.");
        else {
            JSONArray results = qs.retrieveTopHits(query, numHits);
            int numResults = results.size();

            //retrieve the JSON outputs for the query
            for (int i = 0; i < numResults; i++) {
                System.out.println(results.get(i) + "\n");
                JSONObject test = (JSONObject) results.get(i);
            }
        }

    }

    /**
     * Retrieves the number of hits to display from the user.
     * @return The number of hits to display for a given query.
     */
    private static int retrieveNumHits() {
        int result = -1;
        System.out.print("Please input the number of hits you wish to retrieve: ");
        try {
            Scanner s = new Scanner(System.in);
            result = s.nextInt();
            if (result < 0)
                throw new InputMismatchException();
        }
        catch(InputMismatchException e) {
            System.out.println("The input was not a nonnegative integer. Please try " +
                    "again.\n");
            return retrieveNumHits();
        }
        return result;
    }

    /**
     * Retrieves the query from the user.
     * @return The user's query string
     */
    private static String retrieveQuery() {
        System.out.print("Please input the query as one line: ");
        Scanner s = new Scanner(System.in);
        String result = s.nextLine();
        System.out.println("The query string: " + result);
        return result;
    }
}