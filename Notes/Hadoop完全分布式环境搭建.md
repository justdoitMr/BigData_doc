# Hadoop完全分布式环境搭建

## 一，虚拟机环境准备

1. 准备3台客户机（关闭防火墙、静态ip、主机名称）。

~~~ java
//在root权限下打开此配置文件
vim /etc/udev/rules.d/70-persistent-net.rules
//删除eth0该行；将eth1修改为eth0，同时复制物理ip地址
~~~

2. 修改ip地址

~~~ java
//1，打开此配置文件修改静态ip地址
vim /etc/sysconfig/network-scripts/ifcfg-eth0
//2，修改刚才复制的物理地址
//3，修改以下两项为yes和static
ONBOOT=yes
BOOTPROTO=static
//4，修改DNS和网关
DNS1=192.168.1.2
GATEWAY=192.168.1.2
//退出保存即可
~~~

3. 修改主机名称

~~~ java
//在vi /etc/sysconfig/network文件中修改为想要的名称
//通过hostname命令查看主机的名称，修改主机名后必须重启才可以生效
reboot重启
//重启后可以在终端输入ifconfig查看ip
~~~

4. 查看防火墙的状态

~~~ java
//查看防火墙状态
chkconfig iptables --list	
//关闭防火墙
chkconfig iptables off	
~~~

5. 创建自己的用户

~~~ java
useradd rzf
//配置自己的用户具有root权限，在etc/profile文件下修改具有root权限
~~~

6. 在/opt目录下创建文件夹

~~~ java
mkdir module
mkdir software
//修改文件夹所有者
chown rui module
chown rui software
//或者一条语句
chown rui:rui /module /software
~~~

## 二，安装JDK

1. 卸载现有JDK

   1. 查询是否安装Java软件

   ~~~ java
   rpm -qa | grep java
   ~~~

   2. 如果安装的版本低于1.7，卸载该JDK

   ~~~ java
   sudo rpm -e 软件包
   ~~~

   3. 查看JDK安装路径

   ~~~ java
   which java
   ~~~

2. 用SecureCRT工具将JDK导入到opt目录下面的software文件夹下面,并且安装jdk

~~~ java
//解压JDK到/opt/module目录下
tar -zxvf jdk-8u144-linux-x64.tar.gz -C /opt/module/
~~~

3. 配置JDK环境变量

~~~ java
//1，先用pwd命令获取jdk安装目录
//2，打开/etc/profile文件，在profile文件末尾添加JDK路径
#JAVA_HOME
export JAVA_HOME=/opt/module/jdk1.8.0_144
export PATH=$PATH:$JAVA_HOME/bin
//3，让修改后的文件生效
source /etc/profile
//4，此时是否成功
java -version
//如果不可用就重启
sync//标示把内存的文件加载到磁盘
sudo reboot
~~~

## 三，安装Hadoop

1. 下载地址

~~~ java
https://archive.apache.org/dist/hadoop/common/hadoop-2.7.2/
~~~

2. 解压安装文件到/opt/module下面

~~~ java
tar -zxvf hadoop-2.7.2.tar.gz -C /opt/module/
~~~

3. 将Hadoop添加到环境变量

~~~ java
//1，用pwd命令获取安装目录
//2,打开/etc/profile文件,在profile文件末尾添加JDK路径：（shitf+g）
##HADOOP_HOME
export HADOOP_HOME=/opt/module/hadoop-2.7.2
export PATH=$PATH:$HADOOP_HOME/bin
export PATH=$PATH:$HADOOP_HOME/sbin
//3,保存退出，让修改后的文件生效
source /etc/profile
//4,测试是否安装成功
hadoop version
//5，重启(如果Hadoop命令不能用再重启)
sync
sudo reboot
~~~

## 四，Hadoop目录结构分析

（1）bin目录：存放对Hadoop相关服务（HDFS,YARN）进行操作的脚本

（2）etc目录：Hadoop的配置文件目录，存放Hadoop的配置文件

（3）lib目录：存放Hadoop的本地库（对数据进行压缩解压缩功能）

（4）sbin目录：存放启动或停止Hadoop相关服务的脚本

（5）share目录：存放Hadoop的依赖jar包、文档、和官方案例

## 五，ssh集群无密码远程登录配置

~~~ java
//第一步：在home/.ssh文件夹下执行命令生成密钥对
ssh-keygen -t rsa
//第二步：将公钥拷贝到其他主机
ssh-copy-id hadoop101
//现在远程登录192.168.149.103不需要密码
ssh hadoop101
//也要给本主机复制公钥
 ssh-copy-id 192.168.149.102
//在/etc/hosts文件中修改主机ip地址对应的域名
~~~

## 六，编写集群分发脚本xsync

1. 在在/usr/local/bin目录下创建xsync文件，文件内容如下：（也可以在/home/文件夹下创建bin目录存放脚本文件）

~~~ java
#!/bin/bash
#1 获取输入参数个数，如果没有参数，直接退出
pcount=$#
if((pcount==0)); then
echo no args;
exit;
fi

#2 获取文件名称
p1=$1
fname=`basename $p1`
echo fname=$fname

#3 获取上级目录到绝对路径
pdir=`cd -P $(dirname $p1); pwd`
echo pdir=$pdir

#4 获取当前用户名称
user=`whoami`

#5 循环
for((host=103; host<105; host++)); do
        #echo $pdir/$fname $user@hadoop$host:$pdir
        echo --------------- hadoop$host ----------------
        rsync -rvl $pdir/$fname $user@hadoop$host:$pdir
done
~~~

2. 升级脚本权限

~~~ java
chmod 7777 xsync 
xsync 同步的文件名
//即可实现同步,同步的时候一定要在被同步的上一层文件夹
~~~

## 七，基于完全分布式的集群搭建

1. 集群规划

|      | Hadoop101            | Hadoop102                   | Hadoop103                   |
| ---- | -------------------- | --------------------------- | --------------------------- |
| HDFS | NameNode    DataNode | DataNode                    | SecondaryNameNode  DataNode |
| YARN | NodeManager          | ResourceManager NodeManager | NodeManager                 |

2. 集群配置
   1. 核心配置文件：core-site.xml

~~~ java
<!-- 指定HDFS中NameNode的地址 -->
<property>
		<name>fs.defaultFS</name>
      <value>hdfs://hadoop101:9000</value>
</property>

<!-- 指定Hadoop运行时产生文件的存储目录 -->
<property>
		<name>hadoop.tmp.dir</name>
		<value>/opt/module/hadoop-2.7.2/data/tmp</value>
</property>
~~~

​	2. HDFS配置文件:配置hadoop-env.sh

~~~ java
//配置java的环境变量
export JAVA_HOME=/opt/module/jdk1.8.0_144
~~~

​	3. 配置hdfs-site.xml

~~~ java
<property>
		<name>dfs.replication</name>
		<value>3</value>
</property>

<!-- 指定Hadoop辅助名称节点主机配置 -->
<property>
      <name>dfs.namenode.secondary.http-address</name>
      <value>hadoop103:50090</value>
</property>
~~~

​	4. YARN配置文件:配置yarn-env.sh

~~~ java
//配置环境变量
export JAVA_HOME=/opt/module/jdk1.8.0_144
~~~

​	5. 配置yarn-site.xml

~~~ java
<!-- Reducer获取数据的方式 -->
<property>
		<name>yarn.nodemanager.aux-services</name>
		<value>mapreduce_shuffle</value>
</property>

<!-- 指定YARN的ResourceManager的地址 -->
<property>
		<name>yarn.resourcemanager.hostname</name>
		<value>hadoop102</value>
</property>
~~~

​	6. MapReduce配置文件:配置mapred-env.sh

~~~ java
//配置环境变量
export JAVA_HOME=/opt/module/jdk1.8.0_144
~~~

​	7. 配置mapred-site.xml

~~~ java
<!-- 指定MR运行在Yarn上 -->
<property>
		<name>mapreduce.framework.name</name>
		<value>yarn</value>
</property>
~~~

​	8. 配置salvse文件

~~~ java
//注意：该文件中添加的内容结尾不允许有空格，文件中不允许有空行。
Hadoop101
Hadoop102
Hadoop103
~~~

​	9. 在集群上分发配置好的Hadoop配置文件

~~~ java
xsync /opt/module/hadoop-2.7.2/
~~~

​	10. 查看文件分发情况

~~~ java
cat /opt/module/hadoop-2.7.2/etc/hadoop/core-site.xml
~~~

 	11. 集群的启动,如果集群是第一次启动，需要格式化NameNode

~~~ java
hadoop namenode -format
~~~

​	12. 在hadoop101上启动NameNode(以下是单点启动)

~~~ java
hadoop-daemon.sh start namenode
~~~

 	13. 在hadoop102、hadoop103以及hadoop104上分别启动DataNod

~~~ java
hadoop-daemon.sh start datanode
~~~

 	14. 群起集群,应为在上面已经配置过ssh无密码登录和salves文件，所以现在可以群起集群，如果没有配置无密码登录和salves文件，现在需要配置,然后分发文件：xsync slaves。

~~~ java
//如果集群是第一次启动，需要格式化NameNode（注意格式化之前，一定要先停止上次启动的所有namenode和datanode进程，然后再删除data和log数据）
bin/hdfs namenode -format
//启动namenode
sbin/start-dfs.sh
//启动yarn
sbin/start-yarn.sh
//注意：NameNode和ResourceManger如果不是同一台机器，不能在NameNode上启动 YARN，应该在ResouceManager所在的机器上启动YARN。
~~~

 15. Web端查看SecondaryNameNode

     浏览器中输入：<http://hadoop101:50090/status.html>

