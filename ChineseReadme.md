Eql中文手册
====

一个简单，轻量的数据持久层的框架 可以用于代替ibatis/mybatis
<br>
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.bingoohuang/eql/badge.svg?style=flat-square)](https://maven-badges.herokuapp.com/maven-central/com.github.bingoohuang/eql/)
[![License](http://img.shields.io/:license-apache-brightgreen.svg)](http://www.apache.org/licenses/LICENSE-2.0.html)
<br>

表示并不喜欢ibatis的XML配置文件...
+ 示例 1:

```xml
<select id="selectAB">
<![CDATA[
   SELELCT A,B
   FROM SOME_TABLE
   WHERE A > #a#
]]>
</select>
```
在ibatis中，哪怕是一个简单的SQL语句，我们都需要添加一个CDATA块将它包起来使用（我常常忘记如何写CDATA，每次还得去查XML CDATA的引用）
这玩意是真的麻烦。并且我认为select也很多余，因为这SQL语句一看就知道这是一个查询语句

+ 示例 2:

当两个或多个SQL在同一个XML文件上工作时，只要有一个无效的SQL，那你就等着报错，然后逐个检查文件中的SQL，再改正

```xml
<update id="xxx">
SELECT 1 FROM DUAL WHERE 1 > 0
</update>
```

这就是一颗老鼠屎坏了一锅汤（笑）...

+ 示例 3:

每次编写一个新的ibatis配置文件时，我总是会忘记一些细节（例如：如何创建SQLMap，如何编写规范有效的ibatis.xml）
基本每次都需要从别人那里拷一个ibatis.xml文件来对照

+ 示例 4:

对于动态SQL，我们时常要套一些标签进去，麻烦的一批，你又必须得这么写

```sql
<select id="xxx">
   SELECT 1 FROM DUAL
   WHERE 1 = 1
   <isEmpty property="name">
     AND name = #name#
   </isEmpty>
   <isNotEmpty property="name">
     AND id = #id#
   </isNotEmpty>
</select>
```

+ 示例 5:

老子就是不喜欢XML文件，它是真的麻烦，所以我搞了非常简单的Eql。

#一分钟教程
* 将 [eql-DEFAULT.properties](https://github.com/bingoohuang/eql/blob/master/src/test/resources/eql/eql-DEFAULT.properties) 复制到你的类路径EQL,并对数据库连接信息(如URL,密码和用户名)进行一些更改.
* 创建一个类（com/test/Demo.java）
* 再创建一个.eql文件（com/test/Demo.eql）
* 在类中写入如下代码：

```java
String str = new Eql().selectFirst("demo").execute();
```

* 在.eql文件中写入一个eql

```sql
-- [demo]
select 'X' from dual
```

* 正如你所看到的,非常简单

#只返回一行结果

```java
Map<String, String> row = new Eql().selectFirst("oneRow").execute();
```

```sql
-- [oneRow]
select 'X', 'Y' from dual
```

#返回一个Javabean

```java
XYBean xy = new Eql().selectFirst("javabean").execute();
```

```sql
-- [javabean returnType=XYBean]
select 'X', 'Y' from dual
```

#返回行数

```java
List<XYBean> xys = new Eql().select("rows").
             .returnType(XYBean.class)
             .execute();
```

```sql
-- [rows]
select 'X' X, 'Y' Y from dual
union
select 'A' X, 'B' Y from dual
```

#带参数的SQL （参数顺序）
* 示例 1

```java
String x = new Eql().selectFirst("autoSeq1")
          .params("x")
          .execute();
```

```sql
-- [autoSeq1]
select 'X' from dual
where 'x' = '##'
```

* 示例 2

```java
String x = new Eql().selectFirst("autoSeq2")
          .params("x", "y")
          .execute();
```

```sql
-- [autoSeq2]
select 'X' from dual
where 'x' = '##'
and 'y' = '##'
```

* 示例 3

```java
String x = new Eql().selectFirst("autoSeq3")
          .params("y", "x")
          .execute();
```

```sql
-- [autoSeq3]
select 'X' from dual
where 'x' = '#2#'
and 'y' = '#1#'
```

#带参数的SQL （参数名称）

* 示例 1

```java
String x = new Eql().selectFirst("bean")
       .params(new XyBean("x", "y"))
       .execute();
```

```sql
-- [bean]
select 'X' from dual
where 'x' = '#x#'
and 'y' = '#y#'
```

* 示例 2

```java
String x = new Eql().selectFirst("map")
        .params(mapOf("x", "a", "y", "b"))
        .execute();
```

```sql
-- [map]
select 'X' from dual
where 'a' = '#x#'
and 'b' = '#y#'
```

* 示例 3

```java
String x = new Eql().selectFirst("map")
        .params("a", "b")
        .execute();
```

```sql
-- [map]
select 'X' from dual
where 'a' = '#_1#'
and 'b' = '#_2#'
```

在示例3中，我们使用了_1和_2，我叫它们为内置参数。
更多的内置参数列表：
1. `_time` 当前时间，类型： `java.sql.Timestamp`
2. `_date` 当前日期，类型：`java.util.Date`
3. `_host` 当前主机名
4. `_ip` 当前IP
5. `_params` 当前params数组
6. `_paramsCount` 当前params数组长度
7. `_1`,`_2`,`_3`,... prams序列
8. `_dynamics` 当前动态数组
9. `_dynamicsCount` 当前动态数组长度
10. `_databaseId`, 对应的数据库：oracle/mysql/h2/db2/sqlserver

# 动态SQL

Eql的动态SQL主要基于 [OGNL](http://commons.apache.org/proper/commons-ognl/) 的表达式.

## if

```java

// 假如这条SQL是：
//   select 'X' from dual where 'a' = ?
// 这条SQL将带一个参数 'a'
String x = new Eql().selectFirst("ifDemo")
          .params(mapOf("x", "a"))
          .execute();

// 假如这条SQL是：
//   select 'X' from dual
String y = new Eql().selectFirst("ifDemo")
          .params(mapOf("x", "b"))
          .execute();
```

```sql
-- [ifDemo]
select 'X' from dual
-- if x == "a"
where 'a' = '#x#'
-- end

-- [ifDemo2]
select 'X' from dual
-- if x == "a"
where 'a' = '#x#'
-- else if x == "b"
where 'b' = '#x#'
-- else
where 'c' = '##'
-- end

-- 或者使用更加紧凑的语法

-- [ifDemo]
select 'X' from dual /* if x == "a" */  where 'a' = '#x#' /* end */
```

## iff

```sql
-- [ifDemo]
select 'X' from dual
-- iff x == "a"
where 'a' = '#x#'

-- or use more compact syntax

-- [ifDemo]
select 'X' from dual /* iff x == "a" */  where 'a' = '#x#'
```

使用 static 字段：

```java
public class OgnlStaticTest {
    public static String STATE = "102";

    @Test
    public void test() {
        String str = new Eql("mysql").id("ognlStatic").limit(1)
                .params(ImmutableMap.of("state", "102", "x", "y"))
                .execute();
        assertThat(str, is(nullValue()));

        str = new Eql("mysql").id("ognlStatic").limit(1)
                .params(ImmutableMap.of("state", "103", "x", "x"))
                .execute();
        assertThat(str, is("X"));
    }
}
```

```sql
-- [ognlStatic]
select 'X'
from DUAL
-- iff state == @org.n3r.eql.OgnlStaticTest@STATE
where 'x' = '#x#'
```

## switch

```sql
-- [switchSelect returnType=org.n3r.eql.SimpleTest$Bean]
SELECT A,B,C,D,E
FROM EQL_TEST
WHERE
-- switch a
--   case 1
  A = 1
--   case 2
  A = 2
-- end

-- 或者使用默认关键字

-- [switchSelectWithDefault returnType=org.n3r.eql.SimpleTest$Bean]
SELECT A,B,C,D,E
FROM eql_TEST
WHERE
-- switch a
--   case 1
   A = 1
--   case 2
   A = 2
--   default
   A = 3
-- end
```

## for

```java
Map<String, Object> map = Maps.newHashMap();
map.put("list", ImmutableList.of("a", "b", "x"));

String str = new Eql().selectFirst("for1").params(map).execute();
assertThat(str, is("x"));
```

```sql
-- [for1]
SELECT 'x'
FROM DUAL
WHERE 'x' in
-- for item=item index=index collection=list open=( separator=, close=)
#item#
-- end
```

## is(Not)Null/is(Not)Empty/is(Not)Blank

```sql
-- [isEmpty]
SELECT B
FROM eql_TEST
-- isEmpty a
WHERE A in (1,2)
-- else
WHERE A in (3,4)
-- end

-- [isNotEmpty]
SELECT B
FROM eql_TEST
-- isNotEmpty a
WHERE A = '#a#'
-- end
```

## in

```sql
SELECT NAME FROM EQL_IN WHERE ID IN (/* in _1 */)
```

```java
List<String> names = new Eql().params(Lists.newArrayList("1", "2")).execute();
```

## trim

```sql
-- [updateAuthor]
update author
-- trim prefix=SET suffixOverrides=,
  -- iff username != null
         username='#username#',
  -- iff password != null
         PASSWORD='#password#',
  -- iff email != null
         email='#email#',
  -- iff bio != null
          bio='#bio#',
-- end
where id='#id#'

-- [selectBlog]
SELECT STATE FROM BLOG
-- trim prefix=WHERE prefixOverrides=AND|OR
   -- iff state != null
          state = '#state#'
   -- iff title != null
      AND title like '#title#'
   -- iff author != null and author.name != null
      AND author_name like '#author.name#'
-- end
GROUP BY STATE
```

# 分页支持

```java
EqlPage page = new EqlPage(3, 2);
List<SimpleTest.Bean> beans = new Eql().id("testPage")
   .returnType(SimpleTest.Bean.class)
   .limit(page)
   .params("DC")
   .execute();
assertThat(page.getTotalRows(), is(10));
```

```sql
-- [testPage]
SELECT A,B,C,D,E
FROM eql_TEST
WHERE C = '##'
```

# 动态的表名

```java
String str = new Eql().selectFirst("replace1")
   .params("x").dynamics("DUAL").execute();
assertThat(str, is("x"));

str = new Eql().selectFirst("replace2").params("x")
   .dynamics(ImmutableMap.of("table", "DUAL")).execute();
assertThat(str, is("x"));
```

```sql
-- [replace1]
SELECT 'x'
FROM $$
WHERE 'x' = '##'


-- [replace2]
SELECT 'x'
FROM $table$
WHERE 'x' = '##'
```

# 批处理

```java
Eql eql = new Eql();
eql.startBatch(/*batchSize*/10);
for (int i = 0; i < 10; ++i) {
    String orderNo = randLetters(10);
    String userId = randLetters(10);
    int prizeItem = randInt(10);
    int ret = eql.insert("insertPrizeBingoo")
           .params(orderNo, "Olympic", "" + prizeItem, userId)
           .execute();

    assertEquals(0, ret);
}

eql.executeBatch();
```

```sql
-- [insertPrizeBingoo]
INSERT INTO EQL_TEST_BINGOO(ORDER_NO, ACTIVITY_ID, ITEM_ID, USER_ID, BINGOO_TIME)
VALUES(##, ##, ##, ##, SYSDATE)
```

# 选项支持

```sql
-- [likeDemo]
select 'x' from demo where name like '#:Like#'

-- [leftLikeDemo]
select 'x' from demo where name like '#:LeftLike#'

-- [rightLikeDemo]
select 'x' from demo where name like '#:RightLike#'
```

```java
new Eql().id("likeDemo").params("b").execute();
// 16:12:51.316 [main] DEBUG org.n3r.eql.Eql - prepare sql likeDemo: select 'x' from demo where name like ?
// 16:12:51.317 [main] DEBUG org.n3r.eql.map.EqlRun - param: [%b%]

new Eql().id("leftLikeDemo").params("c").execute();
// 16:12:51.326 [main] DEBUG org.n3r.eql.Eql - prepare sql leftLikeDemo: select 'x' from demo where name like ?
// 16:12:51.326 [main] DEBUG org.n3r.eql.map.EqlRun - param: [%c]

new Eql().id("rightLikeDemo").params("a").execute();
// 16:12:51.331 [main] DEBUG org.n3r.eql.Eql - prepare sql rightLikeDemo: select 'x' from demo where name like ?
// 16:12:51.331 [main] DEBUG org.n3r.eql.map.EqlRun - param: [a%]
```

# Oracle Blob 支持

```java
public void testOracleBlob() {
    new Eqll().id("insertBlob").params("中华人民共和国").execute();
    byte[] bytes = new Eqll().id("selectBlob").limit(1).execute();
    assertThat(new String(bytes, Charsets.UTF_8), is("中华人民共和国"));

    String ret = new Eqll().id("selectBlobString").limit(1).execute();
    assertThat(ret, is("中华人民共和国"));

    AsResult asResult = new Eqll().id("selectBlobAsResult").limit(1).execute();
    assertThat(asResult.getSeq(), is(1));
    assertThat(asResult.getRemark(), is("中华人民共和国"));

    Integer effectedRows = new Eqll().id("updateBlob").params("台湾省").execute();
    assertThat(effectedRows, is(1));
    ret = new Eqll().id("selectBlobString").limit(1).execute();
    assertThat(ret, is("台湾省"));
}

public static class AsResult {
    private String state;
    private String remark;
    private int seq;

    // setters ang getters
}
```

```sql
-- [insertBlob onerr=resume]
DROP TABLE eql_BLOB;
CREATE TABLE eql_BLOB (BOB BLOB);
INSERT INTO eql_BLOB(BOB) VALUES(#:LOB#)

-- [selectBlob]
SELECT BOB FROM eql_BLOB

-- [selectBlobString returnType=string]
SELECT BOB FROM eql_BLOB


-- [selectBlobAsResult returnType=org.n3r.eql.JavaBlobTest$AsResult]
SELECT 1 as seq, BOB as remark FROM eql_BLOB

-- [updateBlob]
UPDATE eql_BLOB SET BOB = '#:LOB#'
```

# [Diamond-miner](https://github.com/bingoohuang/diamond-miner) 支持示例
Eql 也可以从 Diamond 进行加载
首先在类路径上创建 eql/eql-diamond.properties 如：

```
sql.resource.loader=org.n3r.eql.diamond.DiamondEqlResourceLoader

transactionType=jdbc
driver=oracle.jdbc.driver.OracleDriver
url=jdbc:oracle:thin:@127.0.0.1:1521:orcl
user=orcl
password=orcl
```

正如你看到的，我们将SQL资源加载器重新定义为diamond-specific。
然后在 diamond 中添加一个配置:

```
group=EQL
dataId=org.n3r.eql.DiamondTest.eql
content=

-- [diamondDemo]
SELECT 'Hello' FROM DUAL

```

然后在Java代码中

```java
String str = new Eql("diamond").selectFirst("diamondDemo").execute();
System.out.println(str);
```

# 使用JDBC语句进行多次 select/update

```java
Eql eql = new Eql().id("selectStmt");
ESelectStmt selectStmt = eql.selectStmt();

selectStmt.executeQuery(3);
String str = selectStmt.next();
assertThat(str, is("CC"));
assertThat(selectStmt.next(), is(nullValue()));

selectStmt.executeQuery(4);
str = selectStmt.next();
assertThat(str, is("DC"));
assertThat(selectStmt.next(), is(nullValue()));

selectStmt.close();
eql.close();
```

```java
Eql eql = new Eql().id("updateStmt");
EUpdateStmt updateStmt = eql.updateStmt();

int rows = updateStmt.update(3, "Bingoo");
assertThat(rows, is(1));

rows = updateStmt.update(4, "Dingoo");
assertThat(rows, is(1));

updateStmt.close();
eql.close();
```

```sql
-- [selectStmt]
SELECT C
FROM eql_TEST
WHERE A = '##'

-- [updateStmt]
UPDATE eql_TEST
SET C = '#2#'
WHERE A = '#1#'
```

# 自定义结果映射器 示例：

```java
import org.n3r.eql.map.EqlRowMapper;

public static class MyMapper implements EqlRowMapper {
    private String name;

    @Override
    public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
        name = rs.getString(1);
        return null;
    }

    public String getName() {
        return name;
    }
}

@Test
public void test() {
    MyMapper myMapper = new MyMapper();
    new Eql().returnType(myMapper).execute("SELECT 'X' FROM DUAL");
    assertThat(myMapper.getName(), is("X"));
}
```

# 自定义配置支持

```java
Eqll.choose(new EqlJdbcConfig("oracle.jdbc.driver.OracleDriver",
        "jdbc:oracle:thin:@127.0.0.1:1521:orcl", "orcl", "orcl"));

Timestamp ts = new Eqll().limit(1).execute("SELECT SYSDATE FROM DUAL");

// or
Eqll.choose(new EqlPropertiesConfig(
        EqlConfigKeys.DRIVER + "=oracle.jdbc.driver.OracleDriver\n" +
        EqlConfigKeys.URL + "=jdbc:oracle:thin:@127.0.0.1:1521:orcl\n" +
        EqlConfigKeys.USER + "=orcl\n" +
        EqlConfigKeys.PASSWORD + "=orcl\n"));
```

支持的配置如下所示:

## **connection.impl**
+ 含义：用于实现 `org.n3r.eql.trans.EqlConnection` 接口。
+ 默认值：当设置 jndiName 时， 应使用 `org.n3r.eql.trans.EqlJndiConnection`，否则 `org.n3r.eql.trans.EqlSimpleConnection`。
+ 比如：`org.n3r.eql.trans.EqlC3p0Connection` 或者你自定义实现。

## **jndiName**
+ 含义：指定的 JNDI 名称，用于使用 JNDI 的数据源。
+ 默认值：N/A。
+ 比如：N/A。

## **java.naming.factory.initial**
+ 含义：与 **jndiName** 一起使用。
+ 默认值：无。
+ 比如：`weblogic.jndi.WLInitialContextFactory`

## **java.naming.provider.url**
+ 含义：与 **jndiName** 一起使用。
+ 默认值：无。
+ 比如: `t3://127.0.0.1:7001/`

## **transactionType**
+ 含义：与 **jndiName** 一起使用。
+ 默认值：JDBC。
+ 比如：JTA。

## **driver**
+ 含义：JDBC驱动程序名称。
+ 默认值：无。
+ 比如：`oracle.jdbc.driver.OracleDriver`， `com.mysql.jdbc.Driver`，等...

## **url**
+ 含义：JDBC的url
+ 默认值：无。
+ 比如：`jdbc:oracle:thin:@127.0.0.1:1521:orcl`，`jdbc:mysql://localhost:3306/diamond?useUnicode=true&&characterEncoding=UTF-8&connectTimeout=1000&autoReconnect=true`

## **user**
+ 含义：JDBC的用户名。
+ 默认值：无。
+ 比如：orcl。

## **password**
+ 含义：JDBC的密码。
+ 默认值：无。
+ 比如：orcl。

## **expression.evaluator**
+ 含义：类全名 `org.n3r.eql.base.ExpressionEvaluator`。
+ 默认值：`org.n3r.eql.impl.OgnlEvaluator`。
+ 比如：看情况。

## **sql.resource.loader**
+ 含义：EQL资源加载器，FQCN实现 `org.n3r.eql.base.EqlResourceLoader`。
+ 默认值：`org.n3r.eql.impl.FileEqlResourceLoader` 它用于读取EQL
+ 比如：`org.n3r.eql.diamond.DiamondEqlResourceLoader` 或者自定义实现。

## **dynamic.language.driver**
+ 含义：EQL的动态支持语言驱动，FQCN实现 `org.n3r.eql.base.DynamicLanguageDriver`。
+ 默认值：`org.n3r.eql.impl.DefaultDynamicLanguageDriver` 使用SQL特殊注释来实现动态SQL。
+ 比如：`org.n3r.eql.impl.FreemarkerDynamicLanguageDriver` 或自定义实现。

## **sql.parse.lazy**
+ 含义：执行时解析动态EQL。
+ 默认值：false。
+ 比如：true or yes.

# 集成 [diamond-client](https://github.com/bingoohuang/diamond-miner)
## 从 diamond 读取连接配置
* 添加 diamond ：

```
group=EqlConfig,dataId=DEFAULT,content=
driver=oracle.jdbc.driver.OracleDriver
url=jdbc:oracle:thin:@127.0.0.1:1521:orcl
user=orcl
password=orcl
```

* 使用Dql替代Eql来处理 diamond 连接配置：

```java
// read diamand content of group=EqlConfig, dataId=DEFAULT as connection config
new Dql().id("xxx").execute();

// read diamond content of group=EqlConfig, dataId=DSMALL as connection config
new Dql("DSMALL").id("yyy").execute();
```

## 从 diamond 中读取Eql
在连接配置中， 设置 `sql.resource.loader` 为 `org.n3r.eql.diamond.DiamondEqlResourceLoader`。

```java
package org.n3r.eql;

public class DiamondTest {
    @Test
    public void test1() throws InterruptedException {
        // Will read diamond content of group=EQL,dataId=org.n3r.eql.DiamondTest.eql
        // The diamond content can have the same structure with normal eql file.
        String str = new Eql("diamond").selectFirst("diamondDemo").execute();
        System.out.println(str);
    }
}
```

## 缓存SQL查询结果
SQL查询结果缓存可以通过SQL选项中的cache关键字启用：

```sql
-- [test1 cache]
SELECT TO_CHAR(SYSTIMESTAMP, 'HH24:MI:SS.FF6') FROM DUAL
```

默认的缓存模型是基于guava缓存，这些缓存将在一天后过期。

如果要替代缓存模型，可以这么写入：
```sql
-- global settings cacheModel.impl.myCache=@org.n3r.eql.cache.GuavaCacheProvider("expireAfterWrite=3s,maximumSize=1000")

-- [test1 cache cacheModel=myCache]
SELECT TO_CHAR(SYSTIMESTAMP, 'HH24:MI:SS.FF6') FROM DUAL
```

该类 `org.n3r.eql.cache.GuavaCacheProvider` 由Eql提供，其缓存构建器的规范与guava的[缓存规范](http://docs.guava-libraries.googlecode.com/git/javadoc/com/google/common/cache/CacheBuilderSpec.html)相同。
自定义缓存所提供程序类应实现 `org.n3r.eql.cache.EqlCacheProvider` 并可选的实现 `org.n3r.eql.spec.ParamsAppliable` 在有参数设置时。


# 支持简单的基于POJO的注解配置

**@EqlTable**

指定与该类相关的表名。
如果该类没有被 @EqlTable 注解，那么默认的表名将会自动进行如下转换。
Person to person（转为小写）， PersonInfo to person_info（第二个单词前加_）。

**@EqlId**

指定该字段是否是表中的主键。
如果属性名称为 **id**， 那么它也会被视为隐性的 @EqlId.

**@EqlColumn**

指定与普通字段名称。
当使用 @EqlColumn 却未注解时，默认名称将从propertyName转换成加_（下划线）和全小写。
比如：personName to person_name。

**@EqlSkip**

跳过映射表的字段。

**CRUD**

更新和删除API并将使用id作为其条件。
读取API将所有的非空字段作为其组合条件。

```java
@EqlTable(name = "personx")
public static class Person2 {
    @EqlId
    @EqlColumn(name = "id")
    private String pid;
    @EqlColumn(name = "name")
    private String pname;
    private Integer age;

    @EqlSkip
    private String remark;

    // getters and setters
}

@Test
public void testAnnotation() {
    Person2 person = new Person2();
    person.setPid("1002");
    person.setPname("bingoo");
    person.setAge(30);

    // delete from person where id = ?
    new Pql("mysql").delete(person);

    // insert into person（id,name,age) values(?,?,?)
    new Pql("mysql").create(person);

    person.setPname("huang");
    person.setAge(null);
    // update person set age = ? where id = ?
    int effectedRows = new Pql("mysql").update(person);
    assertThat(effectedRows, is(1));

    Person2 queryPerson = new Person2();
    queryPerson.setPid("1002");

    // select id,name,age from person where id = ?
    List<Person2> resultPerson = new Pql("mysql").read(queryPerson);
    assertThat(resultPerson.size(), is(1));

    effectedRows = new Pql("mysql").delete(queryPerson);
    assertThat(effectedRows, is(1));
}
```

# Eqler
为了简化Eql API的使用，这里介绍了Eqler。
Eqler是一个接口，其中使用这些方法来执行SQL和用于获取进程的结果
Eqler实例由EqlerFactory创建
以下是示例：

``` java
package org.n3r.eql.eqler.crud;

import org.n3r.eql.eqler.annotations.EqlerConfig;
import org.n3r.eql.eqler.annotations.Sql;
import org.n3r.eql.eqler.annotations.SqlId;

import java.util.List;
import java.util.Map;

@EqlerConfig("mysql")
public interface StudentEqler {
    void prepareData();

    int addStudent(int studentId, String name, int age);

    @Sql("insert into eql_student values(#studentId#, #name#, #age#)")
    int addStudent(Student student);

    @Sql("insert into eql_student values(#a#, #b#, #c#)")
    int addStudentAnotherWay(@NamedParam("a") int studentId, @NamedParam("b") String name, @NamedParam("c") int age);

    @Sql("select * from eql_student")
    List<Student> queryAllStudents();

    String queryStudentName(int studentId);

    @SqlId("queryStudent")
    Map<String, Object> queryStudentMap(int studentId);

    Student queryStudent(int studentId);
}
```

```sql
--  org/n3r/eql/eqler/crud/StudentEqler.eql

-- [prepareData]
drop table if exists eql_student;
create table eql_student(student_id int, name varchar(10), age int);

-- [addStudent]
insert into eql_student
values('##', '##', '##')

-- [queryStudentName]
select name from eql_student where student_id = '##'

-- [queryStudent]
select student_id, name, age from eql_student where student_id = '##'
```

```java
@Test
public void test() {
    StudentEqler eqler = EqlerFactory.getEqler(StudentEqler.class);
    eqler.prepareData();

    eqler.addStudent(1, "bingoo", 123);
    eqler.addStudent(new Student(2, "huang", 124));
    eqler.addStudentAnotherWay(3, "dingoo", 125);

    List<Student> students = eqler.queryAllStudents();
    assertThat(students.toString(), is(equalTo(
            "[Student{studentId=1, name='bingoo', age=123}, " +
                    "Student{studentId=2, name='huang', age=124}, " +
                    "Student{studentId=3, name='dingoo', age=125}]"
    )));

    Student student1 = eqler.queryStudent(1);
    assertThat(student1.toString(), is(equalTo("Student{studentId=1, name='bingoo', age=123}")));

    Map<String, Object> student2 = eqler.queryStudentMap(2);
    assertThat(student2.toString(), is(equalTo("{age=124, name=huang, student_id=2}")));

    String studentName = eqler.queryStudentName(1);
    assertThat(studentName, is(equalTo("bingoo")));
}
```

# TODO
+ 内联注释，比如：`/* iff ... */` 是正则表达式的解析，并且该方法不会 `/* ... */` 在字符串中忽略，比如：`'literal string /* if xxx */'`。

# 常见问题解答
## MySQLNonTransientConnectionException
```
com.mysql.jdbc.exceptions.jdbc4.MySQLNonTransientConnectionException: Communications link failure during rollback(). Transaction resolution unknown.
	at sun.reflect.NativeConstructorAccessorImpl.newInstance0(Native Method)
	at sun.reflect.NativeConstructorAccessorImpl.newInstance(NativeConstructorAccessorImpl.java:62)
	at sun.reflect.DelegatingConstructorAccessorImpl.newInstance(DelegatingConstructorAccessorImpl.java:45)
	at java.lang.reflect.Constructor.newInstance(Constructor.java:423)
	at com.mysql.jdbc.Util.handleNewInstance(Util.java:377)
	at com.mysql.jdbc.Util.getInstance(Util.java:360)
	at com.mysql.jdbc.SQLError.createSQLException(SQLError.java:956)
	at com.mysql.jdbc.SQLError.createSQLException(SQLError.java:935)
	at com.mysql.jdbc.SQLError.createSQLException(SQLError.java:924)
	at com.mysql.jdbc.SQLError.createSQLException(SQLError.java:870)
	at com.mysql.jdbc.ConnectionImpl.rollback(ConnectionImpl.java:4606)
	at org.n3r.eql.trans.SimpleDataSource.popConnection(SimpleDataSource.java:638)
	at org.n3r.eql.trans.SimpleDataSource.getConnection(SimpleDataSource.java:207)
	at org.n3r.eql.trans.EqlSimpleConnection.getConnection(EqlSimpleConnection.java:17)
	at org.n3r.eql.trans.EqlJdbcTran.getConn(EqlJdbcTran.java:58)
	at org.n3r.eql.Eql.createConn(Eql.java:105)
	at org.n3r.eql.Eql.execute(Eql.java:157)
```

你可以试着使用，`jdbc:mysql://192.168.99.100:13306/dba?useUnicode=true&&characterEncoding=UTF-8&connectTimeout=3000&socketTimeout=3000&autoReconnect=true` 而不是 `jdbc:mysql://192.168.99.100:13306/dba`

# 常见问题解答
## java.lang.NullPointerException
像int/long/short这样的一个原始的返回类型会导致NPE没有任何行。
在这种情况下，应该使用像Integer/Long/Short这样的相关包装类型。

```java
@Test
public void returnInteger() {
    Integer intValue = new Eql("h2").limit(1)
            .returnType(Integer.class).execute("select 1 where 2 > 3");
    assertThat(intValue).isNull();
}

@Test(expected = NullPointerException.class)
public void returnInt() {
    int intValue = new Eql("h2").limit(1)
            .returnType(int.class).execute("select 1 where 2 > 3");
}
```

# 搬运工人
## mysql
运行mysql：<br/>
`docker run -p 13306:3306 --name mysql -e MYSQL_ROOT_PASSWORD=my-secret-pw -d mysql`
<br/>运行客户端：<br/>
`docker run -it --rm mysql mysql -h192.168.99.100 -uroot -P13306 -pmy-secret-pw`

# OGNL 相关知识
eql默认使用OGNL表达式来做动态条件SQL的判断，OGNL表达式可以参见[ognl language guide](https://commons.apache.org/proper/commons-ognl/language-guide.html).
## 注意项
`'a'` 表示字符a，要表示字符串a，需要使用双引号`"a"`;
`'ab'`和`"ab""` 都可以表示字符串ab。

## OGNL有以下几种常数：

1. String类型，如：在Java中由单引号或双引号分隔，全部字符转义。
2. Character类型，如：在Java中由单引号分隔，还有全套转义。
3. Numeric类型，除了Java有的类型 如：int,longs,float,double之外，OGNL还可以使用"b"或"B"后缀指定BigDecimals，而BigIntegers具有"h"或"H"后缀（认为“巨大” - 我们选择了“Big”，因为它不干扰十六进制数字）;
布尔（true和false）文字;
4. Boolean类型 (true 和 false) 。
5. null

如果要在动态SQL中将变量与字符串进行比较，请注意单引号或双引号。

##测试代码：
```java
@SneakyThrows
public static void main(String[] args) {
    val map = ImmutableMap.of(
            "a", "1",
            "b", "11",
            "c", 0,
            "d", "0");
    out.println(Ognl.getValue("a == '1'", map)); // false
    out.println(Ognl.getValue("a == 1", map)); // true
    out.println(Ognl.getValue("a == \"1\"", map)); // true

    out.println(Ognl.getValue("b == '11'", map)); // true
    out.println(Ognl.getValue("b == 11", map)); // true

    out.println(Ognl.getValue("c == \"0\"", map)); // true
    out.println(Ognl.getValue("c == '0'", map)); // false
    out.println(Ognl.getValue("c == 0", map)); // true

    out.println(Ognl.getValue("d == \"0\"", map)); // true
    out.println(Ognl.getValue("d == '0'", map)); // false
    out.println(Ognl.getValue("d == 0", map)); // true
}
```

