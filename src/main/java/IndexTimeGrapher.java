import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;

import java.util.ArrayList;

/**
 * This class is to graph the runtimes (completion times) for each document in a line
 * graph using JFreeChart.
 *  @author Lisa Chen, Nikhil Gowda, Poorvaja Sundar, Edward Zabrensky, Jason Zellmer
 *  @version 1.0
 *  @since Feb 06, 2020
 */
public class IndexTimeGrapher extends ApplicationFrame {
    private final String X_AXIS = "Document Number";
    private final String Y_AXIS = "Run Time (ms)";
    private final String COL_KEY = "milliseconds";
    private final int CHART_WIDTH = 560;
    private final int CHART_HEIGHT = 367;

    /**
     * Constructs the grapher object with the application title, the chart title, and the
     * list of completion times for all the documents
     * @param appTitle The application title
     * @param chartTitle The title of the chart/graph
     * @param docTimes The list of completion times for each document
     */
    public IndexTimeGrapher(String appTitle, String chartTitle, ArrayList<Long> docTimes) {
        super(appTitle);
        createChart(chartTitle, docTimes);
    }

    /**
     * Creates the chart with the given chart title and lsit of completion times for
     * the documents.
     * @param chartTitle The title of the chart/graph
     * @param docTimes The list of completion times for each document
     */
    private void createChart(String chartTitle, ArrayList<Long> docTimes) {
        JFreeChart lineChart = ChartFactory.createLineChart (
                chartTitle,
                X_AXIS,Y_AXIS,
                createDataset(docTimes),
                PlotOrientation.VERTICAL,
                false,true,false);

        ChartPanel chartPanel = new ChartPanel(lineChart);
        chartPanel.setPreferredSize(new java.awt.Dimension(CHART_WIDTH, CHART_HEIGHT));
        setContentPane(chartPanel);
    }

    /**
     * Creates the data set to display on the graph using the document runtimes.
     * @param docTimes The completion times (runtimes) for each of the documents
     * @return The dataset created to be displayed on the graph
     */
    private DefaultCategoryDataset createDataset(ArrayList<Long> docTimes) {
        DefaultCategoryDataset dataSet = new DefaultCategoryDataset( );
        int iterations = docTimes.size();
        for (int i = 0; i < iterations; i++) {
            dataSet.addValue(docTimes.get(i), COL_KEY, Integer.toString(i+1));
        }
        return dataSet;
    }
}
