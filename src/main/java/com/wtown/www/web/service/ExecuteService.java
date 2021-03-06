/**
 * @author LYU
 * @create 2017年10月30日 17:16
 * @Copyright(C) 2010 - 2017 GBSZ
 * All rights reserved
 */

package com.wtown.www.web.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.net.URLEncoder;

@Service
public class ExecuteService {

    @Value("${jmeter.config.work_path}")
    private String work_path = "";

    @Value("${jmeter.config.hosts_file}")
    private String hosts_file = "";

    @Value("${jmeter.config.base_dir}")
    private String base_dir = "";

    @Value("${jmeter.config.slave_port}")
    private String slave_port = "";

    @Value("${jmeter.config.jtl_file}")
    private String jtl_file = "";

    @Value("${jmeter.config.jmx_file}")
    private String jmx_file = "";

    public String uploadFile(MultipartFile file) {
        if (file.isEmpty()) {
            return "false";
        }
        String fileName = file.getOriginalFilename();
        int size = (int) file.getSize();
        System.out.println(fileName + "-->" + size);

        //String path = "/root/jmeter-remote-tool/";
        File dest = new File(work_path + jmx_file);
        if (!dest.getParentFile().exists()) { //判断文件父目录是否存在
            dest.getParentFile().mkdir();
        }
        try {
            file.transferTo(dest); //保存文件
            return "true";
        } catch (IllegalStateException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return "false";
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return "false";
        }
    }

    public String getSlaveIp(String domainName) throws IOException {
        //nslookup www.wtown.com.cn | grep 'Address:' | sed '1d' | awk '{print $2}'
        StringBuilder sb = new StringBuilder();
        sb.append("nslookup ");
        sb.append(domainName);
        //sb.append(" | grep 'Address:' | sed '1d' | awk '{print $2}'");

        System.out.println(sb.toString());
        Process process = Runtime.getRuntime().exec(sb.toString());
        String resultStr = getStringFromStream(process.getInputStream());
        int i = resultStr.lastIndexOf(':');
        resultStr = resultStr.substring(i + 1);
        System.out.println(resultStr);
        return trim(resultStr);
    }

    /**
     * 删除文件，可以是文件或文件夹
     *
     * @param fileName 要删除的文件名
     * @return 删除成功返回true，否则返回false
     */
    public boolean delete(String fileName) {
        File file = new File(fileName);
        if (!file.exists()) {
            System.out.println("删除文件失败:" + fileName + "不存在！");
            return false;
        } else {
            if (file.isFile()) {
                return deleteFile(fileName);
            } else {
                return deleteDirectory(fileName);
            }
        }
    }

    public boolean deleteFile(String fileName) {
        File file = new File(fileName);
        // 如果文件路径所对应的文件存在，并且是一个文件，则直接删除
        if (file.exists() && file.isFile()) {
            if (file.delete()) {
                System.out.println("删除单个文件" + fileName + "成功！");
                return true;
            } else {
                System.out.println("删除单个文件" + fileName + "失败！");
                return false;
            }
        } else {
            System.out.println("删除单个文件失败：" + fileName + "不存在！");
            return false;
        }
    }

    public boolean deleteDirectory(String dir) {
        // 如果dir不以文件分隔符结尾，自动添加文件分隔符
        if (!dir.endsWith(File.separator)){
            dir = dir + File.separator;
        }
        File dirFile = new File(dir);
        // 如果dir对应的文件不存在，或者不是一个目录，则退出
        if ((!dirFile.exists()) || (!dirFile.isDirectory())) {
            System.out.println("删除目录失败：" + dir + "不存在！");
            return false;
        }
        boolean flag = true;
        // 删除文件夹中的所有文件包括子目录
        File[] files = dirFile.listFiles();
        for (int i = 0; i < files.length; i++) {
            // 删除子文件
            if (files[i].isFile()) {
                flag = deleteFile(files[i].getAbsolutePath());
                if (!flag){
                    break;
                }
            }
            // 删除子目录
            else if (files[i].isDirectory()) {
                flag = deleteDirectory(files[i]
                        .getAbsolutePath());
                if (!flag){
                    break;
                }
            }
        }
        if (!flag) {
            System.out.println("删除目录失败！");
            return false;
        }
        // 删除当前目录
        if (dirFile.delete()) {
            System.out.println("删除目录" + dir + "成功！");
            return true;
        } else {
            return false;
        }
    }

    private String trim(String s) {
        String result = "";
        if (null != s && !"".equals(s)) {
            result = s.replaceAll("^[　*| *| *|//s*]*", "").replaceAll("[　*| *| *|//s*]*$", "");
        }
        return result;
    }

    public void modifyHosts(String ip, int num) {
        writehosts(hosts_file, "");
        for (int i = 1; i <= num; i++) {
            writehosts(hosts_file, String.format("%s test%d.lvs.com", ip, i));
        }
    }

    private void writehosts(String file, String content) {
        // 创建一个向具有指定name的文件中写入数据的输出文件流。如果第二个参数为true,则将字节写入文件末尾处，而不是写入文件开始处
        FileOutputStream fos2 = null;
        try {
            fos2 = new FileOutputStream(file, true);// 第二個参数为true表示程序每次运行都是追加字符串在原有的字符上
            // 写数据
            fos2.write(content.getBytes());
            fos2.write("\r\n".getBytes());// 写入一个换行
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                fos2.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public String executeTest(int num) throws IOException {
        String iptables = getIptables(num);
        StringBuilder sb = new StringBuilder();
        sb.append(base_dir);
        sb.append("jmeter -n -t ");
        sb.append(work_path);
        sb.append(jmx_file);
        sb.append("  -R  ");
        sb.append(iptables);
        sb.append("  -l  ");
        sb.append(work_path);
        sb.append(jtl_file);
        sb.append(" -e -o ");
        sb.append(work_path);
        sb.append("resultReport");

        //删除之前生成的文件和文件夹
        delete(work_path + "resultReport");
        delete(work_path + jtl_file);

        System.out.println(sb.toString());
        Process process = Runtime.getRuntime().exec(sb.toString());
        String resultStr = getStringFromStream(process.getInputStream());
        System.out.println(resultStr);
        return resultStr;
        //return sb.toString();
    }

    private String getStringFromStream(InputStream tInputStream) {
        if (tInputStream != null) {
            try {
                BufferedReader tBufferedReader = new BufferedReader(new InputStreamReader(tInputStream));
                StringBuffer tStringBuffer = new StringBuffer();
                String sTempOneLine;
                while ((sTempOneLine = tBufferedReader.readLine()) != null) {
                    tStringBuffer.append(sTempOneLine);
                }
                if (tStringBuffer.length() != 0) {
                    return tStringBuffer.toString();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return null;
    }

    private String getIptables(int num) {
        StringBuilder sb = new StringBuilder("");
        for (int i = 1; i <= num; i++) {
            sb.append(String.format("test%d.lvs.com:%s ", i, slave_port));
        }
        System.out.println(sb.toString());
        return sb.toString();
    }

    public ResponseEntity<Resource> returnTestResult() {
        ByteArrayOutputStream bos = null;
        FileInputStream inputStream = null;
        try {
            bos = new ByteArrayOutputStream();
            HttpHeaders headers = new HttpHeaders();
            headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
            headers.add("Pragma", "no-cache");
            headers.add("Expires", "0");
            headers.add("charset", "utf-8");
            //设置下载文件名
            headers.add("Content-Disposition", "attachment;filename=\"" + URLEncoder.encode(jtl_file, "UTF-8") + "\"");
            File file = new File(work_path + jtl_file);
            inputStream = new FileInputStream(file);
            byte[] buffer = new byte[3];
            int len = 0;
            while ((len = inputStream.read(buffer)) != -1) {
                bos.write(buffer, 0, len);
            }
            Resource resource = new InputStreamResource(new ByteArrayInputStream(bos.toByteArray()));

            return ResponseEntity.ok().headers(headers).contentType(MediaType.parseMediaType("application/x-msdownload")).body(resource);

        } catch (IOException e) {
            if (null != bos) {
                try {
                    bos.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
            return null;
        } finally {
            if (null != inputStream) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
