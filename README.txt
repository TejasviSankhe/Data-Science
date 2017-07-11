----README----

Part1: WordCount
Run the R Jupyter notebook to extract tweets and strip the user mentions and hashtags from the data frame.
run hadoop with the cleaned input file.
read the hadoop output file in R notebook and plot the word cloud

Files --->
	A. ipynb -- Jupyter notebook containing the R code.
	B. wordCountOutput.txt -- Contains the text file as the hadoop output. 
	C. wc.jar --- JAR file for the MapReduce code.


Part2: Word Co-occurence Pairs and Stripes

	1. Pairs: Used the tweet text data retrieved from the above process, cleaned it. 
	2. Stripes: Same input

	Used map for stripes implemntation.

Activity 1 : Lemma

-JAR: lemma.jar
- read CSV in main into TreeMap, read documents from input folder in map method. Emitted single words with location i.e 1st token on each line.
- Combined the text using combiner. Appended locations for same keys and emitted the appended locations
- Combined in reducer again. For each key looked up TreeMap for lemmas. If present emitted the Lemma as key and Value as the same appended location entries.

Activity 2 : NGrams

-JAR: NGrams.jar and threeGrams.jar
- read CSV in main into TreeMap, read documents from input folder in map method.
- for each word looked up its Lemma from Map and appended neighbors to the first tokens.
- iterated over each tokens returned Lemma and created combinations for the same.
- emitted the key as word appended with neighbors and value as location i.e first token.
- In reducer combined the values for each keys as text and wrote to file.
-Repeat process but consider two neighbors.


References:
https://hadoop.apache.org/docs/stable/hadoop-mapreduce-client/hadoop-mapreduce-client-core/MapReduceTutorial.html