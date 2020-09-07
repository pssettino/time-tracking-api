package com.scrumbox.mm.timetrackingapi.service;

import net.glxn.qrgen.core.image.ImageType;
import net.glxn.qrgen.javase.QRCode;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;

@CacheConfig(cacheNames = {"qr"})
@Service
public class QrService {

    private final String USERS_API = "http://%s:%s/users-api/api/employees/dni?dni=%s";

    @Cacheable
    public byte[] getQrEmployee(String dni) {
        String serviceUrl = String.format(USERS_API, "localhost", "8080", dni);

        ByteArrayOutputStream stream = QRCode
                .from(serviceUrl)
                .to(ImageType.PNG)
                .withSize(250, 250)
                .stream();

        return stream.toByteArray();
    }
}
