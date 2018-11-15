eql
====
[![Build Status](https://travis-ci.org/bingoohuang/eql.svg?branch=master)](https://travis-ci.org/bingoohuang/eql)
[![Coverage Status](https://coveralls.io/repos/github/bingoohuang/eql/badge.svg?branch=master)](https://coveralls.io/github/bingoohuang/eql?branch=master)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.bingoohuang/eql/badge.svg?style=flat-square)](https://maven-badges.herokuapp.com/maven-central/com.github.bingoohuang/eql/)
[![License](http://img.shields.io/:license-apache-brightgreen.svg)](http://www.apache.org/licenses/LICENSE-2.0.html)

An easy framework of java JDBC to be an alternative to ibatis/mybatis.[中文版](ChineseReadme.md)

I don't like XML in ibatis.
+ excuse 1:

```xml
<select id="selectAB">
<![CDATA[
   SELELCT A,B
   FROM SOME_TABLE
   WHERE A > #a#
]]>
</select>
```
Wooh, for a simple sql of only three lines, we need add a CDATA block to use **>** (I always forget how to write CDATA, and every time I have to lookup XML CDATA reference). It's so bad. And also the **select** is redundant because the SQL itself is telling us it is a SELECT SQL and not some others.

+ excuse 2:

When two or more members are working on the same XML file, only one bad SQL will corrupt all the SQLs in the same file:

```xml
<update id="xxx">
SELECT 1 FROM DUAL WHERE 1 > 0
</update>
```

Ooh, that is "one rotten apple could ruin a whole barrel of apples."

+ excuse 3:

Every time, when I code with ibatis, I forget how to begin (eg. how to create Sqlmap and how to write valid ibatis xml) and have to copy some initial code from others.

+ excuse 4:

There is no else in its dynamic. I have to write like:

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

+ excuse 5:

I don't like XML. I like free text and freedom. And so I created EQL which is really very easy.

# One minute tutorial
* copy [eql-DEFAULT.properties](https://github.com/bingoohuang/eql/blob/master/src/test/resources/eql/eql-DEFAULT.properties) to your classpath eql and do some changes for your database connection info such as url, password and username.
* create com/xxx/Demo.java.
* create com/xxx/Demo.eql(***keep the same package and base filename***) in classpath.
* write following code in com.xxx.Demo main method:

```java
String str = new Eql().selectFirst("demo").execute();
```

* write an eql in Demo.eql

```sql
-- [demo]
select 'X' from dual
```

* that's all. and you see it is very simple.

# Return one row

```java
Map<String, String> row = new Eql().selectFirst("oneRow").execute();
```

```sql
-- [oneRow]
select 'X', 'Y' from dual
```

# Return a Javabean

```java
XYBean xy = new Eql().selectFirst("javabean").execute();
```

```sql
-- [javabean returnType=XYBean]
select 'X', 'Y' from dual
```

# Return rows

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

# With parameters in sequence

* example 1

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

* example 2

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

* example 3

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

# With parameters by properties name

* example 1

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

* example 2

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

* example 3

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

In above example, the _1 and _2 are used, they are called built-in parameters.
More built-in parameters list here:
1. `_time` current timestamp, type: `java.sql.Timestamp`
2. `_date` current date, type:`java.util.Date`
3. `_host` current hostname
4. `_ip` current ip
5. `_params` currrent params array
6. `_paramsCount` length of current params array
7. `_1`,`_2`,`_3`,... the param in sequence
8. `_dynamics` current dynamics array
9. `_dynamicsCount` lenght of current dynamics array
10. `_databaseId`, oracle/mysql/h2/db2/sqlserver

# Dynamic sql

Eql's dynamic sql is now base on [OGNL](http://commons.apache.org/proper/commons-ognl/) expression.

## if

```java

// real sql will be:
//   select 'X' from dual where 'a' = ?
// and with one bound param "a"
String x = new Eql().selectFirst("ifDemo")
          .params(mapOf("x", "a"))
          .execute();

// real sql will be:
//   select 'X' from dual
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

-- or use more compact syntax

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

to use STATIC fields:

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

-- or with default keyword

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

## in statement in sql

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

# Pagination support

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

# Dynamic table name

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

# Batch execute

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

# Like option support

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

# Oracle Blob support

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

# [Diamond-miner](https://github.com/bingoohuang/diamond-miner) support example
Eql also can be loaded from diamond.
First create eql/eql-diamond.properties on classpath like:

```
sql.resource.loader=org.n3r.eql.diamond.DiamondEqlResourceLoader

transactionType=jdbc
driver=oracle.jdbc.driver.OracleDriver
url=jdbc:oracle:thin:@127.0.0.1:1521:orcl
user=orcl
password=orcl
```

As you see, we redefined sql resource loader to diamond-specific.
And then add a config in diamond:

```
group=EQL
dataId=org.n3r.eql.DiamondTest.eql
content=

-- [diamondDemo]
SELECT 'Hello' FROM DUAL

```

And then in java code

```java
String str = new Eql("diamond").selectFirst("diamondDemo").execute();
System.out.println(str);
```

# Reuse jdbc statements to select/update repeatedly.

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

# Custom result mapper example

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

# Custom config support

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

Supported configs are listed below:

## **connection.impl**
+ Meaning: Full qualified class name(FQCN) that implemented `org.n3r.eql.trans.EqlConnection` interface.
+ Default: When jndiName is set, use `org.n3r.eql.trans.EqlJndiConnection`, otherwise `org.n3r.eql.trans.EqlSimpleConnection`.
+ Samples: `org.n3r.eql.trans.EqlC3p0Connection` or your custom implementation.

## **jndiName**
+ Meaning: Specified JNDI name to use JNDI data source.
+ Default: N/A.
+ Samples: N/A.

## **java.naming.factory.initial**
+ Meaning: Used together with **jndiName**.
+ Default: None.
+ Samples: `weblogic.jndi.WLInitialContextFactory`

## **java.naming.provider.url**
+ Meaning: Used together with **jndiName**.
+ Default: None.
+ Samples: `t3://127.0.0.1:7001/`

## **transactionType**
+ Meaning: Used together with **jndiName**.
+ Default: JDBC.
+ Samples: JTA.

## **driver**
+ Meaning: JDBC driver name.
+ Default: None.
+ Samples: `oracle.jdbc.driver.OracleDriver`， `com.mysql.jdbc.Driver`, and ...

## **url**
+ Meaning: JDBC url.
+ Default: None
+ Samples: `jdbc:oracle:thin:@127.0.0.1:1521:orcl`, `jdbc:mysql://localhost:3306/diamond?useUnicode=true&&characterEncoding=UTF-8&connectTimeout=1000&autoReconnect=true`

## **user**
+ Meaning: JDBC username.
+ Default: None.
+ Samples: orcl.

## **password**
+ Meaning: JDBC password.
+ Default: None.
+ Samples: orcl.

## **expression.evaluator**
+ Meaning: Full quantified class name which implements `org.n3r.eql.base.ExpressionEvaluator`.
+ Default: `org.n3r.eql.impl.OgnlEvaluator`.
+ Samples: customed implementation.

## **sql.resource.loader**
+ Meaning: EQL resource loader. FQCN which implements `org.n3r.eql.base.EqlResourceLoader`.
+ Default: `org.n3r.eql.impl.FileEqlResourceLoader` which read eql file of the same package and same base name with Eql's used java class.
+ Samples: `org.n3r.eql.diamond.DiamondEqlResourceLoader` or custom implementation.

## **dynamic.language.driver**
+ Meaning: EQL dynamic support language driver. FQCN which implements `org.n3r.eql.base.DynamicLanguageDriver`.
+ Default: `org.n3r.eql.impl.DefaultDynamicLanguageDriver` which use SQL special comment to achieve dynamic SQL.
+ Samples: `org.n3r.eql.impl.FreemarkerDynamicLanguageDriver` or custom implementation.

## **sql.parse.lazy**
+ Meaning: Parse dynamic EQL while execution or not.
+ Default: false.
+ Samples: true or yes.

# Integrated with [diamond-client](https://github.com/bingoohuang/diamond-miner)
## Read connection configuration from diamond
* Add a diamond stone:

```
group=EqlConfig,dataId=DEFAULT,content=
driver=oracle.jdbc.driver.OracleDriver
url=jdbc:oracle:thin:@127.0.0.1:1521:orcl
user=orcl
password=orcl
```

* Use Dql to replace Eql to work with diamond connection configuration.

```java
// read diamand content of group=EqlConfig, dataId=DEFAULT as connection config
new Dql().id("xxx").execute();

// read diamond content of group=EqlConfig, dataId=DSMALL as connection config
new Dql("DSMALL").id("yyy").execute();
```

## Read eql from diamond
In the connection config, set `sql.resource.loader` to `org.n3r.eql.diamond.DiamondEqlResourceLoader`.

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

## Caching sql query result
Sql query result caching can be enabled with cache keyword in sql options:

```sql
-- [test1 cache]
SELECT TO_CHAR(SYSTIMESTAMP, 'HH24:MI:SS.FF6') FROM DUAL
```

The default cache model is based on guava cache with 1 day expired after write.

If you want to use an alternative cache model, you can use write like following:
```sql
-- global settings cacheModel.impl.myCache=@org.n3r.eql.cache.GuavaCacheProvider("expireAfterWrite=3s,maximumSize=1000")

-- [test1 cache cacheModel=myCache]
SELECT TO_CHAR(SYSTIMESTAMP, 'HH24:MI:SS.FF6') FROM DUAL
```

The class `org.n3r.eql.cache.GuavaCacheProvider` is provided by eql and its cache builder spec is same to guava [cache spec](http://docs.guava-libraries.googlecode.com/git/javadoc/com/google/common/cache/CacheBuilderSpec.html).
Custom cache provider class should implement `org.n3r.eql.cache.EqlCacheProvider` and optionally to implement `org.n3r.eql.spec.ParamsAppliable` when there are parameters to set.


# simple POJO-based CRUD supported

**@EqlTable**

To specify the table name related to the class.
If class is not annotated by @EqlTable, the default table name will be underscore_lowercase converted from CamelClass name. eg. Person to person, PersonInfo to person_info.

**@EqlId**

To specify whether the field is a Primary Key in the table.
If the property name is **id**, it is also regarded as implicit @EqlId.

**@EqlColumn**

To specify the column name which is different with POJO's property name.
When non-annotated with @EqlColumn, the default column name will be underscore_lowercase converted from propertyName. eg. name to name, personName to person_name.

**@EqlSkip**

To skip the mapping to table field.

**CRUD**

the update and delete api will use the id field as its condition.
the read api will use all the non-null fields as its combined condition.

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
In order to simplify the eql api usage, here eqler is introduced.
Eqler is an interface wherein the methods is used to execute sql and process results.
An Eqler instance is created by EqlerFactory.
The following are examples:

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
+ Inline comment such as `/* iff ... */` is parsed by regular expression, and this method will not ignore `/* ... */` in the literal string such as `'literal string /* if xxx */'`.

# FAQ
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

Try use url like  `jdbc:mysql://127.0.0.1:13306/dba?useUnicode=true&&characterEncoding=UTF-8&connectTimeout=3000&socketTimeout=3000&autoReconnect=true` instead of `jdbc:mysql://127.0.0.1:13306/dba`

# FAQ
## java.lang.NullPointerException
A single primitive return type like int/long/short will cause NPE when SQL results no rows. 
In this situation, the related wrapper type like Integer/Long/Short should be used instead.
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
## IDEA plugin 
[eql-plugin](https://github.com/Bpazy/eql-plugin)

# docker
## MySQL

1. `docker pull mysql:5.6`
2. `docker run -p 13306:3306 --name mysql -e MYSQL_ROOT_PASSWORD=my-secret-pw -d mysql:5.6`
3. `mysql -uroot -pmy-secret-pw  -h127.0.0.1 -P13306` 或者进入容器 `docker exec -it mysql bash`， 执行`mysql -uroot -pmy-secret-pw`
4. 
	```sql
	create database dba;
	create database dbb;
	create database dbc;
	```

## ORACLE
1. `docker pull wnameless/oracle-xe-11g`
2. `docker run -d -p 49160:22 -p 49161:1521 -e ORACLE_ALLOW_REMOTE=true wnameless/oracle-xe-11g`
3. download oracle jdbc from [oracle website](http://www.oracle.com/technetwork/database/features/jdbc/index-091264.html)
4. `mvn install:install-file -Dfile=ojdbc6.jar -DgroupId=com.oracle -DartifactId=ojdbc6 -Dversion=11.2.0.4.0 -Dpackaging=jar`
Connect database with following setting:

```
hostname: localhost
port: 49161
sid: xe
username: system
password: oracle
Password for SYS & SYSTEM

Login by SSH
ssh root@localhost -p 49160
password: admin
Support custom DB Initialization
```

# OGNL 相关知识
eql默认使用OGNL表达式来做动态条件SQL的判断，OGNL表达式可以参见[ognl language guide](https://commons.apache.org/proper/commons-ognl/language-guide.html).
## 注意项
`'a'` 表示单个字符a，要表示字符串a，需要使用双引号`"a"`;
`'ab'`和`"ab""` 都可以表示字符串ab。

## OGNL has the following kinds of constants:

1. String literals, as in Java (with the addition of single quotes): delimited by single- or double-quotes, with the full set of character escapes;
2. Character literals, also as in Java: delimited by single-quotes, also with the full set of escapes;
3. Numeric literals, with a few more kinds than Java. In addition to Java's ints, longs, floats and doubles, OGNL lets you specify BigDecimals with a "b" or "B" suffix, and BigIntegers with an "h" or "H" suffix (think "huge"---we chose "h" for BigIntegers because it does not interfere with hexadecimal digits);
4. Boolean (true and false) literals;
5. The null literal.

If you want to compare variable with string in dynamic sql, be careful with single or double quotes.

## Testing code:

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

