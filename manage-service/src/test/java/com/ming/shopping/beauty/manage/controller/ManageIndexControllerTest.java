package com.ming.shopping.beauty.manage.controller;

import com.ming.shopping.beauty.manage.ManageConfigTest;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author helloztt
 */
public class ManageIndexControllerTest extends ManageConfigTest {

    @Test
    public void testManageIndex() throws Exception {
        String result = mockMvc.perform(get(MANAGE_BASE_URL + "/"))
                .andDo(print())
                .andReturn().getResponse().getContentAsString();
        assertThat(result).isEqualTo("manage index");
    }

}