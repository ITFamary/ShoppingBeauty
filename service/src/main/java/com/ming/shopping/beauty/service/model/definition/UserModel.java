package com.ming.shopping.beauty.service.model.definition;

import com.ming.shopping.beauty.service.entity.login.Login;
import com.ming.shopping.beauty.service.entity.login.Login_;
import com.ming.shopping.beauty.service.entity.login.Store_;
import com.ming.shopping.beauty.service.entity.login.User;
import com.ming.shopping.beauty.service.entity.login.User_;
import com.ming.shopping.beauty.service.service.LoginService;
import lombok.Getter;
import me.jiangcai.crud.row.FieldDefinition;
import me.jiangcai.crud.row.field.FieldBuilder;
import me.jiangcai.crud.row.field.Fields;
import me.jiangcai.wx.model.Gender;
import me.jiangcai.wx.standard.entity.StandardWeixinUser;
import org.springframework.core.convert.ConversionService;
import org.springframework.util.StringUtils;

import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

/**
 * @author helloztt
 */
@Getter
public class UserModel implements DefinitionModel<Login> {

    private final List<FieldDefinition<Login>> definitions;

    public UserModel(LoginService loginService, ConversionService conversionService) {
        super();
        definitions = Arrays.asList(
                Fields.asBasic("id")
                , FieldBuilder.asName(Login.class, "mobile")
                        .addSelect(loginRoot -> loginRoot.get(Login_.loginName))
                        .build()
                , FieldBuilder.asName(Login.class, "name")
                        .addBiSelect((loginRoot, criteriaBuilder) -> {
                            Join<?, User> userJoin = loginRoot.join(Login_.user, JoinType.LEFT);
                            Join<?, StandardWeixinUser> wxJoin = loginRoot.join(Login_.wechatUser, JoinType.LEFT);
                            return criteriaBuilder.selectCase(criteriaBuilder.isNull(userJoin))
                                    .when(true, wxJoin.get("nickname"))
                                    .otherwise(userJoin.get(User_.familyName));
                        })
                        .addEntityFunction(login -> {
                            String defaultNickName;
                            if (login.getWechatUser() == null)
                                defaultNickName = null;
                            else
                                defaultNickName = login.getWechatUser().getNickname();
                            if (login.getUser() == null) {
                                return defaultNickName;
                            }
                            String nickName;
                            Gender gender = login.getUser().getGender();
                            if (!StringUtils.isEmpty(login.getUser().getFamilyName())) {
                                String genderStr = gender == null ? "未知" :
                                        (Gender.male.equals(gender) ? "先生" : "女士");
                                nickName = login.getUser().getFamilyName() + genderStr;
                            } else {
                                nickName = defaultNickName;
                            }
                            return nickName;
                        })
                        .build()
                , FieldBuilder.asName(Login.class, "wxNickName")
                        .addSelect(loginRoot -> loginRoot.join(Login_.wechatUser, JoinType.LEFT).get("nickname"))
                        .addEntityFunction(login -> login.getWechatUser() != null ? login.getWechatUser().getNickname() : null)
                        .build()
                , FieldBuilder.asName(Login.class, "avatar")
                        .addSelect(loginRoot -> loginRoot.join(Login_.wechatUser, JoinType.LEFT).get("headImageUrl"))
                        .addEntityFunction(login -> login.getWechatUser() != null ? login.getWechatUser().getHeadImageUrl() : null)
                        .build()
                , FieldBuilder.asName(Login.class, "createTime")
                        .addFormat((data, type) -> conversionService.convert(data, String.class))
                        .build()
                , FieldBuilder.asName(Login.class, "balance")
//                        .addBiSelect(Login::getCurrentBalanceExpr)
                        .addBiSelect((loginRoot, criteriaBuilder) -> criteriaBuilder.literal(BigDecimal.ZERO))
                        .addEntityFunction(login -> login.getUser() == null ? 0 : loginService.findBalance(login.getUser().getId()))
                        .build()
//                , FieldBuilder.asName(Login.class, "consumption")
//                        .build()
                , FieldBuilder.asName(Login.class, "guidable")
                        .addSelect(loginRoot -> loginRoot.get(Login_.guidable))
                        .build()
                , FieldBuilder.asName(Login.class, "storeId")
                        .addBiSelect((loginRoot, cb) -> cb.<Long>selectCase().when(cb.isNull(loginRoot.get(Login_.store)), 0L)
                                .otherwise(cb.<Long>selectCase()
                                        .when(cb.isNull(loginRoot.join(Login_.store, JoinType.LEFT).get(Store_.store)), loginRoot.get(Login_.id))
                                        .otherwise(loginRoot.join(Login_.store, JoinType.LEFT).join(Store_.store, JoinType.LEFT).get(Store_.id))))
                        .addEntityFunction(login -> login.getStore() == null ? "" : login.getStore().getStoreId())
                        .build()
                , FieldBuilder.asName(Login.class, "enabled")
                        .addSelect(loginRoot -> loginRoot.join(Login_.store, JoinType.LEFT).get(Store_.enabled))
                        .addEntityFunction(login -> login.getStore() == null ? login.isEnabled() : login.getStore().isEnabled())
                        .build()
        );
    }
}
