package com.poc.usingautoconfiguration.controller;


import com.madgag.gif.fmsware.AnimatedGifEncoder;
import com.poc.usingautoconfiguration.services.ConverterService;
import com.poc.usingautoconfiguration.services.GifEncoderService;
import com.poc.usingautoconfiguration.services.VideoDecoderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FrameGrabber;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.springframework.http.MediaType.IMAGE_GIF_VALUE;

@RestController
@RequiredArgsConstructor
@Slf4j
public class UploadController {

    @Value("${multipart.location}")
    private String location;

    private final ConverterService converterService;
    private final GifEncoderService gifEncoderService;
    private final VideoDecoderService videoDecoderService;

    @PostMapping(value = "/upload", produces = IMAGE_GIF_VALUE)
    public String upload(@RequestPart("file") MultipartFile file,
                         @RequestParam("start") int start,
                         @RequestParam("end") int end,
                         @RequestParam("speed") int speed,
                         @RequestParam("repeat") boolean repeat) throws IOException, FrameGrabber.Exception {
        File videoFile = new File(location + "/" + System.currentTimeMillis() + ".mp4");
        file.transferTo(videoFile);

        log.info("Saved video file to {}", videoFile.getAbsolutePath());

        Path output = Paths.get(location + "/gif/" + System.currentTimeMillis() + ".gif");

        FFmpegFrameGrabber frameGrabber = videoDecoderService.read(videoFile);
        AnimatedGifEncoder gifEncoder = gifEncoderService.getGifEncoder(repeat,
                (float) frameGrabber.getFrameRate(), output);
        converterService.toAnimatedGif(frameGrabber, gifEncoder, start, end, speed);

        log.info("Saved generated gif to {}", output.toString());

        return output.getFileName().toString();
    }
}
