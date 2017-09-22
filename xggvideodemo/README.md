#本工程是视频播放平台的简单演示程序，不具备生产环境使用要求

#使用技术说明
1. 基于FFmpeg + Nginx-rtmp-module搭建流媒体服务器，使用FFmpeg转RTSP为RTMP或HLS，然后推送到Nginx-rtmp服务器
2. 演示程序：Spring-boot + Thymeleaf + Jwplayer
3. 所有依赖项都在pom.xml中声明

#注意事项
1. 引入外部jar包[onvif-java-lib](https://github.com/milg0/onvif-java-lib)
   以下命令将该jar安装到本地maven仓库以供引用（onvif-java-lib非maven工程，所以手动添加到本地仓库）
   
   ```mvn install:install-file -Dfile=onvif-2016-03-16.jar -DgroupId=milg0 -DartifactId=onvif-java-lib -Dversion=6.3.16 -Dpackaging=jar```
   
2. 本程序还依赖另外一个项目jffmpeg-cli，这个项目使用java开启进程，执行FFmpeg命令
   由于jffmpeg-cli是maven工程，因此在根目录下执行以下命令：
   ```mvn install```