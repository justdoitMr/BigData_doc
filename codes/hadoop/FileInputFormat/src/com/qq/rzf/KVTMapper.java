package com.qq.rzf;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.IOException;

public class KVTMapper extends Mapper<LongWritable,Text,Text, LongWritable> {
    LongWritable longWritable=new LongWritable(1);
    Text k=new Text();

    @Override
    protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
        //获取一行文本
        String line=value.toString();
        //对文本进行切分
        String []fields=line.split(" ");
        //  循环遍历数组进行写出
        for(String tmp:fields){
            k.set(tmp);
            context.write(k,longWritable);
        }
    }
}
