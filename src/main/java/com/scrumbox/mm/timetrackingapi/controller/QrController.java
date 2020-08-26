package com.scrumbox.mm.timetrackingapi.controller;


import com.scrumbox.mm.timetrackingapi.persistence.domain.Tracking;
import javassist.bytecode.ByteArray;
import net.glxn.qrgen.javase.QRCode;
import org.springframework.web.bind.annotation.*;

import java.io.File;

@RestController
@RequestMapping("/api/tracking/qr")
public class QrController {

    //TODO
    private final String URL_TRACKING = "localhost:8080/time-tracking-api/api/tracking/";

    @GetMapping("/{dni}")
    public File trackingQr(@PathVariable Integer dni) {return QRCode.from(URL_TRACKING + dni).file(dni.toString());
    }

}
