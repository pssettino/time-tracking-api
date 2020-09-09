package com.scrumbox.mm.timetrackingapi.controller;


import com.scrumbox.mm.timetrackingapi.service.QrService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tracking/qr")
public class QrController {

    @Autowired
    private QrService qrService;

    @GetMapping(value = "/{documentNumber}", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public @ResponseBody byte[] trackingQr(@PathVariable String documentNumber) {return qrService.getQrEmployee(documentNumber); }

}
