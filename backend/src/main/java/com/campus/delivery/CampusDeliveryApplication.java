package com.campus.delivery;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 智能 AI 校园外卖平台 · 启动类。
 *
 * <p>模块划分见 docs/02-四人分工与模块归属.md：
 * <ul>
 *   <li>成员1：user（画像/地址）、cart、ai（点餐/搜索/推荐/饮食分析）</li>
 *   <li>成员2：shop、dish、review、ai（评价分析/经营分析）</li>
 *   <li>成员3：admin、ai（智能客服/知识库）</li>
 *   <li>成员4：security、order、delivery、websocket</li>
 * </ul>
 *
 * <p>启动后接口文档地址：http://localhost:8080/api/doc.html
 */
@SpringBootApplication
@MapperScan("com.campus.delivery.modules.**.mapper")
@EnableAsync
@EnableScheduling
public class CampusDeliveryApplication {

    public static void main(String[] args) {
        SpringApplication.run(CampusDeliveryApplication.class, args);
    }
}
