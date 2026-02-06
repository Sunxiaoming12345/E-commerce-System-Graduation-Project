package com.example.gateway.controller.user;

import com.example.gateway.service.UserService;
import com.example.gateway.vo.UserLoginInfo;
import com.example.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
@Slf4j
@RestController("userLoginController")
@RequestMapping("/user")
public class LoginController {


    @Autowired
    private UserService userService;
    @PostMapping("/login")

    public Result<UserLoginInfo> login(@RequestBody UserLoginInfo userLoginInfoDTO){



        UserLoginInfo userLoginInfo = userService.login(userLoginInfoDTO);

        log.info("user登录成功");
        return Result.success(userLoginInfo);
    }

}
