# Hive小项目之谷粒

## 一，数据过滤之初处理













### 1.3，导入数据到`hdfs`根目录

~~~ 
hadoop fs -put data/ /    //把表中的数据存储在data目录下面
~~~

### 1.4，运行`jar`包

~~~ java
//运行jar包可以用hadoop命令，也可以用yarn命令，在这里用yarn命令
yarn jar包的路径名 主类全路径名 数据输入路径 数据输出路径//输入路径下面的数据只能是待处理的数据
yarn jar /opt/module/jars/guli-vedio-1.0-SNAPSHOT.jar com.rzf.mr.ETLdriver /data/video /guli
resourcemanager服务端口号 8032
~~~

### 1.5，创建表准备工作

- 我们存储表中的数据要以`orc`格式存储，所以先创建一张临时表，以`Textfile`格式存储，然后导入另一张表中以`orc`格式存储。

#### 1.5.1，创建初始表

~~~ java
//创建表：gulivideo_ori
create table gulivideo_ori(
    videoId string, 
    uploader string, 
    age int, 
    category array<string>, 
    length int, 
    views int, 
    rate float, 
    ratings int, 
    comments int,
    relatedId array<string>)
row format delimited 
fields terminated by "\t"
collection items terminated by "&"
stored as textfile;
//创建gulivideo_user_ori
create table gulivideo_user_ori(
    uploader string,
    videos int,
    friends int)
row format delimited 
fields terminated by "\t" 
stored as textfile;
~~~

#### 1.5.2，创建`orc`表

~~~ java
create table gulivideo_orc(
    videoId string, 
    uploader string, 
    age int, 
    category array<string>, 
    length int, 
    views int, 
    rate float, 
    ratings int, 
    comments int,
    relatedId array<string>)
clustered by (uploader) into 8 buckets 
row format delimited fields terminated by "\t" 
collection items terminated by "&" 
stored as orc;
create table gulivideo_user_orc(
    uploader string,
    videos int,
    friends int)
row format delimited 
fields terminated by "\t" 
stored as orc;
~~~

#### 1.5.3，把清洗过的数据导入初始表

~~~ java
load data inpath '/guli/part-r-00000' into table gulivideo_ori
load data inpath '/data/user/user.txt'into table gulivideo_user_ori;
~~~

#### 1.5.4，导入数据到`orc`表

~~~ java
insert into table gulivideo_orc select * from gulivideo_ori;
insert into table gulivideo_user_orc select * from gulivideo_user_ori;
~~~









### 1.6，业务分析

#### 1.6.1，**统计视频观看数Top10**

~~~ java
select
videoId,
uploader,
age,
category,
length,
views,
rate,
ratings,
conments,
relatedIds
from gulivideo_orc
order by views desc
limit 10;
~~~

#### 1.6.2，**统计视频类别热度`Top10`**

~~~ java
//某一类视频的个数作为视频的热度
//先把视频的类别数组炸裂开
select videoId,category_name from gulivideo_orc lateral view explode(category) tmp_category as category_name
//按照类别进行计数，并且选出前十名
select category_name,count(*) category_count 
from (select videoId,category_name from gulivideo_orc lateral view explode(category) tmp_category as category_name)t1 
group by category_name
order by category_count desc 
limit 10;
//统计结果
category_name   category_count
Music   179049
Entertainment   127674
Comedy  87818
 Animation      73293
Film    73293
Sports  67329
 Games  59817
Gadgets         59817
 Blogs  48890
People  48890
~~~

#### 1.6.3，**统计出视频观看数最高的20个视频的所属类别以及类别包含Top20视频的个数**

~~~ java
//如何理解本题目？
//也就是先计算出观看数最高的前20个视频以及这些视频所属的类别，然后在统计这些类别中有多少个视频的观看数在这前20个视频中 
//先统计观看数最高的20个视频
select
videoId,
category,
views
from gulivideo_orc
order by views desc
limit 20;t1
videoid category        views
dMH0bHeiRNg     ["Comedy"]      42513417
0XxI-hvPRRA     ["Comedy"]      20282464
1dmVU08zVpA     ["Entertainment"]       16087899
RB-wUgnyGv0     ["Entertainment"]       15712924
QjA5faZF1A8     ["Music"]       15256922
-_CSo1gOd48     ["People "," Blogs"]    13199833
49IDp76kjPw     ["Comedy"]      11970018
tYnn51C3X_w     ["Music"]       11823701
pv5zWaTEVkI     ["Music"]       11672017
//对t1表中的类别进行炸裂
select videoId,category_name
from (select
videoId,
category,
views
from gulivideo_orc
order by views desc
limit 20)t1
lateral view explode(category) tmp_category as category_name;t2
//统计结果
videoid category_name
dMH0bHeiRNg     Comedy
0XxI-hvPRRA     Comedy
1dmVU08zVpA     Entertainment
RB-wUgnyGv0     Entertainment
QjA5faZF1A8     Music
-_CSo1gOd48     People 
-_CSo1gOd48      Blogs
49IDp76kjPw     Comedy
tYnn51C3X_w     Music
pv5zWaTEVkI     Music
//对t2表中的结果进行分组统计
select category_name,count(*) category_count
from (select videoId,category_name
from (select
videoId,
category,
views
from gulivideo_orc
order by views desc
limit 20)t1
lateral view explode(category) tmp_category as category_name)t2
group by category_name
order by category_count desc;
//统计结果
category_name   category_count
 UNA    1
 Blogs  2
People  2
Music   5
Comedy  6
~~~













#### 1.6.5，**统计每个类别中的视频热度Top10，以Music为例**

 