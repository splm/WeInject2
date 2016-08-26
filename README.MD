###最前面

###功能
> “这是一个关于android 的跳转库。而在androind中跳转表示这什么呢，一般的理解就是activity和activity,fragment之间的跳转，以及传值了。”上述文字来源于andjump依赖库的文档说明。

###说明
> 无意当中看到大神编写的基于注解传值的AndJump项目，该项目只需要在变量上加入特定注解即可在目标activity中接收变量值，并且会自动构建Java类。原作者的想法不错，因此对AndJump这个项目产生好奇之心，但却没有找到项目的核心部分（可能粗心），因此自己编写名字叫`WeInject2`的`Demo`来重新实现了一遍原作者的AndJump功能，并开放核心部分，方便其他朋友研究学习。由于时间问题，目前仅用来传递字符串，至于其他引用类型，后续加入。就上面说的自动构建类的一说，如果巧妙利用可以成为懒人利器。

###解决的问题

> `比如我们想从mainActivity传值到SecActivity`

> 以前我们这样传值

```java
    Intent intent=new Intent();
    intent.putExtra("name","John");
    intent.putExtra("sex","male");
    intent.setClass(this,SecActivity.class);
    startActivity(intent);
```

>现在

```java
    //发起
    WeSecActivity.getIntance().setName("john").setSex("male").start(MainActivity.this);
```

```java
    //接收
@WeInject
public class SecActivity extends AppCompatActivity {
    @WeInject
    public String name;
    @WeInject
    public String sex;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sec);
        WeSecActivity.inject(this);
        .....
        showBridgeData_tv.setText("ReName:--"+name+"---ReSex:"+sex);
    }
}
```

### 几个技术要点
> - 明确`Java注解`几种运行周期和作用域；
> - 了解`JavaPoet`的是基础语法和原理；
> - 了解`注解处理器`的玩法；
> - `apt`。