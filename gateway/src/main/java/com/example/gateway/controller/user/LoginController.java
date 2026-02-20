package com.example.gateway.controller.user;

import com.example.gateway.service.UserService;
import com.example.gateway.vo.UserLoginInfo;
import com.example.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController("userLoginController")
@RequestMapping("/user")
@Api(tags = "用户登录")
public class LoginController {

    @Autowired
    private UserService userService;

    @PostMapping("/login")
    @ApiOperation(value = "用户登录", notes = "用户通过用户名和密码登录系统")
    public Result<UserLoginInfo> login(@ApiParam(name = "userLoginInfo", value = "用户登录信息", required = true)
                                      @RequestBody UserLoginInfo userLoginInfoDTO){
        UserLoginInfo userLoginInfo = userService.login(userLoginInfoDTO);
        log.info("user登录成功");
        return Result.success(userLoginInfo);
    }

}
