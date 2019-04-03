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
 * @description：mysql分页拦截器
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

        //获取参数
        Object param = invocation.getArgs()[1];
        BoundSql boundSql = mappedStatement.getBoundSql(param);
        Object parameterObject = boundSql.getParameterObject();

        /**
         * 判断是否是继承PageVo来判断是否需要进行分页
         */
        if (parameterObject instanceof PageVo) {
            //强转 为了拿到分页数据
            PageVo pagevo = (PageVo) param;
            String sql = boundSql.getSql();


            //获取相关配置
            Configuration config = mappedStatement.getConfiguration();
            Connection connection = config.getEnvironment().getDataSource().getConnection();

            //拼接查询当前条件的sql的总条数
            String countSql = "select count(*) from (" + sql + ") a";
            PreparedStatement preparedStatement = connection.prepareStatement(countSql);
            BoundSql countBoundSql = new BoundSql(config, countSql, boundSql.getParameterMappings(), boundSql.getParameterObject());
            ParameterHandler parameterHandler = new DefaultParameterHandler(mappedStatement, parameterObject, countBoundSql);
            parameterHandler.setParameters(preparedStatement);
            //执行获得总条数
            ResultSet rs = preparedStatement.executeQuery();
            int count = 0;
            if (rs.next()) {
                count = rs.getInt(1);
            }


            //拼接分页sql
            String pageSql = sql + " limit " + pagevo.getLimit() + " , " + pagevo.getOffset();
            //重新执行新的sql
            doNewSql(invocation, pageSql);

            Object result = invocation.proceed();
            connection.close();
            //处理新的结构
            PageResult<?> pageResult = new PageResult<List>(pagevo.page, pagevo.rows, count, (List) result);
            List<PageResult> returnResultList = new ArrayList<>();
            returnResultList.add(pageResult);

            return returnResultList;
        }
        return invocation.proceed();

    }

    private void doNewSql(Invocation invocation, String sql){
        final Object[] args = invocation.getArgs();
        MappedStatement statement = (MappedStatement) args[0];
        Object parameterObject = args[1];
        BoundSql boundSql = statement.getBoundSql(parameterObject);
        MappedStatement newStatement = newMappedStatement(statement, new BoundSqlSqlSource(boundSql));
        MetaObject msObject = MetaObject.forObject(newStatement, new DefaultObjectFactory(), new DefaultObjectWrapperFactory(), new DefaultReflectorFactory());
        msObject.setValue("sqlSource.boundSql.sql", sql);
        args[0] = newStatement;
    }


    /**
     * 获取新的MappedStatement
     * @param ms
     * @param newSqlSource
     * @return
     */
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

    /**
     * 新的SqlSource需要实现
     */
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

