package sg.ncs.kp.admin.feign.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;

/**
 * @date 2021/12/21
 */
@Component
public class FeignInterceptor implements RequestInterceptor {


    @Override
    public void apply(RequestTemplate requestTemplate) {
        String url = requestTemplate.request().url();
        if(url.contains("/actuator")){
            return;
        }
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder
                .getRequestAttributes();
        if (null == attributes) {
            return;
        }
        HttpServletRequest request = attributes.getRequest();
        if (null == request) {
            return;
        }

        Enumeration<String> headerNames = request.getHeaderNames();
        if (headerNames != null) {
            while (headerNames.hasMoreElements()) {
                String name = headerNames.nextElement();
                String values = request.getHeader(name);
                // skip content-length
                if (name.equalsIgnoreCase("Content-Length")){
                    continue;
                }
                requestTemplate.header(name, values);
            }
        }
    }
}
