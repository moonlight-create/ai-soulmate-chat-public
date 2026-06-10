package com.wj.aisoulmatechat.controller;

import com.wj.aisoulmatechat.config.properties.MyServerConfigProperties;
import com.wj.aisoulmatechat.service.SoulmateAvatarService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import opennlp.tools.util.StringUtil;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/avatar/soulmate")
public class SoulmateAvatarController {
    private final SoulmateAvatarService soulmateAvatarService;

    private static final String DIR = System.getProperty("user.dir") + "/avatar/soulmate/file";
    //默认头像
    private static final String DEFAULT_AVATAR = "https://picsum.photos/id/64/300/300";
    private static final Long DEFAULT_USER_ID = 1L;

    private final MyServerConfigProperties myConfigProperties;

    //查询头像链接
    @GetMapping("/get")
    public String getAvatar() {
        String url = soulmateAvatarService.getSoumateAvatar(DEFAULT_USER_ID);
        return StringUtil.isEmpty(url) ? DEFAULT_AVATAR : url;
    }

    /**
     * 上传并保存伴侣头像
     */
    @SneakyThrows
    @PostMapping("/uploadAndSave")
    public String uploadAndSave(@RequestParam("file") MultipartFile file) {
        File dir = new File(DIR);
        if (!dir.exists()) dir.mkdirs();

        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
        File dest = new File(dir, fileName);
        file.transferTo(dest);

//        String avatarUrl = myConfigProperties.getIp() + "/avatar/soulmate/file/" + fileName;
        String avatarUrl = "avatar/soulmate/file/" + fileName;

        soulmateAvatarService.saveOrUpdateAvatar(DEFAULT_USER_ID, avatarUrl);

        return avatarUrl;
    }

    /**
     * 上传并保存伴侣头像
     */
    @SneakyThrows
    @PostMapping("/upload")
    public String upload(@RequestParam("file") MultipartFile file) {
        File dir = new File(DIR);
        if (!dir.exists()) dir.mkdirs();

        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
        File dest = new File(dir, fileName);
        file.transferTo(dest);

//        return myConfigProperties.getIp() + "/avatar/soulmate/file/" + fileName;
        return "/avatar/soulmate/file/" + fileName;
    }

    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        //明文固定123456
        String newPwd = encoder.encode("Wj@666888");
        System.out.println(newPwd);
    }

}
