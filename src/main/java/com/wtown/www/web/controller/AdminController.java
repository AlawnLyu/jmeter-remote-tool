/**
 * @author LYU
 * @create 2017年10月30日 16:50
 * @Copyright(C) 2010 - 2017 GBSZ
 * All rights reserved
 */

package com.wtown.www.web.controller;

import com.wtown.www.web.service.ExecuteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;

@Controller
public class AdminController {

    @Autowired
    private ExecuteService executeService;

    @RequestMapping("/index.html")
    public String index(Model model) {
        model.addAttribute("num", "10");
        model.addAttribute("domainname", "test.lvs.com");
        return "index";
    }

    @RequestMapping("/execute")
    @ResponseBody
    public String executeTest(@RequestParam("num") int num, @RequestParam("domainname") String domainname) throws IOException {
        executeService.modifyHosts(executeService.getSlaveIp(domainname),num);
        return executeService.executeTest(num);
    }

    @RequestMapping(value = "/upload", method = RequestMethod.POST)
    @ResponseBody
    public String upload(@RequestParam("file") MultipartFile file) {
        return executeService.uploadFile(file);
    }

    @RequestMapping(value = "/file", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<Resource> returnTestResult() {
        return executeService.returnTestResult();
    }

    @RequestMapping("/test")
    @ResponseBody
    public String test() throws IOException {
        return executeService.getSlaveIp("www.wtown.com.cn");
    }
}
