package com.example.gateway.controller.admin;

import com.example.gateway.dto.AdminLoginInfoDTO;
import com.example.gateway.entity.Admin;
import com.example.gateway.service.AdminService;
import com.example.gateway.vo.AdminLoginInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.example.result.Result;


@Slf4j
@RestController("adminLoginController")
@RequestMapping("/admin")
public class LoginController {


    @Autowired
    private AdminService adminService;
    @PostMapping("/login")

    public Result<AdminLoginInfo> login(@RequestBody AdminLoginInfoDTO adminLoginInfoDTO){



        AdminLoginInfo adminLoginInfo = adminService.login(adminLoginInfoDTO);

        log.info("admin登录成功");
        return Result.success(adminLoginInfo);
    }

}
