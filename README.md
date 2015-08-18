aldan3-jdo
==========

Simple JDO library pulled from Aldan 3

## How to use

Let's have an object

```java
public class MyFirstJDO {
   public int id; // remember use public qualifier for all JDO exposed fields
   public String name;
   public Date date;
   String noJDO;
}
```

Annotate all JDO exposed attributes

```
@DataRelation
public class MyFirstJDO {
   @DBField
   public int id; // remember use public qualifier for all JDO exposed fields
   @DBField(size = 200)
   public String name;
   @DBField
   public Date date;
   String noJDO;
}
```

### create table

```java
try {
   new SchemaCreator().create(model_package_name, getDOService(), true);
} catch (ProcessException e) {
   log("", e);
}
```

### add data
```java
MyFirstJDO jdo = new MyFirstJDO();

// fill fields
try {
   getDOService().addObject(new DODelegator(jdo, null, "", "id"));
   log("new id: %d", null, jdo.id);
} catch (Exception e) {
   log("", e);
}
```

### retrieve data

```java

MyFirstJDO jdo = new MyFirstJDO();
jdo.id=1;

try {
   getDOService().getObjectLike(new DODelegator(jdo, null, "", "id"));
   log("retrieved name: %s", null, jdo.name);
} catch (Exception e) {
   log("", e);
}
```
