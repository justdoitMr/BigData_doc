package com.qq.rzf;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;


import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import java.io.FileInputStream;
import java.io.IOException;

public class KVTdriver  {
    public  static void main(String []args) throws Exception {
        //1 获取配置文件信息,job对象
        Configuration configuration=new Configuration();
        Job job= Job.getInstance(configuration);
        //设置jar包的位置，也就是和map和reducer类关联
        job.setJarByClass(KVTdriver.class);
        job.setJarByClass(KVTMapper.class) ;
        job.setJarByClass(KVTreducer.class);
        //设置map输出类型
        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(LongWritable.class);
        //设置结果输出值类型
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(LongWritable.class);
        //设置输入输出值的路径
        FileInputFormat.setInputPaths(job,new Path(args [0]));
        FileOutputFormat.setOutputPath(job,new Path(args[1]));
        //提交作业
        job.waitForCompletion(true);





    }
}
