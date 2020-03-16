package GraphPackage;

import org.jfree.ui.RefineryUtilities;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Random;

/**
 * This class is for testing purposes to quickly create a graph for given files.
 *  @author Lisa Chen, Nikhil Gowda, Poorvaja Sundar, Edward Zabrensky, Jason Zellmer
 *  @version 1.0
 *  @since Mar 03, 2020
 */
public class GrapherTester {
    private static final String APP_TITLE = "CS242 - Lucene Runtime Graph";
    private static final String GRAPH_TITLE = "Lucene Document Completion Times";
    private static final String LUCENE_TIME_FILENAME = "lucenetimes.txt";
    private static final String[] LINE_TITLES = {"Lucene Indexer", "Hadoop Indexer"};

    public static void main(String[] args) throws FileNotFoundException {

        //test graphing one dataset
        IndexTimeGrapher chart = new IndexTimeGrapher(APP_TITLE, GRAPH_TITLE,
                new File(LUCENE_TIME_FILENAME), LINE_TITLES[0]);

//        createDummy();
        //test graphing 2 datasets
//        IndexTimeGrapher chart = new IndexTimeGrapher(APP_TITLE, GRAPH_TITLE,
//                new File(LUCENE_TIME_FILENAME), new File("dummy.txt"),
//                LINE_TITLES);

        chart.pack( );
        RefineryUtilities.centerFrameOnScreen( chart );
        chart.setVisible( true );
    }

    /**
     * Creates dummy data to test plotting two datasets on the same graph.
     * @throws FileNotFoundException
     */
    private static void createDummy() throws FileNotFoundException {
        Random rn = new Random();
        int[] increments = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2,
                2, 2, 2, 2, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 15, 20};
        int firstNum = 100;
        PrintWriter writer = new PrintWriter("dummy.txt");
        for (int line = 0; line < 188640; line++) {
            writer.println(firstNum);
            if (line < 80000 || line > 150000) {
                int increment = increments[rn.nextInt(increments.length)];
                firstNum = firstNum + increment;
            }
        }
        writer.close();
    }
}