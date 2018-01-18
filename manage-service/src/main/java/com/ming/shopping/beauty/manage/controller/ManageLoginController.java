package com.ming.shopping.beauty.manage.controller;

import com.ming.shopping.beauty.service.entity.login.*;
import com.ming.shopping.beauty.service.exception.ApiResultException;
import com.ming.shopping.beauty.service.model.ApiResult;
import com.ming.shopping.beauty.service.model.ResultCodeEnum;
import com.ming.shopping.beauty.service.service.LoginService;
import me.jiangcai.crud.controller.AbstractCrudController;
import me.jiangcai.crud.row.FieldDefinition;
import me.jiangcai.crud.row.RowDefinition;
import me.jiangcai.crud.row.field.FieldBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.persistence.criteria.Predicate;
import javax.websocket.server.PathParam;
import java.text.MessageFormat;
import java.util.*;

import static com.ming.shopping.beauty.service.entity.login.Login_.delete;

/**
 * @author helloztt
 */
@Controller
@RequestMapping("/login")
public class ManageLoginController extends AbstractCrudController<Login, Long> {

    @Autowired
    private LoginService loginService;

    @Override
    @PreAuthorize("hasAnyRole('ROOT', '" + Login.ROLE_MERCHANT_ROOT + "','" + Login.ROLE_STORE_ROOT + "')")
    public Object getOne(Long aLong) {
        return super.getOne(aLong);
    }

    @PutMapping("/{loginId}")
    @Transactional(rollbackFor = RuntimeException.class)
    @PreAuthorize("hasAnyRole('ROOT')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void setEnable(@PathVariable("loginId") long loginId, Map<String, Boolean> putData) {
        final String param = "enable";
        if (putData.get(param) != null) {
            loginService.freezeOrEnable(loginId, putData.get(param));
        } else {
            throw new ApiResultException(ApiResult.withCodeAndMessage(ResultCodeEnum.REQUEST_DATA_ERROR.getCode()
                    , MessageFormat.format(ResultCodeEnum.REQUEST_DATA_ERROR.getMessage(), param), null));
        }
    }

    @Override
    protected List<FieldDefinition<Login>> listFields() {
        return Arrays.asList(
                FieldBuilder.asName(Login.class, "loginId")
                        .addSelect(root -> root.get(Login_.id))
                        .build()
                , FieldBuilder.asName(Login.class, "username")
                        .addSelect(root -> root.get(Login_.nickName))
                        .build()
                , FieldBuilder.asName(Login.class, "mobile")
                        .addSelect(root -> root.get(Login_.loginName))
                        .build()
                , FieldBuilder.asName(Login.class, "enabled")
                        .build()
        );
    }

    @Override
    protected Specification<Login> listSpecification(Map<String, Object> queryData) {
        return (root, query, cb) -> {
            List<Predicate> conditions = new ArrayList<>();
            conditions.add(cb.equal(root.get(Login_.delete), false));
            if (queryData.get("loginId") != null) {
                conditions.add(cb.equal(root.get(Login_.id), queryData.get("loginId")));
            }
            // TODO: 2018/1/18 这里判断请求字段并设置查询条件
            return cb.and(conditions.toArray(new Predicate[conditions.size()]));
        };
    }

    @Override
    @PreAuthorize("denyAll()")
    public RowDefinition<Login> getDetail(Long aLong) {
        return null;
    }

    @Override
    @PreAuthorize("denyAll()")
    public void deleteOne(Long aLong) {

    }
}
