package com.ming.shopping.beauty.manage.controller;

import com.ming.shopping.beauty.service.entity.login.Login;
import com.ming.shopping.beauty.service.entity.login.Login_;
import com.ming.shopping.beauty.service.entity.support.ManageLevel;
import com.ming.shopping.beauty.service.exception.ApiResultException;
import com.ming.shopping.beauty.service.model.ApiResult;
import com.ming.shopping.beauty.service.model.ResultCodeEnum;
import com.ming.shopping.beauty.service.service.LoginService;
import me.jiangcai.crud.controller.AbstractCrudController;
import me.jiangcai.crud.row.FieldDefinition;
import me.jiangcai.crud.row.field.FieldBuilder;
import me.jiangcai.crud.row.field.Fields;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionService;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.persistence.criteria.Predicate;
import java.net.URISyntaxException;
import java.text.MessageFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 超级管理员
 *
 * @author helloztt
 */
@Controller
@RequestMapping("/manage")
@PreAuthorize("hasAnyRole('ROOT')")
public class ManageController extends AbstractCrudController<Login, Long> {
    @Autowired
    private LoginService loginService;
    @Autowired
    private ConversionService conversionService;

    @PreAuthorize("denyAll()")
    @Override
    public ResponseEntity addOne(Login postData, Map<String, Object> otherData) throws URISyntaxException {
        return null;
    }

    @PreAuthorize("denyAll()")
    @Override
    public void deleteOne(Long aLong) {
        super.deleteOne(aLong);
    }

    /**
     * 设置某角色有管理员权限
     *
     * @param id 角色编号
     */
    @PutMapping("/{id}/manageable/on")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Transactional(rollbackFor = RuntimeException.class)
    public void manageable(@PathVariable long id, Map<String, Boolean> putData) {
        final String param = "manageable";
        if (putData.get(param) != null) {
            loginService.upOrDowngradeToRoot(id,putData.get(param));
        } else {
            throw new ApiResultException(ApiResult.withCodeAndMessage(ResultCodeEnum.REQUEST_DATA_ERROR.getCode()
                    , MessageFormat.format(ResultCodeEnum.REQUEST_DATA_ERROR.getMessage(), param), null));
        }
    }


    @Override
    protected List<FieldDefinition<Login>> listFields() {
        return Arrays.asList(
                Fields.asBasic("id")
                , FieldBuilder.asName(Login.class, "username")
                        .addSelect(loginRoot -> loginRoot.get(Login_.loginName))
                        .build()
                , Fields.asBasic("enabled")
                , FieldBuilder.asName(Login.class, "level")
                        .addSelect(loginRoot -> loginRoot.get(Login_.levelSet))
                        .addFormat((data, type) -> {
                            Set<ManageLevel> levelSet = (Set<ManageLevel>) data;
                            return levelSet.stream().map(ManageLevel::title).collect(Collectors.joining(","));
                        })
                        .build()
                , FieldBuilder.asName(Login.class, "createtime")
                        .addSelect(loginRoot -> loginRoot.get(Login_.createTime))
                        .addFormat((data, type) -> conversionService.convert(data, String.class))
                        .build()
        );
    }

    @Override
    protected Specification<Login> listSpecification(Map<String, Object> queryData) {
        return (root, cq, cb) -> {
            List<Predicate> conditionList = new ArrayList<>();
            conditionList.add(cb.isTrue(root.get(Login_.manageable)));
            if (queryData.get("username") != null) {
                conditionList.add(cb.equal(root.get(Login_.loginName), queryData.get("username")));
            }
            return cb.and(conditionList.toArray(new Predicate[conditionList.size()]));
        };
    }

}