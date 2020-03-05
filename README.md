# Lucene Indexer and Query
##### Team Members: Lisa Chen, Nikhil Gowda, Poorvaja Sundar, Ed Zabrensky, Jason Zellmer

## Description

Our project creates a .edu page web crawler using JSoup and an indexer with Lucene Core. The Indexer program reads .data files generated by https://github.com/edzabrensky/PLEJN-EDU-Indexer and indexes all the websites by its contents. The program generates files made by the Lucene IndexWriter that can be used for later queries and a line chart displaying the run-time for indexing the documents.

The Lucene query uses the files from the Indexer to find top hits for a given query. 

## Dataset
A dataset of ~2GB from the listOfUniversities.txt seed can be found here: https://drive.google.com/open?id=1HtZeNl80qhMpVBlPq5bAKW_DitR-QkaJ

## Output
- Indexer: The program generates multiple Lucene IndexWriter that can be used by Lucene QueryParser/IndexReader to retrieve the index results. These files are automatically placed in INDEX_FILES. The files for our run for the given dataset can already be found in this folder.
- QuerySearcher: The program outputs a JSON array with all the results best matching the query as JSONObjects with snippets of the body, urls, and titles of the webpages. 

### Libraries
The following packages and were used directly or by extension are found in the libraries folder:
- Lucene Core 8.4.1
- JFreeChart 1.0.13
- JCommon 1.0.16
- JUnit 4.10
- JSON-simple 1.1.1
- Hamcrest Core 1.1
- Lucene QueryParser 8.4.1
- Lucene Highlighter 8.4.1
- Lucene Memory 8.4.1
- Lucene Queries 8.4.1
- Lucene Sandbox 8.4.1

## Deployment

To run the indexer, simply download the whole project. If using the default setup, simply place the .data files in the DATA_FILES folder and run indexbuilder.bat found in the main folder. A web0.data file is already provided for testing.

If the user wishes to designate a custom directory, the .bat file can be edited to change the line 
`java IndexPackage.IndexBuilder` to `java IndexPackage.IndexBuilder <relative or absolute path of your custom directory>`.


