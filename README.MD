###Features

> long long ago,we usually send data to the other activity like the following codes

```java
    Intent intent=new Intent();
    intent.putExtra("name","John");
    intent.putExtra("sex","male");
    intent.setClass(this,SecActivity.class);
    startActivity(intent);
```

>But Now,we can also do like this

```java
    WeSecActivity.getIntance().setName("john").setSex("male").start(MainActivity.this);
```
> It has some advantages
###How does it works?
###How to use?