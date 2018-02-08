package com.ming.shopping.beauty.service.service.impl;

import com.ming.shopping.beauty.service.entity.login.Login;
import com.ming.shopping.beauty.service.entity.login.Merchant;
import com.ming.shopping.beauty.service.entity.login.User;
import com.ming.shopping.beauty.service.repository.LoginRepository;
import com.ming.shopping.beauty.service.repository.UserRepository;
import com.ming.shopping.beauty.service.service.MerchantService;
import com.ming.shopping.beauty.service.service.StagingService;
import me.jiangcai.jpa.entity.support.Address;
import me.jiangcai.wx.model.Gender;
import org.apache.commons.lang.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;

/**
 * @author CJ
 */
@Service
public class StagingServiceImpl implements StagingService {

    @Autowired
    private LoginRepository loginRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private MerchantService merchantService;

    @Override
    public void initStagingEnv() {
        int count = 20;
        if (loginRepository.count() <= count) {
            // 在staging 中 建立足够多的测试帐号
            while (count-- > 0)
                createDemoUser();
        }
    }

    private Login createDemoUser() {
        // TODO 最好是走现有的service
        Login login = new Login();
        login.setLoginName(RandomStringUtils.randomNumeric(11));
        login.setGuidable(new Random().nextBoolean());
//        login.addLevel(ManageLevel.root, ManageLevel.user);
        login.setCreateTime(LocalDateTime.now());
        login = loginRepository.saveAndFlush(login);
        User user = new User();
        user.setId(login.getId());
        user.setLogin(login);
        user.setFamilyName(RandomStringUtils.randomAlphabetic(1));
        user.setGender(Gender.values()[new Random().nextInt(Gender.values().length)]);
        userRepository.save(user);
        return login;
    }

    /**
     * 一个商户，一个门店，一个门店代表
     * 商户一共建立了4个项目；
     * 已审核，enabled, 到门店，门店未enabled
     * 已审核，enabled, 到门店，门店enabled
     * 已审核，没enabled, 到门店，门店enabled
     * 非已审核；，enabled, 到门店，门店enabled
     *
     * @return 供staging使用的测试数据;一个商户，一个门店，一个门店代表
     */
    public Object[] generateStagingData() {
        Login merchantLogin = createDemoUser();
        Merchant merchant = new Merchant();
        merchant.setName("staging商户");
        merchant.setContact("staging商户联系人");
        merchant.setTelephone(merchantLogin.getLoginName());
        merchant.setAddress(address("staging地址"));
//        merchant.
        merchant = merchantService.addMerchant(merchantLogin.getId(), merchant);

        return new Object[0];
    }

    private Address address(String other) {
        Address address = new Address();
        address.setProvince("浙江省");
        address.setPrefecture("绍兴市");
        address.setCounty("诸暨市");
        address.setOtherAddress(other);
        return address;
    }
}
