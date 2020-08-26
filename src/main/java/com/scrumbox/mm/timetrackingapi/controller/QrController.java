package com.scrumbox.mm.timetrackingapi.controller;


import com.scrumbox.mm.timetrackingapi.persistence.domain.Tracking;
import com.scrumbox.mm.timetrackingapi.service.QrService;
import javassist.bytecode.ByteArray;
import net.glxn.qrgen.javase.QRCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.io.File;

@RestController
@RequestMapping("/api/tracking/qr")
public class QrController {

    @Autowired
    private QrService qrService;

    @GetMapping(value = "/{dni}", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public @ResponseBody byte[] trackingQr(@PathVariable String dni) {return qrService.trackingQr(dni); }

}
