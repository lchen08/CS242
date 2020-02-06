import java.io.*;
import java.nio.file.Paths;
import java.util.Scanner;

import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * This class is for the Lucene indexer, which takes a list of .data files from our
 * JSoup crawler and indexes the websites found by the crawler by its text and title as
 * the tokens.
<<<<<<< HEAD
 *
 * References for creating this indexer:
 * https://lucene.apache.org/core/8_4_1/core/index.html
 * http://web.cs.ucla.edu/classes/winter15/cs144/projects/lucene/index.html
 * https://www.tutorialspoint.com/lucene/lucene_indexing_process.htm
 *
=======
>>>>>>> 32c2c9c... Created and ran indexer to index Jsoup files
 * @author Lisa Chen, Nikhil Gowda, Poorvaja Sundar, Edward Zabrensky, and Jason Zellmer
 * @version 1.0
 * @since Feb 05, 2020
 */
public class Indexer {
    private IndexWriter writer;
    private final static String INDEX_DIR = "Index_Files/";
    private final static File DATA_DIR = new File("Data_Files/");
    private final static File[] DATA_FILE_LIST = DATA_DIR.listFiles();
    private final String[] JSON_KEYS = {"text", "title", "url"};
    private final String[] STOP_WORDS = {"a", "an", "and", "are", "as", "at", "be", "but",
            "by", "for", "if", "in", "into", "is", "it", "no", "not", "of", "on", "or",
            "such", "that", "the", "their", "then", "there", "these", "they", "this",
            "to", "was", "will", "with"};

    //for testing purposes
    public static void main(String[] args) throws IOException, ParseException {
        //run the indexer to create index files
        Indexer indexer = new Indexer(DATA_FILE_LIST, INDEX_DIR);
        System.out.println("Indexing complete");
    }

    /**
     * Constructs the indexer using the list of files to index and the path for the
     * resulting Lucene index files to be saved.
     * @param fileList The list of files to index
     * @param indexDirectoryPath The directory path for the resulting indexing files
     * @throws ParseException
     * @throws IOException
     */
    public Indexer(File[] fileList, String indexDirectoryPath) throws ParseException,
            IOException {
        Directory indexDirectory = FSDirectory.open(Paths.get(INDEX_DIR));
        try {
            writer = new IndexWriter(indexDirectory, new IndexWriterConfig(
                    new StandardAnalyzer(initializeStopWords())));
            indexFiles(fileList);
            closeIndexWriter();
        } catch (FileNotFoundException e) {
            System.out.println ("Input file path is incorrect");
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println ("Error with creating the index writer");
            e.printStackTrace();
        } catch (ParseException e) {
            System.out.println("Error with reading the files");
            e.printStackTrace();
        }
    }

    /**
     * Indexes a given website document, which has a text, title, and url field.
     * @param website A website object
     */
    private void indexWebsite(Document website) {
        try {
            writer.addDocument(website);
        } catch (IOException e) {
            System.out.println("Error with adding documents to index writer");
            e.printStackTrace();
        }
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
     * Indexes all the lines in a file, where each line represents the information for
     * a website.
     * @param fileList The list of files to index its contents.
     * @throws ParseException
     * @throws FileNotFoundException
     */
    private void indexFiles(File[] fileList) throws ParseException, FileNotFoundException {
        JSONParser jsonParser = new JSONParser();

        for (int i = 0; i < fileList.length; i++) {
            System.out.println("Indexing File " + i);
            Scanner scanner = new Scanner(fileList[i]);
            while (scanner.hasNext()) {
                Document doc = new Document();
                String jsonString = scanner.nextLine();
                JSONObject obj = (JSONObject) jsonParser.parse(jsonString);
                indexWebsite(createWebsiteDocument((obj)));
            }
        }
    }

    /**
     * Creates an document object representing the fields of the website, as per the
     * defined json keys: title, text, and url.
     * @param obj The object representing the content of the website in JSON format
     * @return
     */
    private Document createWebsiteDocument(JSONObject obj) {
        Document doc = new Document();
        doc.add(new TextField(JSON_KEYS[0], (String) obj.get(JSON_KEYS[0]),
                Field.Store.NO)); //text
        doc.add(new TextField(JSON_KEYS[1], (String) obj.get(JSON_KEYS[1]),
                Field.Store.YES)); //title
        doc.add(new StringField(JSON_KEYS[2], (String) obj.get(JSON_KEYS[2]),
                Field.Store.YES)); //url
        return doc;
    }

    /**
     * Closes the indexer.
     * @throws CorruptIndexException
     * @throws IOException
     */
    public void closeIndexWriter() throws CorruptIndexException, IOException {
        writer.close();
    }
}

