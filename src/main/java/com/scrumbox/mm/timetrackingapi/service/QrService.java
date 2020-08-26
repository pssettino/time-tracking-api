package com.scrumbox.mm.timetrackingapi.service;

import com.netflix.appinfo.InstanceInfo;
import com.netflix.discovery.EurekaClient;
import com.scrumbox.mm.timetrackingapi.exception.TimeTrackingException;
import javassist.bytecode.ByteArray;
import net.glxn.qrgen.core.image.ImageType;
import net.glxn.qrgen.javase.QRCode;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

@CacheConfig(cacheNames = {"qr"})
@Service
public class QrService {

    private final String TRACKING_API = "http://%s:%s/time-tracking-api/api/tracking/%s";

    @Cacheable
    public byte[] trackingQr(String dni) {
        String serviceUrl = String.format(TRACKING_API, "localhost", "8080", dni);

        ByteArrayOutputStream stream = QRCode
                .from(serviceUrl)
                .to(ImageType.PNG)
                .withSize(250, 250)
                .stream();

        return stream.toByteArray();
    }
}
