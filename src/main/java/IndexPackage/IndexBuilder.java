package IndexPackage;

import GraphPackage.IndexTimeGrapher;
import org.jfree.ui.RefineryUtilities;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

/**
 * This class is to run and test the Lucene Indexer. It creates a line graph displaying
 * the completion times for each of the documents it indexes from a given set of data
 * files. The graph is from JFreeChart.
 *
 *  @author Lisa Chen, Nikhil Gowda, Poorvaja Sundar, Edward Zabrensky, Jason Zellmer
 *  @version 1.1
 *  @since Mar 03, 2020
 */
public class IndexBuilder {
    private static final String CURRENT_DIR = System.getProperty("user.dir");
    private static final File DEFAULT_DATA_DIR = new File(CURRENT_DIR +
            "/Data_Files/");
    private static final File[] DEFAULT_DATA_FILE_LIST = DEFAULT_DATA_DIR.listFiles();
    private static final String INDEX_DIR = "Index_Files";
    private static final String APP_TITLE = "CS242 - Lucene Runtime Graph";
    private static final String GRAPH_TITLE = "Document Completion Times";
    private static final String LUCENE_TIME_FILENAME = "lucenetimes.txt";
    private static final String LINE_TITLE = "Lucene Indexer";

    /**
     * Runs the program with either no given inputs (default input directory) or the
     * path to the directory for the data files.
     * @param args Only accepts no inputs (default directory) or 1 input (given directory)
     * @throws IOException
     * @throws ParseException
     */
    public static void main(String[] args) throws IOException, ParseException {
        ArrayList<Long> indexingRuntimes;
        if (args.length > 1)
            throw new RuntimeException("Only accepts at most one input for the directory "
                    + "of the data files for indexing. The indexer only uses standard " +
                    "analysis per design.");
        else if (args.length == 0) {
            if (!isValidDir(DEFAULT_DATA_DIR)) {
                throw new FileNotFoundException("Directory \"Data_Files\" was not found "
                        + "in directory " + CURRENT_DIR + ". Please locate this data " +
                        "folder or specify the directory to the files requiring " +
                        "indexing.");
            }
            indexingRuntimes = runIndexer();
        }
        else {
            File dataDir = new File(args[0]);

            if (!isValidDir(dataDir)) {
                throw new FileNotFoundException("Directory " + args[0] + " is invalid. " +
                        "Verify that the input is the full directory.");
            }
            indexingRuntimes = runIndexer(dataDir.listFiles());
        }
        if (!(indexingRuntimes == null))
            saveDocTimes(indexingRuntimes);
        createIndexerRuntimeGraph();
    }

    /**
     * Creates a graph displaying the completion times for indexing all the documents
     * with a given input list of runtimes. The file to create the graph was already
     * generated from a different part of the code.
     */
    private static void createIndexerRuntimeGraph()
            throws FileNotFoundException {

        IndexTimeGrapher chart = new IndexTimeGrapher(APP_TITLE, GRAPH_TITLE,
                new File(LUCENE_TIME_FILENAME), LINE_TITLE);
        chart.pack( );
        RefineryUtilities.centerFrameOnScreen( chart );
        chart.setVisible( true );
    }

    /**
     * Verifies that the given directory exists.
     * @param directory The given directory to check for existence
     * @return
     */
    private static boolean isValidDir(File directory) { return directory.exists(); }

    /**
     * Runs the Lucene indexer using the default directory for the data files to be
     * indexed. Runtime is recorded for each document in the file that are indexed.
     * @return The amount of time it takes to index each document.
     * @throws IOException
     * @throws ParseException
     */
    public static ArrayList<Long> runIndexer() throws IOException, ParseException {
        return runIndexer(DEFAULT_DATA_FILE_LIST);
    }

    /**
     * Runs the Lucene indexer with a given list of files to index. Runtime is recorded
     * for each document in the file that are indexed.
     * @param fileList Directory to the files to index
     * @return The amount of time it takes to index each document.
     * @throws IOException
     * @throws ParseException
     */
    public static ArrayList<Long> runIndexer(File[] fileList) throws IOException,
            ParseException {
        if (isEmptyDirectory(fileList)) {
            System.out.println("Data folder is empty. No files were indexed");
            return null;
        }

        System.out.println("Starting Index. Please wait.");
        Indexer indexer = new Indexer(fileList, INDEX_DIR);
        System.out.println("Indexing complete. Index files are saved in the directory: "
                + CURRENT_DIR + "\\" + INDEX_DIR);
        return indexer.getDocTimes();
    }

    public static void saveDocTimes(ArrayList<Long> timeList) throws FileNotFoundException {
        PrintWriter writer = new PrintWriter(new File(LUCENE_TIME_FILENAME));
        for (Long time : timeList) {
            writer.println(time);
        }
        writer.close();
    }

    /**
     * Checks if a given directory folder is empty (has no files).
     * @param fileList Directory to a list of a files, if any
     * @return True if there are files; false otherwise.
     */
    private static boolean isEmptyDirectory(File[] fileList) {
        return fileList.length == 0;
    }
}