package com.ming.shopping.beauty.client.controller;

import com.google.zxing.WriterException;
import com.ming.shopping.beauty.service.entity.login.Login;
import com.ming.shopping.beauty.service.entity.login.Login_;
import com.ming.shopping.beauty.service.entity.login.User;
import com.ming.shopping.beauty.service.entity.login.User_;
import com.ming.shopping.beauty.service.entity.order.MainOrder;
import com.ming.shopping.beauty.service.exception.ApiResultException;
import com.ming.shopping.beauty.service.model.ApiResult;
import com.ming.shopping.beauty.service.model.ResultCodeEnum;
import com.ming.shopping.beauty.service.service.MainOrderService;
import com.ming.shopping.beauty.service.service.QRCodeService;
import com.ming.shopping.beauty.service.service.SystemService;
import me.jiangcai.crud.row.FieldDefinition;
import me.jiangcai.crud.row.RowCustom;
import me.jiangcai.crud.row.RowDefinition;
import me.jiangcai.crud.row.field.FieldBuilder;
import me.jiangcai.crud.row.supplier.SingleRowDramatizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.persistence.criteria.JoinType;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author helloztt
 */
@Controller("clientUserController")
@RequestMapping("/user")
public class UserController {
    @Autowired
    protected MainOrderService orderService;
    @Autowired
    private SystemService systemService;
    @Autowired
    private QRCodeService qrCodeService;

    /**
     * 获取当前登录用户信息
     *
     * @param login
     * @return
     */
    @GetMapping
    @RowCustom(distinct = true, dramatizer = SingleRowDramatizer.class)
    public RowDefinition<Login> userBaseInfo(@AuthenticationPrincipal Login login) {
        return new RowDefinition<Login>() {
            @Override
            public Class<Login> entityClass() {
                return Login.class;
            }

            @Override
            public List<FieldDefinition<Login>> fields() {
                return listFields();
            }

            @Override
            public Specification<Login> specification() {
                return (root, cq, cb) ->
                        cb.equal(root.get(Login_.id), login.getId());
            }
        };
    }

    /**
     * 用来给门店代表扫码的用户二维码
     *
     * @param login
     * @return
     */
    @GetMapping("/vipCard")
    @ResponseBody
    public Object vipCard(@AuthenticationPrincipal Login login, HttpServletResponse response) throws IOException, WriterException {
        //未激活的用户没有二维码
        if(!login.getUser().isActive()){
            throw new ApiResultException(ApiResult.withError(ResultCodeEnum.USER_NOT_ACTIVE));
        }
        MainOrder mainOrder = orderService.newEmptyOrder(login.getUser());
        response.setHeader("X-Order-Id", String.valueOf(mainOrder.getOrderId()));
        Map<String,Object> result = new HashMap<>(1);
        // TODO: 2018/1/12 这里要确定购物车地址
        String text = systemService.toUrl("/" + mainOrder.getOrderId());
        BufferedImage qrCode = qrCodeService.generateQRCode(text);
        result.put("vipCard",login.getUser().getCardNo());
        result.put("qrCode",text);
        return result;
    }

    private List<FieldDefinition<Login>> listFields() {
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
                , FieldBuilder.asName(Login.class, "isRepresent")
                        .addBiSelect((loginRoot, cb) -> cb.<Boolean>selectCase().when(cb.isNull(loginRoot.get(Login_.represent)), Boolean.FALSE)
                                .otherwise(Boolean.TRUE))
                        .build()
        );
    }
}
