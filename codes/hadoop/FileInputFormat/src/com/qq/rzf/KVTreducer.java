package com.qq.rzf;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;

public class KVTreducer extends Reducer<Text, LongWritable,Text, LongWritable> {
    Text text=new Text();
    @Override
    protected void reduce(Text key, Iterable<LongWritable> values, Context context) throws IOException, InterruptedException {
        super.reduce(key, values, context);
        int count=0;
        for(LongWritable value:values){
            count++;
        }
        text.set(key);
        context.write(text,new LongWritable(count));
    }
}
