import java.io.*;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.search.*;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.MMapDirectory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;

import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;


import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.FSDirectory;

public class Indexer {
    private static String indexDir = "Index_Files/"; //path relative to current directory
    private static String dataDir = "Data_Files/";
//    String suffix = "jar";
    public static void main(String[] args) throws IOException {

    }

    public void createIndex() throws IOException {
//    File indexDir = new File("C:/Exp/Index/");
//    File dataDir = new File("C:/Users/rezkar/Downloads/lucene-6.3.0/lucene-6.3.0/");
    StandardAnalyzer analyzer = new StandardAnalyzer();
    Path indexPath = Paths.get(indexDir);
    MMapDirectory index = new MMapDirectory(indexPath);

    }

}
