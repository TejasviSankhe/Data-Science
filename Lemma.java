
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.io.IOException;
import java.util.StringTokenizer;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import org.apache.hadoop.io.Writable;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.MapWritable;
import org.apache.hadoop.io.LongWritable;

import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;


public class Lemma {
	static Map<String,ArrayList<String>> lemMap=new TreeMap<String, ArrayList<String>>();
	
	public static void main(String args[]) throws ClassNotFoundException, IOException, InterruptedException
	{

		Configuration conf = new Configuration();

		try{
			Path path=new Path("new_lemmatizer.csv");
			FileSystem fs = FileSystem.get(conf);
			BufferedReader br=new BufferedReader(new InputStreamReader(fs.open(path)));
			String line;
			line=br.readLine();
			while (line != null && line.length()>0){
				String[] lemmas =line.split(",");
				ArrayList<String> temp=new ArrayList<String>();
				for(int i=1;i<lemmas.length && lemmas[i].length()>0 ;i++){ 
					temp.add(lemmas[i]);
				}
				lemMap.put(lemmas[0], temp);
				line=br.readLine();
			}
		}catch(Exception e){
		}


		Job job = Job.getInstance(conf, "lemma");
		job.setJarByClass(Lemma.class);
		job.setMapperClass(LMapper.class);
		job.setCombinerClass(CombinerClass.class);
		job.setReducerClass(LReducer.class);
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(Text.class);
		job.setMapOutputKeyClass(Text.class);
		job.setMapOutputValueClass(Text.class);
		FileInputFormat.addInputPath(job, new Path(args[0]));
		FileOutputFormat.setOutputPath(job, new Path(args[1]));

		System.exit(job.waitForCompletion(true) ? 0 : 1);
	
	}

	public static class LMapper extends Mapper<LongWritable, Text, Text, Text>{

			private Text location = new Text();
			private Text word = new Text();

			@Override
			protected void map(LongWritable key, Text value, Context context) 
					throws IOException, InterruptedException {

				if(value.toString().contains(">"))
				{

					location.set(value.toString().split(">")[0].concat(">,"));
					
					String[] tokens = value.toString().split(">")[1].replaceAll("[^a-zA-Z0-9\\s]", "").split("\\s+");
					if (tokens.length > 1) {
						for (int i = 0; i < tokens.length - 1; i++) {
							word.set(tokens[i].replaceAll("j", "i").replaceAll("v","u"));
							context.write(word,location );
						}
													
					}
				}
			}

		}
	
	  public static class CombinerClass extends Reducer<Text, Text, Text, Text>{
	        public void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
	            String temp = "";
	            for (Text value : values) {
	                temp += value.toString() ;
	            }
	            context.write(key, new Text(temp));
	        }
	    
	    }
	    

		public static class LReducer extends Reducer<Text, Text, Text, Text> {

			//private MyMap incrementingMap = new MyMap();

			protected void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
				//incrementingMap.clear();
				String temp="";
				for (Text value : values) {
					 temp += value.toString() + " ";
				}
				
				 if (lemMap.containsKey(key.toString())) {
		                ArrayList<String> list=lemMap.get(key.toString());
		                list = lemMap.get(key.toString());
		                for (int i = 0; i < list.size(); i++) {
		                    context.write(new Text(list.get(i)), new Text("{"+temp+"}"));
		                }
		            } else {
		                context.write(key, new Text("{"+temp+"}"));
		            }
			}

		}
		
	}

