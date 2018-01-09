package com.ming.shopping.beauty.service;

import com.ming.shopping.beauty.service.config.ServiceConfig;
import com.ming.shopping.beauty.service.entity.login.Login;
import com.ming.shopping.beauty.service.entity.login.Merchant;
import com.ming.shopping.beauty.service.repository.MerchantRepository;
import com.ming.shopping.beauty.service.service.LoginService;
import com.ming.shopping.beauty.service.service.MerchantService;
import me.jiangcai.lib.test.SpringWebTest;
import me.jiangcai.wx.model.Gender;
import me.jiangcai.wx.model.WeixinUserDetail;
import me.jiangcai.wx.test.WeixinTestConfig;
import me.jiangcai.wx.test.WeixinUserMocker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;

/**
 * @author helloztt
 */
@ActiveProfiles({ServiceConfig.PROFILE_TEST, ServiceConfig.PROFILE_UNIT_TEST})
@ContextConfiguration(classes = {CoreServiceTestConfig.class})
@WebAppConfiguration
public abstract class CoreServiceTest extends SpringWebTest {
    @Autowired
    protected WeixinTestConfig weixinTestConfig;
    @Autowired
    protected MerchantService merchantService;
    @Autowired
    protected LoginService loginService;

    private static final String firstName = "赵钱孙李周吴郑王冯陈褚卫蒋沈韩杨朱秦尤许何吕施张孔曹严华金魏陶姜戚谢邹喻柏水窦章云苏潘葛奚范彭郎鲁韦昌马苗凤花方俞任袁柳酆鲍史唐费廉岑薛雷贺倪汤滕殷罗毕郝邬安常乐于时傅皮卞齐康伍余元卜顾孟平黄和穆萧尹姚邵湛汪祁毛禹狄米贝明臧计伏成戴谈宋茅庞熊纪舒屈项祝董梁杜阮蓝闵席季麻强贾路娄危江童颜郭梅盛林刁钟徐邱骆高夏蔡田樊胡凌霍虞万支柯咎管卢莫经房裘缪干解应宗宣丁贲邓郁单杭洪包诸左石崔吉钮龚程嵇邢滑裴陆荣翁荀羊於惠甄魏加封芮羿储靳汲邴糜松井段富巫乌焦巴弓牧隗山谷车侯宓蓬全郗班仰秋仲伊宫宁仇栾暴甘钭厉戎祖武符刘姜詹束龙叶幸司韶郜黎蓟薄印宿白怀蒲台从鄂索咸籍赖卓蔺屠蒙池乔阴郁胥能苍双闻莘党翟谭贡劳逄姬申扶堵冉宰郦雍却璩桑桂濮牛寿通边扈燕冀郏浦尚农温别庄晏柴瞿阎充慕连茹习宦艾鱼容向古易慎戈廖庚终暨居衡步都耿满弘匡国文寇广禄阙东殴殳沃利蔚越夔隆师巩厍聂晁勾敖融冷訾辛阚那简饶空曾毋沙乜养鞠须丰巢关蒯相查后江红游竺权逯盖益桓公万俟司马上官欧阳夏侯诸葛闻人东方赫连皇甫尉迟公羊澹台公冶宗政濮阳淳于仲孙太叔申屠公孙乐正轩辕令狐钟离闾丘长孙慕容鲜于宇文司徒司空亓官司寇仉督子车颛孙端木巫马公西漆雕乐正壤驷公良拓拔夹谷宰父谷粱晋楚阎法汝鄢涂钦段干百里东郭南门呼延归海羊舌微生岳帅缑亢况后有琴梁丘左丘东门西门商牟佘佴伯赏南宫墨哈谯笪年爱阳佟第五言福百家姓续";

    protected static final String RESULT_CODE_PATH = "$.resultCode";
    protected static final String RESULT_DATA_PATH = "$.data";


    /**
     * @return 生成一个新的微信帐号，并且应用在系统中
     */
    protected WeixinUserDetail nextCurrentWechatAccount() {
        WeixinUserDetail detail = WeixinUserMocker.randomWeixinUserDetail();
        weixinTestConfig.setNextDetail(detail);
        return detail;
    }

    /**
     * 随机生成一个登陆用户
     *
     * @return
     */
    protected Login mockLogin() {
        return mockLogin(null, null);
    }

    /**
     * 生成一个有推荐人的用户
     *
     * @param guideUserId
     * @return
     */
    protected Login mockLogin(long guideUserId) {
        return mockLogin(null, guideUserId);
    }

    /**
     * 生成一个登陆用户
     *
     * @param cardNo      卡密
     * @param guideUserId 推荐人
     * @return
     */
    protected Login mockLogin(String cardNo, Long guideUserId) {
        WeixinUserDetail weixinUserDetail = nextCurrentWechatAccount();
        String mobile = randomMobile();
        return loginService.getLogin(weixinUserDetail.getOpenId(), mobile,
                "1234", randomChinese(1), randomEnum(Gender.class), cardNo, guideUserId);
    }

    /**
     * 生成一个商户
     *
     * @return
     */
    protected Merchant mockMerchant() {
        Login login = mockLogin();
        return merchantService.addMerchant(login.getId(), randomChinese(5)
                , randomMobile(), randomChinese(3), null);
    }

    /**
     * 生成一个商户管理员
     *
     * @param merchantId 所属商户
     * @return
     */
    protected Merchant mockMerchant(long merchantId) {
        Login login = mockLogin();
        return merchantService.addMerchant(login.getId(), merchantId);
    }

    /**
     * 随机中文
     *
     * @param length
     * @return
     */
    protected String randomChinese(int length) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < length; i++) {
            int index = random.nextInt(firstName.length());
            sb.append(firstName.substring(index, index + 1));
        }
        return sb.toString();
    }

    /**
     * 随机枚举
     *
     * @param enumCls
     * @param <T>
     * @return
     */
    protected <T extends Enum> T randomEnum(Class<T> enumCls) {
        T[] enumArr = enumCls.getEnumConstants();
        return enumArr[random.nextInt(enumArr.length)];
    }
}
