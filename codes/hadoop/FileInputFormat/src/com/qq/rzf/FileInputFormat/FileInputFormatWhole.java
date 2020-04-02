package com.qq.rzf.FileInputFormat;



import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.JobContext;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;

import java.io.IOException;

public class FileInputFormatWhole extends FileInputFormat {

    public FileInputFormatWhole() {
        super();
    }

    @Override
    public RecordReader createRecordReader(InputSplit inputSplit, TaskAttemptContext taskAttemptContext) throws IOException, InterruptedException {
        RecordReaderWhole recordReaderWhole=new RecordReaderWhole();
        //初始化
        recordReaderWhole.initialize(inputSplit,taskAttemptContext);
        return recordReaderWhole;
    }

    @Override
    protected boolean isSplitable(JobContext context, Path filename) {
        return false;
    }
}
