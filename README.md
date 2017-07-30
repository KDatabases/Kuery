# Kuery
MySQL Kotlin wrapper based on HikariCP

## How to get it!
```xml
<dependency>
    <groupId>com.sxtanna.database</groupId>
    <artifactId>Kuery</artifactId>
    <version>LATEST</version>
</dependency>
```

## How it works!

### 1-0. To create a new instance of [Kuery](src/main/kotlin/com/sxtanna/database/Kuery.kt), you would follow this syntax

#### From Kotlin
``` kotlin
val kuery = Kuery[file: File]
val kuery = Kuery[config: KueryConfig]
```

#### From Java
``` java
final Kuery kuery = Kuery.get(file: File);
final Kuery kuery = Kuery.get(config: KueryConfig);
```

### 1-1. To initialize and shutdown a database use these two methods
``` java
kuery.enable()
```
and
``` java
kuery.disable()
```


### 2-0. After you have an instance, to get a resource

#### From Kotlin
``` kotlin
val resource = kuery.resource()
```

#### From Java
``` java
final Connection resource = kuery.resource();
```
*[Database#resource](https://github.com/KDatabases/Core/blob/master/src/main/kotlin/com.sxtanna/database/base/Database.kt#L96) will throw an IllegalStateException if it's unable to into a resource a/o the database isn't enabled*


### 2-1. Or you could utilize the Database's ability to automatically manage the connection with various sql functions

#### Creating a Table
##### From Kotlin
``` java
kuery {
  create("User", "name" co VarChar.of(255, true)) // deprecated in favour of the cacheable builders
ex.
  Create.table("User").co("name", VarChar(255, true))()
}                                                                                                                                           
```

##### From Java
``` java
kuery.execute(task -> {                                                                  
  task.execute(Create.table("User").co("name", new VarChar(255, true)));
});                                                                                    
```

##### Storing a Create Statement (syntax is nearly the same for Kotlin and Java)
``` java
val createUser = Create.table("User").co("name", VarChar.of(255, true))

vs 

final CreateBuilder createUser = Create.table("User").co("name", VarChar.of(255, true));
```
#### Using them however is quite different
##### From Kotlin
``` java
kuery {
  createUser()
}
```
##### From Java
``` java
kuery.execute(task -> {
  task.execute(createUser);
});
```

## More examples soon.
#### For Java examples.
- [JKueryTest](src/test/java/com/sxtanna/database/tests/JKueryTest.java)
- [JKueryTestOrm](src/test/java/com/sxtanna/database/tests/JKueryTestOrm.java)
#### For Kotlin examples.
- [KKueryTest](src/test/kotlin/com/sxtanna/database/tests/KKueryTest.kt)
- [KKueryTestOrm](src/test/kotlin/com/sxtanna/database/tests/KKueryTestOrm.kt)
