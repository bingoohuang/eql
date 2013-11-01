eql
====

an easy sql of an alternative to ibatis.

#One minute tutorial
* copy [eql-DEFAULT.properties](https://github.com/bingoohuang/eql/blob/master/src/test/resources/eql/eql-DEFAULT.properties) to your classpath esql and do some changes for your database connection info such as url, password and username.
* create a Demo class.
* create a Demo.eql resouce in classpath.
* write following code in your Demo main method:
```
String str = new Eql().selectFirst("demo").execute;
``` 
* write an eql in Demo.eql
```
-- [demo]
select 'X' from dual
```
* that's all. and you see it is very simple.




