package com.ming.shopping.beauty.service.model.definition;

import com.ming.shopping.beauty.service.entity.login.Login;
import com.ming.shopping.beauty.service.entity.login.Login_;
import com.ming.shopping.beauty.service.entity.login.Store_;
import com.ming.shopping.beauty.service.entity.login.User_;
import lombok.Getter;
import me.jiangcai.crud.row.FieldDefinition;
import me.jiangcai.crud.row.field.FieldBuilder;
import me.jiangcai.crud.row.field.Fields;
import me.jiangcai.wx.model.Gender;
import org.springframework.util.StringUtils;

import javax.persistence.criteria.JoinType;
import java.util.Arrays;
import java.util.List;

/**
 * @author helloztt
 */
@Getter
public class UserModel implements DefinitionModel<Login> {

    private final List<FieldDefinition<Login>> definitions;

    public UserModel() {
        super();
        definitions = Arrays.asList(
                Fields.asBasic("id")
                , FieldBuilder.asName(Login.class, "mobile")
                        .addSelect(loginRoot -> loginRoot.get(Login_.loginName))
                        .build()
                , FieldBuilder.asName(Login.class, "name")
                        .addSelect(loginRoot -> loginRoot.get(Login_.nickName))
                        .addEntityFunction(login -> {
                            String nickName;
                            if (login.getUser() == null) {
                                return null;
                            }
                            Gender gender = login.getUser().getGender();
                            if (!StringUtils.isEmpty(login.getUser().getFamilyName())) {
                                String genderStr = gender == null ? "未知" :
                                        (Gender.male.equals(gender) ? "先生" : "女士");
                                nickName = login.getUser().getFamilyName() + genderStr;
                            } else {
                                nickName = login.getNickName();
                            }
                            return nickName;
                        })
                        .build()
                , FieldBuilder.asName(Login.class, "avatar")
                        .addSelect(loginRoot -> loginRoot.join(Login_.wechatUser, JoinType.LEFT).get("headImageUrl"))
                        .addEntityFunction(login -> login.getWechatUser() != null ? login.getWechatUser().getHeadImageUrl() : null)
                        .build()
                , FieldBuilder.asName(Login.class, "balance")
                        .addSelect(loginRoot -> loginRoot.join(Login_.user, JoinType.LEFT).get(User_.currentAmount))
                        .addEntityFunction(login -> login.getUser() == null ? null : login.getUser().getCurrentAmount())
                        .build()
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
                        .addEntityFunction(login -> login.getStore() == null ? "" : login.getStore().isEnabled())
                        .build()
        );
    }
}
