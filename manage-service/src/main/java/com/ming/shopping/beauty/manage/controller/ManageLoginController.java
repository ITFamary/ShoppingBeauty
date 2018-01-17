package com.ming.shopping.beauty.manage.controller;

import com.ming.shopping.beauty.service.entity.login.*;
import com.ming.shopping.beauty.service.entity.support.ManageLevel;
import com.ming.shopping.beauty.service.exception.ApiResultException;
import com.ming.shopping.beauty.service.model.ApiResult;
import com.ming.shopping.beauty.service.model.ResultCodeEnum;
import com.ming.shopping.beauty.service.model.response.LoginDetailResponse;
import com.ming.shopping.beauty.service.service.LoginService;
import com.ming.shopping.beauty.service.service.MerchantService;
import com.ming.shopping.beauty.service.service.RepresentService;
import com.ming.shopping.beauty.service.service.StoreService;
import me.jiangcai.crud.controller.AbstractCrudController;
import me.jiangcai.crud.row.FieldDefinition;
import me.jiangcai.crud.row.RowCustom;
import me.jiangcai.crud.row.RowDefinition;
import me.jiangcai.crud.row.field.FieldBuilder;
import me.jiangcai.crud.row.supplier.JQueryDataTableDramatizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.websocket.server.PathParam;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.ming.shopping.beauty.service.entity.login.Login_.delete;

@Controller
@RequestMapping("/login")
public class ManageLoginController extends AbstractCrudController<Login, Long> {

    @Autowired
    private LoginService loginService;
    @Autowired
    private StoreService storeService;
    @Autowired
    private MerchantService merchantService;
    @Autowired
    private RepresentService representService;
    /**
     * 查看用户详情
     * @param loginId
     * @return
     */
    @GetMapping("/{loginId}")
    @Transactional
    public LoginDetailResponse loginDetail(@PathParam("loginId")Long loginId){
        Login login = loginService.findOne(loginId);
        final User user = login.getUser();
        final Store store = login.getStore();
        final  Represent represent = login.getRepresent();
        return new LoginDetailResponse(login.getId(),login.getNickName(),login.getUsername(),login.isEnabled());
    }



    @PutMapping("/{loginId}/enable")
    @Transactional
    public void setEnable(@PathVariable("loginId")long loginId,boolean enabled){
        Login login = loginService.findOne(loginId);
        login.setEnabled(enabled);
    }

    @PostMapping("/login/{loginId}/setManager")
    @Transactional
    public void setManager(@PathVariable("loginId")long loginId,long storeId,long merchantId,int identity){
        final Login login ;
        if(identity == 0){
            //商户管理员
            merchantService.addMerchant(loginId,merchantId);
        }else if(identity == 1){
            //商户操作员
            login = loginService.findOne(loginId);
            login.getLevelSet().add(ManageLevel.merchantManager);
        }else if(identity == 2){
            //门店管理员
            storeService.addStore(loginId,storeId);
        }else if(identity == 3){
            //门店代表
            representService.addRepresent(loginId,storeId);
        }else{
            throw new ApiResultException(ApiResult.withError(ResultCodeEnum.MANAGER_NOT_EXIST));
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
        return (root,query,cb)-> cb.equal(root.get(Login_.delete),false);
    }

    @Override
    @PreAuthorize("denyAll()")
    public RowDefinition<Login> getDetail(Long aLong) {
        return super.getDetail(aLong);
    }

    @Override
    @PreAuthorize("denyAll()")
    public void deleteOne(Long aLong) {
        super.deleteOne(aLong);
    }
}
