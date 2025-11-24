package sg.ncs.kp.admin.strategy.impl;

import org.springframework.stereotype.Component;
import sg.ncs.kp.admin.strategy.NationStrategy;
import sg.ncs.kp.uaa.common.constant.CommonConstant;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Wang Shujin
 * @date 2022/10/14 15:17
 */
@Component(CommonConstant.THAILAND_COUNTRY_CODE)
public class ThailandStrategy implements NationStrategy {

    @Override
    public boolean verifyPhone(String phone) {
        Pattern compile = Pattern.compile(CommonConstant.THAILAND_PHONE_REG);
        Matcher matcher = compile.matcher(phone);
        return matcher.matches();
    }
}
