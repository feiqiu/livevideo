## FFmpeg示例程序
### 1. transcoding.c
转码流程和过滤器(时间戳水印)演示

### 2. pusher.c
rtsp转rtmp并推送到流媒体服务器

## 备注：
### 1.编译
ubuntu16.04下：
```gcc -o hello_ffmpeg hello_ffmpeg.c -Iffmpeg/include -Lffmpeg/lib -lavdevice -lavformat -lavfilter -lavcodec -lswscale -lavutil -lswresample```

centos7下：
```gcc -g livepush.c -o livepush -I/home/zzh/ffmpeg_build/include -L/home/zzh/ffmpeg_build/lib -lavdevice -lavformat -lavfilter -lavcodec -lswscale -lavutil -lpostproc -lswresample -lm -lpthread -lz -lbz2 -ldl -lx264 -lx265 -lfdk-aac -lvpx -lvorbis -lvorbisenc -logg -lmp3lame -lfreetype -lopus -llzma -lnuma -lstdc++```