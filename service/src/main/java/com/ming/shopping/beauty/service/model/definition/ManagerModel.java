package com.ming.shopping.beauty.service.model.definition;

import com.ming.shopping.beauty.service.entity.login.Login;
import com.ming.shopping.beauty.service.entity.login.Login_;
import com.ming.shopping.beauty.service.entity.login.Merchant;
import com.ming.shopping.beauty.service.entity.login.Merchant_;
import com.ming.shopping.beauty.service.entity.login.Store;
import com.ming.shopping.beauty.service.entity.login.Store_;
import com.ming.shopping.beauty.service.entity.support.ManageLevel;
import lombok.Getter;
import me.jiangcai.crud.row.FieldDefinition;
import me.jiangcai.crud.row.field.FieldBuilder;
import me.jiangcai.crud.row.field.Fields;
import org.springframework.core.convert.ConversionService;
import org.springframework.security.core.GrantedAuthority;

import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author CJ
 */
@Getter
public class ManagerModel implements DefinitionModel<Login> {

    private final List<FieldDefinition<Login>> definitions;

    @SuppressWarnings("unchecked")
    public ManagerModel(ConversionService conversionService) {
        super();
        definitions = Arrays.asList(
                Fields.asBasic("id")
                , FieldBuilder.asName(Login.class, "username")
                        .addSelect(loginRoot -> loginRoot.get(Login_.loginName))
                        .build()
                , FieldBuilder.asName(Login.class, "nickname")
                        .addSelect(loginRoot -> loginRoot.get(Login_.nickName))
                        .build()
                , Fields.asBasic("enabled")
                , FieldBuilder.asName(Login.class, "authorities")
                        .addSelect(loginRoot -> loginRoot.get(Login_.levelSet))
                        .addFormat((data, type) -> {
                            if (data == null)
                                return null;
                            Set<ManageLevel> levelSet = (Set<ManageLevel>) data;
                            return Login.getGrantedAuthorities(levelSet).stream()
                                    .map(GrantedAuthority::getAuthority)
                                    .collect(Collectors.toSet());
                        })
                        .build()
                , FieldBuilder.asName(Login.class, "level")
                        .addSelect(loginRoot -> loginRoot.get(Login_.levelSet))
                        .addFormat((data, type) -> {
                            if (data == null)
                                return null;
                            Set<ManageLevel> levelSet = (Set<ManageLevel>) data;
                            return levelSet.stream().map(ManageLevel::title).collect(Collectors.joining(","));
                        })
                        .build()
                , FieldBuilder.asName(Login.class, "merchantId")
                        .addBiSelect((loginRoot, cb)
                                        -> {
                                    final Join<Login, Merchant> merchantJoin = loginRoot.join(Login_.merchant, JoinType.LEFT);
                                    return cb.selectCase(merchantJoin.get(Merchant_.manageable))
                                            .when(true, merchantJoin.get(Merchant_.id))
                                            .otherwise(merchantJoin.get(Merchant_.merchant).get(Merchant_.id));
                                }
                        )
                        .addEntityFunction(login -> login.getMerchant() == null ? null : login.getMerchant().getMerchantId())
                        .build()
                , FieldBuilder.asName(Login.class, "storeId")
                        .addBiSelect((loginRoot, cb)
                                        -> {
                                    final Join<Login, Store> storeJoin = loginRoot.join(Login_.store, JoinType.LEFT);
                                    return cb.selectCase(storeJoin.get(Store_.manageable))
                                            .when(true, storeJoin.get(Store_.id))
                                            .otherwise(storeJoin.get(Store_.store).get(Store_.id));
                                }
                        )
                        .addEntityFunction(login -> login.getStore() == null ? null : login.getStore().getStoreId())
                        .build()
                , FieldBuilder.asName(Login.class, "createTime")
                        .addSelect(loginRoot -> loginRoot.get(Login_.createTime))
                        .addFormat((data, type) -> conversionService.convert(data, String.class))
                        .build()
        );
    }
}
