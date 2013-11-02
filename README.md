eql
====

an easy sql of an alternative to ibatis.

#One minute tutorial
* copy [eql-DEFAULT.properties](https://github.com/bingoohuang/eql/blob/master/src/test/resources/eql/eql-DEFAULT.properties) to your classpath esql and do some changes for your database connection info such as url, password and username.
* create a Demo class.
* create a Demo.eql resouce in classpath.
* write following code in your Demo main method:

```java
String str = new Eql().selectFirst("demo").execute();
``` 

* write an eql in Demo.eql

```sql
-- [demo]
select 'X' from dual
```

* that's all. and you see it is very simple.

#Return one row

```java
Map<String, String> row = new Eql().selectFirst("oneRow").execute();
```

```sql
-- [oneRow]
select 'X', 'Y' from dual
```

#Return a Javabean

```java
XYBean xy = new Eql().selectFirst("javabean").execute();
```

```sql
-- [javabean returnType=XYBean]
select 'X', 'Y' from dual
```

#Return rows

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

#With parameters in sequence

* example 1

```java
String x = new Eql().selectFirst("autoSeq1")
          .params("x")
          .execute();
```

```sql
-- [autoSeq1]
select 'X' from dual
where 'x' = ##
```

* exmpale 2

```java
String x = new Eql().selectFirst("autoSeq2")
          .params("x", "y")
          .execute();
```

```sql
-- [autoSeq2]
select 'X' from dual
where 'x' = ##
and 'y' = ##
```

* exmpale 3

```java
String x = new Eql().selectFirst("autoSeq3")
          .params("x", "y")
          .execute();
```

```sql
-- [autoSeq3]
select 'X' from dual
where 'x' = ##
and 'y' = ##
```

* exmpale 4

```java
String x = new Eql().selectFirst("autoSeq4")
          .params("y", "x")
          .execute();
```

```sql
-- [autoSeq4]
select 'X' from dual
where 'x' = #2#
and 'y' = #1#
```

#With parameters by properties name

* example 1

```java
String x = new Eql().selectFirst("bean")
       .params(new XyBean("x", "y"))
       .execute();
```

```sql
-- [bean]
select 'X' from dual
where 'x' = #x#
and 'y' = #y#
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
where 'a' = #x#
and 'b' = #y#
```

# Dynamic sql

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
where 'a' = #x#
-- end
```

or use more compact syntax

```sql
-- [ifDemo]
select 'X' from dual /* if x == "a" */  where 'a' = #x# /* end */
```

## iff

```sql
-- [ifDemo]
select 'X' from dual
-- iff x == "a"
where 'a' = #x#
```

or use more compact syntax

```sql
-- [ifDemo]
select 'X' from dual /* iff x == "a" */  where 'a' = #x#
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
```

or with default keyword

```sql
-- [switchSelectWithDefault returnType=org.n3r.eql.SimpleTest$Bean]
SELECT A,B,C,D,E
FROM ESQL_TEST
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
FROM ESQL_TEST
-- isEmpty a
WHERE A in (1,2)
-- end

-- [isNotEmpty]
SELECT B
FROM ESQL_TEST
-- isNotEmpty a
WHERE A = #a#
-- end
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
FROM ESQL_TEST
WHERE C = ##
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
WHERE 'x' = ##


-- [replace2]
SELECT 'x'
FROM $table$
WHERE 'x' = ##
```

# Batch execute

```java
Eql esql = new Eql();
esql.startBatch(/*batchSize*/10);
for (int i = 0; i < 10; ++i) {
String orderNo = randLetters(10);
String userId = randLetters(10);
int prizeItem = randInt(10);
int ret = esql.insert("insertPrizeBingoo")
       .params(orderNo, "Olympic", "" + prizeItem, userId)
       .execute();

assertEquals(0, ret);
}

esql.executeBatch();
```

```sql
-- [insertPrizeBingoo]
INSERT INTO EQL_TEST_BINGOO(ORDER_NO, ACTIVITY_ID, ITEM_ID, USER_ID, BINGOO_TIME)
VALUES(##, ##, ##, ##, SYSDATE)
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
DROP TABLE ESQL_BLOB;
CREATE TABLE ESQL_BLOB (BOB BLOB);
INSERT INTO ESQL_BLOB(BOB) VALUES(#:LOB#)

-- [selectBlob]
SELECT BOB FROM ESQL_BLOB

-- [selectBlobString returnType=string]
SELECT BOB FROM ESQL_BLOB


-- [selectBlobAsResult returnType=org.n3r.eql.JavaBlobTest$AsResult]
SELECT 1 as seq, BOB as remark FROM ESQL_BLOB

-- [updateBlob]
UPDATE ESQL_BLOB SET BOB = #:LOB#
```


     


