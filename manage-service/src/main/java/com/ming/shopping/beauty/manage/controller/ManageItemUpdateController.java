package com.ming.shopping.beauty.manage.controller;

import com.ming.shopping.beauty.service.entity.item.Item;
import com.ming.shopping.beauty.service.entity.login.Login;
import com.ming.shopping.beauty.service.exception.ApiResultException;
import com.ming.shopping.beauty.service.model.ApiResult;
import com.ming.shopping.beauty.service.model.ResultCodeEnum;
import com.ming.shopping.beauty.service.service.ItemService;
import me.jiangcai.crud.controller.AbstractCrudController;
import me.jiangcai.crud.row.FieldDefinition;
import me.jiangcai.crud.row.RowDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Root;
import javax.servlet.http.HttpServletRequest;
import java.net.URISyntaxException;
import java.text.MessageFormat;
import java.util.List;
import java.util.Map;

/**
 * 这个controller仅仅是为了解决item中URL冲突而创建的,所以将额外的方法全部禁用了,测试还是在ManageItemControllerTest中
 *
 * @author lxf
 */
@Controller
@RequestMapping("/itemUpdater")
public class ManageItemUpdateController extends AbstractCrudController<Item,Long> {

    @Autowired
    private ItemService itemService;

    /**
     * 项目批量上架下架
     *
     * @param putData 上下架
     */
    @PutMapping("/enabled")
    @ResponseBody
    @PreAuthorize("hasAnyRole('ROOT', '" + Login.ROLE_MERCHANT_ITEM + "','" + Login.ROLE_PLATFORM_AUDIT_ITEM + "')")
    public ApiResult enabled(@RequestBody Map<String, Object> putData) {
        final String param = "enabled";
        final String items = "items";
        //失败的个数
        int count = 0;
        List<Integer> itemList = (List<Integer>) putData.get(items);
        //总个数
        int size = itemList.size();
        if (putData.get(param) != null || putData.get(items) != null) {
            if (itemList.size() != 0) {
                for (Integer id : itemList) {
                    try {
                        itemService.freezeOrEnable(Long.parseLong(id.toString()), (boolean) putData.get(param));
                    } catch (Exception e) {
                        count++;
                    }
                }
            } else {
                throw new ApiResultException(ApiResult.withCodeAndMessage(ResultCodeEnum.REQUEST_DATA_ERROR.getCode()
                        , MessageFormat.format(ResultCodeEnum.REQUEST_DATA_ERROR.getMessage(), items), null));
            }
        } else {
            throw new ApiResultException(ApiResult.withCodeAndMessage(ResultCodeEnum.REQUEST_DATA_ERROR.getCode()
                    , MessageFormat.format(ResultCodeEnum.REQUEST_DATA_ERROR.getMessage(), param), null));
        }
        return ApiResult.withOk("总数:" + size + ",成功数:" + (size - count) + ",失败数:" + count);
    }

    /**
     * 项目批量推荐/取消推荐
     * TODO: 讲道理商户应该改不了的吧……
     *
     * @param putData
     */
    @PutMapping("/recommended")
    @ResponseBody
    @PreAuthorize("hasAnyRole('ROOT', '" + Login.ROLE_PLATFORM_AUDIT_ITEM + "','" + Login.ROLE_MERCHANT_ITEM + "')")
    public ApiResult recommended(@RequestBody Map<String, Object> putData) {
        final String param = "recommended";
        final String items = "items";
        //失败的个数
        int count = 0;
        List<Integer> itemList = (List<Integer>) putData.get(items);
        //总个数
        int size = itemList.size();
        if (putData.get(param) != null) {
            if (itemList.size() != 0) {
                for (Integer id : itemList) {
                    itemService.recommended(Long.parseLong(id.toString()), (boolean) putData.get(param));
                }
            } else {
                throw new ApiResultException(ApiResult.withCodeAndMessage(ResultCodeEnum.REQUEST_DATA_ERROR.getCode()
                        , MessageFormat.format(ResultCodeEnum.REQUEST_DATA_ERROR.getMessage(), items), null));
            }
        } else {
            throw new ApiResultException(ApiResult.withCodeAndMessage(ResultCodeEnum.REQUEST_DATA_ERROR.getCode()
                    , MessageFormat.format(ResultCodeEnum.REQUEST_DATA_ERROR.getMessage(), param), null));
        }
        return ApiResult.withOk("总数:" + size + ",成功数:" + (size - count) + ",失败数:" + count);
    }


    @Override
    @PreAuthorize("denyAll()")
    protected List<FieldDefinition<Item>> listFields() {
        return null;
    }

    @Override
    @PreAuthorize("denyAll()")
    protected Specification<Item> listSpecification(Map<String, Object> queryData) {
        return null;
    }

    @Override
    @PreAuthorize("denyAll()")
    public Object getOne(Long aLong) {
        return super.getOne(aLong);
    }

    @Override
    @PreAuthorize("denyAll()")
    public RowDefinition<Item> getDetail(Long aLong) {
        return super.getDetail(aLong);
    }

    @Override
    @PreAuthorize("denyAll()")
    public ResponseEntity addOne(Item postData, Map<String, Object> otherData) throws URISyntaxException {
        return super.addOne(postData, otherData);
    }

    @Override
    @PreAuthorize("denyAll()")
    public void deleteOne(Long aLong) {
        super.deleteOne(aLong);
    }

    @Override
    @PreAuthorize("denyAll()")
    public RowDefinition<Item> list(HttpServletRequest request) {
        return super.list(request);
    }

    @Override
    @PreAuthorize("denyAll()")
    protected List<Order> listOrder(CriteriaBuilder criteriaBuilder, Root<Item> root) {
        return super.listOrder(criteriaBuilder, root);
    }
}
