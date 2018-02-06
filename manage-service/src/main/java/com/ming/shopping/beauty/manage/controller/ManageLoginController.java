package com.ming.shopping.beauty.manage.controller;

import com.ming.shopping.beauty.service.entity.login.Login;
import com.ming.shopping.beauty.service.entity.login.Login_;
import com.ming.shopping.beauty.service.entity.login.User_;
import com.ming.shopping.beauty.service.exception.ApiResultException;
import com.ming.shopping.beauty.service.model.ApiResult;
import com.ming.shopping.beauty.service.model.ResultCodeEnum;
import com.ming.shopping.beauty.service.model.definition.UserModel;
import com.ming.shopping.beauty.service.repository.MainOrderRepository;
import com.ming.shopping.beauty.service.service.LoginService;
import me.jiangcai.crud.controller.AbstractCrudController;
import me.jiangcai.crud.row.FieldDefinition;
import me.jiangcai.crud.row.RowCustom;
import me.jiangcai.crud.row.RowDefinition;
import me.jiangcai.crud.row.RowService;
import me.jiangcai.crud.row.field.FieldBuilder;
import me.jiangcai.crud.row.supplier.AntDesignPaginationDramatizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.persistence.criteria.*;
import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;


/**
 * @author helloztt
 */
@Controller
@RequestMapping("/login")
@PreAuthorize("hasAnyRole('ROOT')")
@RowCustom(dramatizer = AntDesignPaginationDramatizer.class, distinct = true)
public class ManageLoginController extends AbstractCrudController<Login, Long> {

    @Autowired
    private LoginService loginService;
    @Autowired
    private MainOrderRepository mainOrderRepository;

    /**
     * 用户详情
     *
     * @param aLong
     * @return
     */
    @Override
    @PreAuthorize("hasAnyRole('ROOT', '" + Login.ROLE_MERCHANT_ROOT + "','" + Login.ROLE_STORE_ROOT + "')")
    @GetMapping("/{id}")
    @ResponseBody
    public Object getOne(@PathVariable("id") Long aLong) {
        Login login = loginService.findOne(aLong);
        BigDecimal consumption = mainOrderRepository.sumFinalAmountLByPayer(login.getId());
        if(consumption != null){
            login.setConsumption(consumption);
        }else{
            login.setConsumption(BigDecimal.ZERO);
        }
        return RowService.drawEntityToRow(login, new UserModel().getDefinitions(), null);
    }

    /**
     * 冻结/启用 用户
     *
     * @param loginId 被设置的用户
     * @param putData
     */
    @PutMapping("/{id}/enabled")
    @PreAuthorize("hasAnyRole('ROOT','" + Login.ROLE_MERCHANT_ROOT + "','" + Login.ROLE_STORE_ROOT + "')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void setEnable(@PathVariable(value = "id", required = true) long loginId, @RequestBody Boolean putData) {
        if (putData != null) {
            loginService.freezeOrEnable(loginId, putData);
        } else {
            throw new ApiResultException(ApiResult.withCodeAndMessage(ResultCodeEnum.REQUEST_DATA_ERROR.getCode()
                    , MessageFormat.format(ResultCodeEnum.REQUEST_DATA_ERROR.getMessage(), putData), null));
        }
    }

    /**
     * 设置一个用户是否可以推荐他人
     *
     * @param loginId 被设置的用户
     * @param putData
     */
    @PutMapping("/{id}/guidable")
    @PreAuthorize("hasAnyRole('ROOT','" + Login.ROLE_MERCHANT_ROOT + "','" + Login.ROLE_STORE_ROOT + "')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void setGuidable(@PathVariable(value = "id", required = true) long loginId, @RequestBody Boolean putData){
        if (putData != null) {
            loginService.setGuidable(loginId, putData);
        } else {
            throw new ApiResultException(ApiResult.withCodeAndMessage(ResultCodeEnum.REQUEST_DATA_ERROR.getCode()
                    , MessageFormat.format(ResultCodeEnum.REQUEST_DATA_ERROR.getMessage(), putData), null));
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
                , FieldBuilder.asName(Login.class, "active")
                        .addBiSelect((loginRoot, cb) -> cb.isNotNull(loginRoot.join(Login_.user).get(User_.cardNo)))
                        .build()
                , FieldBuilder.asName(Login.class, "currentAmount")
                        //TODO 余额这里还是有问题的.
                        .addSelect(loginRoot -> loginRoot.join(Login_.user, JoinType.LEFT).get(User_.currentAmount))
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
            if (queryData.get("enabled") != null) {
                if ((boolean) queryData.get("enabled")) {
                    conditions.add(cb.isTrue(root.get(Login_.enabled)));
                } else {
                    conditions.add(cb.isFalse(root.get(Login_.enabled)));
                }
            }
            if (queryData.get("mobile") != null) {
                conditions.add(cb.equal(root.get(Login_.loginName), queryData.get("mobile")));
            }
            return cb.and(conditions.toArray(new Predicate[conditions.size()]));
        };
    }

    @Override
    protected List<Order> listOrder(CriteriaBuilder criteriaBuilder, Root<Login> root) {
        return Arrays.asList(
                criteriaBuilder.desc(root.get(Login_.id))
        );
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
