package com.example.gateway.controller.user;

import com.example.gateway.dto.UserLoginInfoDTO;
import com.example.gateway.entity.User;
import com.example.gateway.service.UserService;
import com.example.gateway.vo.UserLoginInfo;
import com.example.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import com.example.annotation.RateLimit;
import com.example.gateway.dto.UserRegisterDTO;

@Slf4j
@RestController("userLoginController")
@RequestMapping("/user")
public class LoginController {

    @Autowired
    private UserService userService;

    @PostMapping("/login")
    @RateLimit(key = "login", max = 5, seconds = 60, keyType = "ip", message = "登录过于频繁，请稍后再试")
    public Result<UserLoginInfo> login(@RequestBody UserLoginInfoDTO userLoginInfoDTO, HttpServletRequest request){
        String clientIp = getClientIp(request);
        UserLoginInfo userLoginInfo = userService.login(userLoginInfoDTO, clientIp);
        log.info("user登录成功");
        return Result.success(userLoginInfo);
    }

    /**
     * 获取客户端真实 IP（优先从 X-Forwarded-For 取，适配 nginx 代理场景）
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // X-Forwarded-For 可能包含多级代理，取第一个
        if (ip != null && ip.contains(",")) {
            ip = ip.substring(0, ip.indexOf(",")).trim();
        }
        return ip;
    }

    @PostMapping("/register")
    @RateLimit(key = "register", max = 3, seconds = 60, keyType = "ip", message = "注册过于频繁，请稍后再试")
    public Result<User> register(@RequestBody UserRegisterDTO userRegisterDTO){
        User registeredUser = userService.register(userRegisterDTO);
        log.info("user注册成功");
        return Result.success(registeredUser);
    }

    @PostMapping("/send-code")
    @RateLimit(key = "code", max = 1, seconds = 60, keyType = "param:phone", message = "验证码发送过于频繁，请稍后再试")
    public Result<String> sendVerificationCode(@RequestParam String phone){
        userService.sendVerificationCode(phone);
        log.info("向手机号 {} 发送验证码", phone);
        return Result.success("验证码发送成功");
    }

}
