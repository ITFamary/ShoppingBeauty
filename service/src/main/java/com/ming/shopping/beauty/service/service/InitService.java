package com.ming.shopping.beauty.service.service;

import me.jiangcai.lib.jdbc.ConnectionProvider;
import me.jiangcai.lib.jdbc.JdbcService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StreamUtils;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.charset.Charset;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * @author helloztt
 */
@Service
public class InitService {
    private static final Log log = LogFactory.getLog(InitService.class);

    @Autowired
    private JdbcService jdbcService;
    @Autowired
    private ApplicationContext applicationContext;

    @PostConstruct
    @Transactional(rollbackFor = RuntimeException.class)
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public void init() throws IOException, SQLException {
//        database();
    }

    private void database() throws SQLException {
        // TODO: 2018/1/15  
        jdbcService.runJdbcWork(connection -> {
            if (connection.profile().isH2()) {
                executeSQLCode(connection, "LoginAgentLevel.h2.sql");
            } else if (connection.profile().isMySQL()) {
                try (Statement statement = connection.getConnection().createStatement()) {
                    statement.executeUpdate("DROP FUNCTION IF EXISTS `LoginAgentLevel`");
                }
                executeSQLCode(connection, "LoginAgentLevel.mysql.sql");
            }
            //
            String fileName;
            if (connection.profile().isMySQL()) {
                fileName = "mysql";
            } else if (connection.profile().isH2()) {
                fileName = "h2";
            } else{
                throw new IllegalStateException("not support for:" + connection.getConnection());
            }
            try {
                try (Statement statement = connection.getConnection().createStatement()) {
                    statement.executeUpdate("DROP TABLE IF EXISTS `LoginCommissionJournal`");
                    statement.executeUpdate(StreamUtils.copyToString(new ClassPathResource(
                                    "/LoginCommissionJournal." + fileName + ".sql").getInputStream()
                            , Charset.forName("UTF-8")));
                    statement.executeUpdate("DROP TABLE IF EXISTS `AgentGoodAdvancePaymentJournal`");
                    statement.executeUpdate(StreamUtils.copyToString(new ClassPathResource(
                                    "/AgentGoodAdvancePaymentJournal." + fileName + ".sql").getInputStream()
                            , Charset.forName("UTF-8")));
                }
            } catch (IOException e) {
                throw new IllegalStateException("读取SQL失败", e);
            }
            //

        });
    }

    private void executeSQLCode(ConnectionProvider connection, String resourceName) throws SQLException {
        try {
            String code = StreamUtils.copyToString(applicationContext.getResource("classpath:/" + resourceName).getInputStream(), Charset.forName("UTF-8"));
            try (Statement statement = connection.getConnection().createStatement()) {
                statement.executeUpdate(code);
            }
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
