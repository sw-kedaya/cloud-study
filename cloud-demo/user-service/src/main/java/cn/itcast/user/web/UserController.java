package cn.itcast.user.web;

import cn.itcast.user.config.PatternConfig;
import cn.itcast.user.pojo.User;
import cn.itcast.user.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@RestController
@RequestMapping("/user")
//@RefreshScope
public class UserController {

    @Autowired
    private UserService userService;

//    @Value(value = "${pattern.dataformat}")
//    private String pattern;

    @Autowired
    private PatternConfig patternConfig;

    @GetMapping("/prop")
    public PatternConfig prop() {
        return patternConfig;
    }

    @GetMapping("/now")
    public String now() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern(patternConfig.getDataformat()));
    }

    /**
     * 路径： /user/110
     *
     * @param id 用户id
     * @return 用户
     */
    @GetMapping("/{id}")
    public User queryById(@PathVariable("id") Long id) throws InterruptedException {
        if (id == 1){
            // 测试熔断功能
            Thread.sleep(60);
        }else if(id == 2){
            throw new RuntimeException("触发异常熔断");
        }
        return userService.queryById(id);
    }
}
