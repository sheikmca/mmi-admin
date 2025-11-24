package sg.ncs.kp.admin;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestTemplate;
import sg.ncs.kp.admin.config.LdapSocketFactory;

/**
 * @author Duan Ran
 * @date 2022/8/18
 */

@EnableScheduling
@EnableDiscoveryClient
@SpringBootApplication
@ComponentScan({"sg.ncs.kp"})
@MapperScan("sg.ncs.kp.admin.mapper")
public class AdminApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext run = SpringApplication.run(AdminApplication.class, args);
        LdapSocketFactory.setApplicationContext(run);
    }
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }



}
