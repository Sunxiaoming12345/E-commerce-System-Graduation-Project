package com.example.userservice.controller;

import com.example.context.BaseContext;
import com.example.userservice.entity.User;
import com.example.userservice.service.UserService;
import com.example.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
@Api(tags = "用户管理")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/info")
    @ApiOperation(value = "获取用户信息", notes = "获取当前用户的详细信息")
    public Result<User> getUserInfo(){
        Long userId = BaseContext.getCurrentId();
        User user = userService.getUserInfo(userId);
        return Result.success(user);
    }

    @PutMapping("/info")
    @ApiOperation(value = "更新用户信息", notes = "更新当前用户的详细信息")
    public Result updateUserInfo(@RequestBody User user){
        Long userId = BaseContext.getCurrentId();
        user.setId(userId);
        userService.updateUserInfo(user);
        return Result.success();
    }

    @PutMapping("/password")
    @ApiOperation(value = "修改密码")
    public Result changePassword(@RequestBody java.util.Map<String, String> body){
        Long userId = BaseContext.getCurrentId();
        userService.changePassword(userId, body.get("oldPassword"), body.get("newPassword"));
        return Result.success();
    }

}