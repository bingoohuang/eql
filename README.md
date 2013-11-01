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


