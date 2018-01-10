package com.ming.shopping.beauty.client.controller;

import com.ming.shopping.beauty.service.entity.login.Login;
import com.ming.shopping.beauty.service.entity.login.Login_;
import com.ming.shopping.beauty.service.entity.login.User_;
import com.ming.shopping.beauty.service.service.LoginService;
import me.jiangcai.crud.row.FieldDefinition;
import me.jiangcai.crud.row.RowCustom;
import me.jiangcai.crud.row.RowDefinition;
import me.jiangcai.crud.row.field.FieldBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.persistence.criteria.Expression;
import javax.persistence.criteria.JoinType;
import java.util.Arrays;
import java.util.List;

/**
 * @author helloztt
 */
@Controller("clientUserController")
@RequestMapping("/user")
public class UserController {
    @Autowired
    private LoginService loginService;

    @GetMapping
    @RowCustom(distinct = true)
    public RowDefinition<Login> userBaseInfo(@AuthenticationPrincipal Login login) {
        return new RowDefinition<Login>() {
            @Override
            public Class<Login> entityClass() {
                return Login.class;
            }

            @Override
            public List<FieldDefinition<Login>> fields() {
                return Arrays.asList(
                        FieldBuilder.asName(Login.class, "avatar")
                                .addSelect(loginRoot -> loginRoot.join(Login_.wechatUser, JoinType.LEFT).get("headImageUrl"))
                                .build()
                        , FieldBuilder.asName(Login.class, "name")
                                .addSelect(loginRoot -> loginRoot.join(Login_.user, JoinType.LEFT).get(User_.familyName))
                                .build()
                        , FieldBuilder.asName(Login.class, "mobile")
                                .addSelect(loginRoot -> loginRoot.get(Login_.loginName))
                                .build()
                        , FieldBuilder.asName(Login.class, "balance")
                                .addSelect(loginRoot -> loginRoot.join(Login_.user, JoinType.LEFT).get(User_.currentAmount))
                                .build()
                        , FieldBuilder.asName(Login.class, "isMember")
                                .addSelect(loginRoot -> loginRoot.join(Login_.user, JoinType.LEFT).get(User_.active))
                                .build()
                        // TODO: 2018/1/11  
//                        , FieldBuilder.asName(Login.class, "isRepresent")
//                                .addBiSelect((loginRoot, cb) -> cb.function("IF", Boolean.class, cb.isNull(loginRoot.get(Login_.represent))
//                                        , cb.literal(Boolean.TRUE), cb.literal(Boolean.FALSE)))
//                                .build()
                );
            }

            @Override
            public Specification<Login> specification() {
                return (root, cq, cb) ->
                        cb.equal(root.get(Login_.id), login.getId());
            }
        };
    }
}
