
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



	public static class LMapper
	extends Mapper<LongWritable, Text, Text, Text>{

			private Text location = new Text();
			private Text word = new Text();
			private MapWritable map=new MapWritable();

			@Override
			protected void map(LongWritable key, Text value, Context context) 
					throws IOException, InterruptedException {

				if(value.toString().contains(">"))
				{

					location.set(value.toString().split(">")[0]);
					String[] tokens = value.toString().split(">")[1].replaceAll("[^a-zA-Z0-9\\s]", "").split("\\W+");
					if (tokens.length > 1) {
						for (int i = 0; i < tokens.length - 1; i++) {
							//map.clear();
							word.set(tokens[i]);
							if(map.containsKey(word)){
								Text t = new Text(map.get(word)+","+location);
								map.put(word, t);
							}
							else
								map.put(word, location);
							//context.write(word,map);
						}
					}
				}
			}

			@Override
			protected void cleanup(Context context) throws IOException, InterruptedException {
				MapWritable emit=new MapWritable();
				Text lemmaList=new Text();
				Set<Writable> keys=map.keySet();
				for(Writable t:keys)
				{
					Text locList = (Text) map.get(t);
					//String[] locs=map.get(t).toString().split("|");
					if(lemMap.containsKey(t))
					{
						ArrayList<String> lems=lemMap.get(t);
						for(int count=0;count<lems.size();count++)
						{
							lemmaList=new Text(lems.get(count));
							{
								emit.put(lemmaList, locList);
							}
						}
					}
					else
					{
						emit.put(t, locList);
					}
					
					for(Writable wr: emit.keySet())
						context.write((Text) wr, (Text) emit.get(wr));
				}
			}
		}

		public static class LReducer extends Reducer<Text, Text, Text, Text> {

			//private MyMap incrementingMap = new MyMap();

			protected void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
				//incrementingMap.clear();
				Text list=new Text();
				for (Text value : values) {
					list.set(list+","+value);
				}
				context.write(key,list);
			}

		}
		public static void main(String[] args) throws Exception {
			Configuration conf = new Configuration();

			Job job = Job.getInstance(conf, "lemma");
			job.setJarByClass(Lemma.class);
			job.setMapperClass(LMapper.class);

			job.setReducerClass(LReducer.class);
			job.setOutputKeyClass(Text.class);
			job.setOutputValueClass(Text.class);
			job.setMapOutputKeyClass(Text.class);
			job.setMapOutputValueClass(Text.class);
			FileInputFormat.addInputPath(job, new Path(args[0]));
			FileOutputFormat.setOutputPath(job, new Path(args[1]));

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

			System.exit(job.waitForCompletion(true) ? 0 : 1);
		}
	}

