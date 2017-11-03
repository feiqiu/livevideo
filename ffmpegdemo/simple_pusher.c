/**
 * 简单的RTMP推流程序(不进行转码操作)
 */
#include <libavformat/avformat.h>
#include <libavutil/mathematics.h>
#include <libavutil/time.h>

int main(int argc, char **argv) {
    AVOutputFormat *ofmt = NULL;
    AVFormatContext *ifmt_ctx = NULL, *ofmt_ctx = NULL;
    AVPacket pkt;
    const char* in_filename, *out_filename;
    int ret, i;
    int video_index = -1;
    int frame_index = 0;
    int64_t start_time = 0;

    if (argc < 3) {
        printf("usage: %s input output\n", argv[0]);
        return 1;
    }
    in_filename = argv[1];
    out_filename = argv[2];

    av_register_all();
    avformat_network_init();

    //使用TCP连接打开RTSP，设置最大延迟时间
    AVDictionary *avdic = NULL;
    char *option_key = "rtsp_transport";
    char *option_value = "tcp";
    av_dict_set(&avdic, option_key, option_value, 0);
    char option_key2[]="max_delay";
    char option_value2[]="5000000";
    av_dict_set(&avdic,option_key2,option_value2,0);

    ret = avformat_open_input(&ifmt_ctx, in_filename, NULL, &avdic);
    if (ret < 0) {
        av_log(NULL, AV_LOG_ERROR, "failed to open input file\n");
        goto end;
    }

    ret = avformat_find_stream_info(ifmt_ctx, NULL);
    if (ret < 0) {
        av_log(NULL, AV_LOG_ERROR, "failed to retrieve input file info\n");
        goto end;
    }

    for (i = 0; i < ifmt_ctx->nb_streams; i++) {
        if (ifmt_ctx->streams[i]->codecpar->codec_type == AVMEDIA_TYPE_VIDEO) {
            video_index = i;
            break;
        }
    }
    if (video_index < 0) {
        av_log(NULL, AV_LOG_FATAL, "cannot find video stream in input file\n");
        ret = AVERROR_UNKNOWN;
        goto end;
    }
    av_dump_format(ifmt_ctx, 0, in_filename, 0);

    // Output
    ret = avformat_alloc_output_context2(&ofmt_ctx, NULL, "flv", out_filename);
    if (ret < 0) {
        av_log(NULL, AV_LOG_FATAL, "failed to open output file\n");
        goto end;
    }

    // 此时已经通过上一步获取了oformat，如果没指定"flv"则会通过out_filename推断
    ofmt = ofmt_ctx->oformat;
    for (i = 0; i < ifmt_ctx->nb_streams; i++) {
        AVStream *in_stream = ifmt_ctx->streams[i];
        AVStream *out_stream = avformat_new_stream(ofmt_ctx, NULL);
        if (!out_stream) {
            av_log(NULL, AV_LOG_FATAL, "failed allocating output stream\n");
            ret = AVERROR_UNKNOWN;
            goto end;
        }
        AVCodecParameters *in_codecpar = in_stream->codecpar;

        // copy parameters
        ret = avcodec_parameters_copy(out_stream->codecpar, in_codecpar);
        if (ret < 0) {
            av_log(NULL, AV_LOG_FATAL, "failed to copy parameters\n");
            goto end;
        }

        out_stream->codecpar->codec_tag = 0;
    }

    // open output url
    if (!(ofmt->flags & AVFMT_NOFILE)) {
        ret = avio_open(&ofmt_ctx->pb, out_filename, AVIO_FLAG_WRITE);
        if (ret < 0) {
            printf("Could not open output URL '%s'\n", out_filename);
            goto end;
        }
    }

    // dump format
    av_dump_format(ofmt_ctx, 0, out_filename, 1);

    // 写头文件
    ret = avformat_write_header(ofmt_ctx, NULL);
    if (ret < 0) {
        av_log(NULL, AV_LOG_ERROR, "failed to write header\n");
        goto end;
    }

    start_time = av_gettime();
    while(1) {
        ret = av_read_frame(ifmt_ctx, &pkt);
        if (ret < 0) {
            break;
        }

        // FIX: No PTS
        // Simple write PTS
        if (pkt.pts == AV_NOPTS_VALUE) {
            AVRational in_time_base = ifmt_ctx->streams[video_index]->time_base;
            // Duration between 2 frames
            int64_t calc_duration = (double)AV_TIME_BASE / av_q2d(ifmt_ctx->streams[video_index]->r_frame_rate);
            // Parameters
            pkt.pts = (double)(frame_index * calc_duration) / (double)(av_q2d(in_time_base) * AV_TIME_BASE);
            pkt.dts = pkt.pts;
            pkt.duration = (double)calc_duration / (double)(av_q2d(in_time_base)*AV_TIME_BASE);
        }

        // Important: Delay
        if (pkt.stream_index == video_index) {
            AVRational in_time_base = ifmt_ctx->streams[video_index]->time_base;
            int64_t pts_time = av_rescale_q(pkt.dts, in_time_base, AV_TIME_BASE_Q);
            int64_t now_time = av_gettime() - start_time;
            if (pts_time > now_time) {
                printf("now_time:%d pts_time: %d\n", now_time, pts_time);
                av_usleep(pts_time - now_time);
            }
        }

        AVStream *in_stream = ifmt_ctx->streams[pkt.stream_index];
        AVStream *out_stream = ofmt_ctx->streams[pkt.stream_index];
        /* copy packet */
        // Convert PTS/DTS
        pkt.pts = av_rescale_q_rnd(pkt.pts, in_stream->time_base, out_stream->time_base,
                                 AV_ROUND_NEAR_INF | AV_ROUND_PASS_MINMAX);
        pkt.dts = av_rescale_q_rnd(pkt.dts, in_stream->time_base, out_stream->time_base,
                                   AV_ROUND_NEAR_INF | AV_ROUND_PASS_MINMAX);
        pkt.duration = av_rescale_q(pkt.duration, in_stream->time_base, out_stream->time_base);
        pkt.pos = -1;

//        av_log(NULL, AV_LOG_INFO, "Send %d video frames to output url\n", frame_index);
        frame_index++;

        ret = av_interleaved_write_frame(ofmt_ctx, &pkt);
        if (ret < 0) {
            /**
            当网络有问题时，容易出现到达包的先后不一致，pts时序混乱会导致
            av_interleaved_write_frame函数报 -22 错误。暂时先丢弃这些迟来的帧吧
            若所大部分包都没有pts时序，那就要看情况自己补上时序（比如较前一帧时序+1）再写入。
            */
            if (ret == -22) {
                continue;
            } else {
                av_log(NULL, AV_LOG_ERROR, "Error muxing packet.error code %d\n", ret);
                break;
            }
        }

        av_packet_unref(&pkt);
    }

    av_write_trailer(ofmt_ctx);

    end:
    avformat_close_input(&ifmt_ctx);
    if (ofmt && ofmt_ctx && !(ofmt->flags & AVFMT_NOFILE)) {
        avio_close(ofmt_ctx->pb);
    }
    avformat_free_context(ofmt_ctx);
    if (ret < 0) {
        av_log(NULL, AV_LOG_ERROR, "Error occurred\n");
        return 1;
    }

    return 0;
}