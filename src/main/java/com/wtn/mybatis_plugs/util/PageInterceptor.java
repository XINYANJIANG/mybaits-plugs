package com.wtn.mybatis_plugs.util;

import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.executor.parameter.ParameterHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.reflection.DefaultReflectorFactory;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.factory.DefaultObjectFactory;
import org.apache.ibatis.reflection.wrapper.DefaultObjectWrapperFactory;
import org.apache.ibatis.scripting.defaults.DefaultParameterHandler;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * @author ：JiangXinYang
 * @date ：Created in 2019/4/3 9:18
 * @description：oracle分页拦截器
 */

@Intercepts({
        @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class})
})
@Component
public class PageInterceptor implements Interceptor {

    private static final Logger logger = LoggerFactory.getLogger(PageInterceptor.class);

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        logger.info("进入拦截器");
        Object[] args = invocation.getArgs();
        MappedStatement mappedStatement = (MappedStatement) args[0];
        Object para = invocation.getArgs()[1];
        BoundSql boundSql = mappedStatement.getBoundSql(para);
        Object parameterObject = boundSql.getParameterObject();
        if (parameterObject instanceof PageVo) {
            PageVo pagevo = (PageVo) para;
            String sql = boundSql.getSql();
            String countSql = "select count(*) from (" + sql + ") a";
//            Connection connection = (Connection) invocation.getArgs()[0];
            Configuration config = mappedStatement.getConfiguration();
            Connection connection = config.getEnvironment().getDataSource().getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(countSql);
            BoundSql countBoundSql = new BoundSql(config, countSql, boundSql.getParameterMappings(), boundSql.getParameterObject());
            ParameterHandler parameterHandler = new DefaultParameterHandler(mappedStatement, parameterObject, countBoundSql);
            parameterHandler.setParameters(preparedStatement);
            ResultSet rs = preparedStatement.executeQuery();
            int count = 0;
            if (rs.next()) {
                count = rs.getInt(1);
            }
            String pageSql = sql + " limit " + pagevo.getLimit() + " , " + pagevo.getOffset();
            resetSql2Invocation(invocation, pageSql);
            Object result = invocation.proceed();
            connection.close();
            PageResult<?> pageResult = new PageResult<List>(pagevo.page, pagevo.rows, count, (List) result);
            List<PageResult> returnResultList = new ArrayList<>();

            returnResultList.add(pageResult);

            return returnResultList;
        }
        return invocation.proceed();

    }

    private void resetSql2Invocation(Invocation invocation, String sql) throws SQLException {
        final Object[] args = invocation.getArgs();
        MappedStatement statement = (MappedStatement) args[0];
        Object parameterObject = args[1];
        BoundSql boundSql = statement.getBoundSql(parameterObject);
        MappedStatement newStatement = newMappedStatement(statement, new BoundSqlSqlSource(boundSql));
        MetaObject msObject = MetaObject.forObject(newStatement, new DefaultObjectFactory(), new DefaultObjectWrapperFactory(), new DefaultReflectorFactory());
        msObject.setValue("sqlSource.boundSql.sql", sql);
        args[0] = newStatement;
    }


    private MappedStatement newMappedStatement(MappedStatement ms, SqlSource newSqlSource) {
        MappedStatement.Builder builder =
                new MappedStatement.Builder(ms.getConfiguration(), ms.getId(), newSqlSource, ms.getSqlCommandType());
        builder.resource(ms.getResource());
        builder.fetchSize(ms.getFetchSize());
        builder.statementType(ms.getStatementType());
        builder.keyGenerator(ms.getKeyGenerator());
        if (ms.getKeyProperties() != null && ms.getKeyProperties().length != 0) {
            StringBuilder keyProperties = new StringBuilder();
            for (String keyProperty : ms.getKeyProperties()) {
                keyProperties.append(keyProperty).append(",");
            }
            keyProperties.delete(keyProperties.length() - 1, keyProperties.length());
            builder.keyProperty(keyProperties.toString());
        }
        builder.timeout(ms.getTimeout());
        builder.parameterMap(ms.getParameterMap());
        builder.resultMaps(ms.getResultMaps());
        builder.resultSetType(ms.getResultSetType());
        builder.cache(ms.getCache());
        builder.flushCacheRequired(ms.isFlushCacheRequired());
        builder.useCache(ms.isUseCache());

        return builder.build();
    }


    @Override
    public Object plugin(Object o) {
        Object wrap = Plugin.wrap(o, this);
        return wrap;
    }

    @Override
    public void setProperties(Properties properties) {

    }

    class BoundSqlSqlSource implements SqlSource {
        private BoundSql boundSql;

        public BoundSqlSqlSource(BoundSql boundSql) {
            this.boundSql = boundSql;
        }

        @Override
        public BoundSql getBoundSql(Object parameterObject) {
            return boundSql;
        }
    }
}

