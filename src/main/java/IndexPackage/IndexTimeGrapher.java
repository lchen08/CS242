package IndexPackage;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RectangleInsets;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * This class is to graph the runtimes (completion times) for each document in a line
 * graph using JFreeChart.
 *  @author Lisa Chen, Nikhil Gowda, Poorvaja Sundar, Edward Zabrensky, Jason Zellmer
 *  @version 1.1
 *  @since Mar 03, 2020
 */
public class IndexTimeGrapher extends ApplicationFrame {
    private final String X_AXIS = "Document Number";
    private final String Y_AXIS = "Run Time (sec)";
    private final int CHART_WIDTH = 560;
    private final int CHART_HEIGHT = 500;
    private final int PADDING = 10;
    private final int TICK_INTERVAL = 50000;

    /**
     * Constructs the grapher object with the application title, the chart title, and the
     * list of completion times for all the documents
     * @param appTitle The application title
     * @param chartTitle The title of the chart/graph
     * @param timeListFile The file with the list of times to graph
     * @param lineTitle The label for the line in the chart
     */
    public IndexTimeGrapher(String appTitle, String chartTitle, File timeListFile,
                            String lineTitle) throws FileNotFoundException {
        super(appTitle);
        System.out.println("Creating the graph. Please wait.");

        ArrayList<Long>[] timeList = new ArrayList[1];
        String[] titles = new String[1];

        timeList[0] = createTimeList(timeListFile);
        titles[0] = lineTitle;
        createChart(chartTitle,timeList, titles);
    }

    /**
     * Constructs the grapher object with the application title, the chart title, the
     * list of completion times for all the documents for 2 different indexers, and the
     * titles for the line graphs to label the data form the indexers.
     * @param appTitle The application title
     * @param chartTitle The title of the chart/graph
     * @param timeListFile1 The first file with the list of times to graph for indexer 1
     * @param timeListFile2 The second file with the list of times to graph for indexer 2
     * @param lineTitles The list of titles to correspond to the data in the files
     */
    public IndexTimeGrapher(String appTitle, String chartTitle, File timeListFile1,
                            File timeListFile2, String[] lineTitles)
            throws FileNotFoundException {
        super(appTitle);
        System.out.println("Creating the graph. Please wait.");
        createChart(chartTitle, createTimeList(timeListFile1, timeListFile2), lineTitles);
    }

    /**
     * Creates an object representing the list of times from a given file.
     * @param timeListFile The file with the list of times, 1 time per line
     * @return An array object representing the list of times
     * @throws FileNotFoundException
     */
    private ArrayList<Long> createTimeList(File timeListFile)
            throws FileNotFoundException {
        Scanner s = new Scanner(timeListFile);
        ArrayList<Long> times = new ArrayList<>();
        while (s.hasNext()) {
            Long timeInMS = Long.valueOf(s.nextLine());
            times.add((long) (timeInMS/1000.0));
        }
        return times;
    }

    /**
     * Creates an object representing the list of times from 2 given files to compare
     * between the data of the two files.
     * @param timeListFile1 The first file with the list of times, 1 time per line
     * @param timeListFile2 The second file with the list of times, 1 time per line
     * @return An array of 2 objects representing the list of times (also in array)
     * @throws FileNotFoundException
     */
    private ArrayList<Long>[] createTimeList(File timeListFile1, File timeListFile2)
            throws FileNotFoundException {
        ArrayList<Long>[] lists = new ArrayList[2];
        lists[0] = createTimeList(timeListFile1);
        lists[1] = createTimeList(timeListFile2);
        return lists;
    }

    /**
     * Creates the chart with the given chart title and list of completion times for
     * the documents for indexers to compare.
     * @param chartTitle The title of the chart/graph
     * @param docTimesLists The list of document completion times (ms) for all indexers
     */
    private void createChart(String chartTitle, ArrayList[] docTimesLists,
                             String[] lineTitles) {
        JFreeChart lineChart = ChartFactory.createXYLineChart(
                chartTitle,
                X_AXIS,Y_AXIS,
                createDataset(docTimesLists, lineTitles),
                PlotOrientation.VERTICAL,
                false,true,false);

        //customize domain axis
        XYPlot xyPlot = (XYPlot) lineChart.getPlot();
        NumberAxis domain = (NumberAxis) xyPlot.getDomainAxis();
        domain.setVerticalTickLabels(true);
        domain.setTickUnit(new NumberTickUnit(TICK_INTERVAL));
        lineChart.setPadding(new RectangleInsets(PADDING, PADDING, PADDING, PADDING));

        //customize chart
        ChartPanel chartPanel = new ChartPanel(lineChart);
        chartPanel.setPreferredSize(new java.awt.Dimension(CHART_WIDTH, CHART_HEIGHT));
        setContentPane(chartPanel);
    }

    /**
     * Creates the data set to display on the graph using the document runtimes of the
     * indexers to compare. The graph takes an array of data and an array titles for the
     * data where the arrays are ordered to correspond to each other.
     * @param docTimesLists The document completion time lists for indexers to compare
     * @param lineTitles The titles for each line in the graph
     * @return The data set created to be displayed on the graph
     */
    private XYDataset createDataset(ArrayList[] docTimesLists, String[] lineTitles) {
        XYSeriesCollection dataset = new XYSeriesCollection();
        for (int i = 0; i < docTimesLists.length; i++) {
            XYSeries series = new XYSeries(lineTitles[i]);
            ArrayList<Long> docTimes = docTimesLists[i];
            int numDocuments = docTimes.size();
            for (int j = 0; j < numDocuments; j++) {
                series.add(j+1, docTimes.get(j));
            }
            dataset.addSeries(series);
        }
        return dataset;
    }
}
