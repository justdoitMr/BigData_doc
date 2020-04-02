package com.qq.rzf.FileInputFormat;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.SequenceFileOutputFormat;

import java.io.IOException;

public class SequenceFileDriver {
    public static void main(String []args) throws Exception {
        //获取job对象
        Configuration configuration=new Configuration();
        Job job= Job.getInstance(configuration);
        //获取jar位置
        job.setJarByClass(SequenceFileDriver.class);
        job.setJarByClass(SequenceFileMapper.class);
        job.setJarByClass(SequenceFileReducer.class);
        //设置map输出类型
        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(BytesWritable.class);
        //设置最终结果输出
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(BytesWritable.class);
        //设置输出输入路径
        FileInputFormat.setInputPaths(job,new Path(args[0]));
        FileOutputFormat.setOutputPath(job,new Path(args[1]));
        //设置输入map端的输入格式，即设置inputformat
        job.setInputFormatClass(FileInputFormatWhole.class);
        //设置输出的outputformat
        job.setOutputFormatClass(SequenceFileOutputFormat.class);
        //提交作业
        job.waitForCompletion(true);



    }


}
