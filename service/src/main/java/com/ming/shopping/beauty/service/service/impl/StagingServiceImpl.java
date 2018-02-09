package com.ming.shopping.beauty.service.service.impl;

import com.ming.shopping.beauty.service.entity.item.Item;
import com.ming.shopping.beauty.service.entity.item.StoreItem;
import com.ming.shopping.beauty.service.entity.login.Login;
import com.ming.shopping.beauty.service.entity.login.Merchant;
import com.ming.shopping.beauty.service.entity.login.Represent;
import com.ming.shopping.beauty.service.entity.login.Store;
import com.ming.shopping.beauty.service.entity.login.User;
import com.ming.shopping.beauty.service.entity.support.AuditStatus;
import com.ming.shopping.beauty.service.repository.ItemRepository;
import com.ming.shopping.beauty.service.repository.LoginRepository;
import com.ming.shopping.beauty.service.repository.StoreItemRepository;
import com.ming.shopping.beauty.service.repository.UserRepository;
import com.ming.shopping.beauty.service.service.ItemService;
import com.ming.shopping.beauty.service.service.MerchantService;
import com.ming.shopping.beauty.service.service.RepresentService;
import com.ming.shopping.beauty.service.service.StagingService;
import com.ming.shopping.beauty.service.service.StoreItemService;
import com.ming.shopping.beauty.service.service.StoreService;
import me.jiangcai.jpa.entity.support.Address;
import me.jiangcai.lib.resource.service.ResourceService;
import me.jiangcai.wx.model.Gender;
import org.apache.commons.lang.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Random;
import java.util.UUID;

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
    @Autowired
    private StoreService storeService;
    @Autowired
    private RepresentService representService;
    @Autowired
    private ItemService itemService;
    @Autowired
    private ItemRepository itemRepository;
    @Autowired
    private ResourceService resourceService;

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

    @Autowired
    private StoreItemService storeItemService;

    @Override
    public void initStagingEnv() throws IOException {
        int count = 20;
        if (loginRepository.count() <= count) {
            // 在staging 中 建立足够多的测试帐号
            while (count-- > 0)
                createDemoUser();
        }
        if (itemRepository.count() < 2) {
            generateStagingData();
        }
    }

    @Autowired
    private Environment environment;

    @Override
    public void init() throws IOException {
        if (environment.acceptsProfiles("staging")) {
            initStagingEnv();
        }
    }

    @Override
    public Object[] generateStagingData() throws IOException {
        Login merchantLogin = createDemoUser();
        Merchant merchant = new Merchant();
        merchant.setName("staging商户");
        merchant.setContact("staging商户联系人");
        merchant.setTelephone(merchantLogin.getLoginName());
        merchant.setAddress(address("staging地址"));
//        merchant.
        merchant = merchantService.addMerchant(merchantLogin.getId(), merchant);

        Login storeLogin = createDemoUser();
        Store store = storeService.addStore(storeLogin.getId(), merchant.getId(), "staging门店", storeLogin.getLoginName()
                , "staging商户门店", address("staging门店地址"));
        // 门店代表
        Login representLogin = createDemoUser();
        Represent represent = representService.addRepresent(representLogin.getId(), store.getId());
        // 项目
        Item[] items = new Item[]{
                createItem(merchant, "ok", new BigDecimal("1000"), AuditStatus.AUDIT_PASS, true, store, true)
                , createItem(merchant, "门店项目!enabled", new BigDecimal("500"), AuditStatus.AUDIT_PASS, true, store, false)
                , createItem(merchant, "项目!enabled", new BigDecimal("600"), AuditStatus.AUDIT_PASS, false, store, true)
                , createItem(merchant, "审核下架了", new BigDecimal("700"), AuditStatus.TO_AUDIT, true, store, true)
                , createItem(merchant, "没有门店", new BigDecimal("800"), AuditStatus.AUDIT_PASS, true, null, true)
        };
        return new Object[]{
                merchant, store, represent, items
        };
    }

    @Autowired
    private StoreItemRepository storeItemRepository;

    private Item createItem(Merchant merchant, String name, BigDecimal cost, AuditStatus status, boolean enabled
            , Store store, boolean storeEnabled) throws IOException {
        String path = "tmp/" + UUID.randomUUID().toString().replaceAll("-", "") + ".jpg";
        try (InputStream stream = new ClassPathResource("simpleItem.jpg").getInputStream()) {
            resourceService.uploadResource(path, stream);
        }
        Item item = itemService.addItem(merchant, path, "staging项目" + name, "staging"
                , cost.multiply(BigDecimal.valueOf(5)), cost.multiply(BigDecimal.valueOf(2)), cost
                , "一个好项目", "<p>真的是一个好项目</p>", false);
        item.setAuditStatus(AuditStatus.AUDIT_PASS);
        item.setEnabled(enabled);
        item = itemRepository.save(item);
        if (store != null) {
            StoreItem storeItem = storeItemService.addStoreItem(store.getId(), item.getId(), null, false);
            storeItem.setEnable(storeEnabled);
            storeItem = storeItemRepository.save(storeItem);
        }
        item.setAuditStatus(status);
        item = itemRepository.save(item);
        return item;
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
