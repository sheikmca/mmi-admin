package sg.ncs.kp.admin.strategy.impl;

import org.springframework.stereotype.Component;
import sg.ncs.kp.admin.strategy.NationStrategy;
import sg.ncs.kp.uaa.common.constant.CommonConstant;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @auther IVAN
 * @date 2022/8/20
 * @description
 */
@Component(CommonConstant.ZH_COUNTRY_CODE)
public class ZHStrategy implements NationStrategy {

    @Override
    public boolean verifyPhone(String phone) {
        Pattern compile = Pattern.compile(CommonConstant.ZH_PHONE_REG);
        Matcher matcher = compile.matcher(phone);
        return matcher.matches();
    }
}
