package com.ming.shopping.beauty.service.service;

import com.ming.shopping.beauty.service.entity.item.RechargeCard;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;

/**
 * @author CJ
 */
public interface StagingService {
    /**
     * 构造staging环境
     *
     * @throws IOException
     */
    @Transactional(rollbackFor = RuntimeException.class)
    void initStagingEnv() throws IOException;


    /**
     * 一个商户，一个门店，一个门店代表
     * 商户一共建立了5个项目；
     * 已审核，enabled, 到门店，门店enabled
     * 已审核，enabled, 到门店，门店未enabled
     * 已审核，没enabled, 到门店，门店enabled
     * 非已审核；，enabled, 到门店，门店enabled
     * 已审核，enabled, 未到门店
     *
     * @return 供staging使用的测试数据;一个商户，一个门店，一个门店代表，以及一堆项目
     * @throws IOException
     */
    Object[] generateStagingData() throws IOException;
}
