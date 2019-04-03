# mybatis介绍
MyBatis 是一款优秀的持久层框架，它支持定制化 SQL、存储过程以及高级映射。MyBatis 避免了几乎所有的 JDBC 代码和手动设置参数以及获取结果集。MyBatis 可以使用简单的 XML 或注解来配置和映射原生类型、接口和 Java 的 POJO（Plain Old Java Objects，普通老式 Java 对象）为数据库中的记录。
## mybatis架构图
![mybatis架构](https://img-blog.csdnimg.cn/2019040322420121.png? =400x280)
## mybatis插件模块
我们这个demo实现就是基于mybatis的插件模块（主要实现mybatis的Interceptor接口）
### Interceptor接口
```java
package org.apache.ibatis.plugin;

import java.util.Properties;

/**
 * @author Clinton Begin
 */
public interface Interceptor {

  Object intercept(Invocation invocation) throws Throwable;

  Object plugin(Object target);

  void setProperties(Properties properties);

}
   ```

# demo实现
主要技术 spring boot + mybatis

## pom.xml
```xml
<dependency>
      <groupId>org.mybatis.spring.boot</groupId>
      <artifactId>mybatis-spring-boot-starter</artifactId>
      <version>1.3.2</version>
</dependency>
<dependency>
      <groupId>mysql</groupId>
      <artifactId>mysql-connector-java</artifactId>
      <scope>runtime</scope>
 </dependency>
   ```

## 数据库 DDL 
```sql
create table user
(
  id       int         auto_increment primary key,
  username varchar(20) null
);
```

## 核心代码
```java
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
```

## 测试结果
![测试结果](https://img-blog.csdnimg.cn/2019040323152536.png? =800x400)
# github地址
https://github.com/XINYANJIANG/mybaits-plugs
