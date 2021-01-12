# JConsoleApplication
> 基于`Java8`，开发控制台应用的轻量级的框架，简化控制台应用的开发过程。
>
> 框架的核心功能是使用一个工厂管理`java`方法，使得其他位置可以不受限制的的用工厂管理的方法并接收结果，同时框架提供一个命令解释器，可以将字符串格式的命令映射到`java`方法的参数上并执行，可以像执行`liunx`命令一样调用`java`方法，命令名称即是方法名称，命令参数即是方法参数。
>
> 以此为基础扩展出其他实用的功能，例如应用事件监听器，命令执行条件、自定义类型解析，可选参数和参数默认值等。
>
> 另外框架提供了多种开始方式，用于不同的应用场景。

- 注解式开发，通过标记注解实现对应的功能



## 相关技术

`Java8`、`maven`、注解、反射

几乎不使用第三方库，所以框架体量非常小，所有功能都由代码实现，仅有的两个依赖其中一个是单元测试，只在演示应用启动时使用，另外一个是`lombok`插件，后续可能会移除这个依赖。



## 基本功能

> 像调用`liunx`命令一样调用`java`方法

### 命令的语法

先说一下主要的语法

- **基本命令语法:** 一条命令主要由**命令名**和**命令参数**构成，例如`add 12 13`，这一条命令中`add`是命令名，对应一个名为add的方法，`12`和`13`是命令参数，也对应add方法的参数，当方法没有参数时，命令也不需要参数。

- **命令的别名:** 一个方法对应一个命令，但是一个命令还可以有一个别名，举个例子:

  ```java
  @Cmd(name = "man")
  public void help() {
      System.out.println("hello world!");
  }
  ```

  输入`man`或者`help`效果相同，都会在控制台上输出`hello world!`

- **可选参数功能:**  当一个方法有多个参数，但是这些参数并不都是必要的，可以通过框架的`@Opt`注解实现可选参数功能，例如：

  ```java
  @Cmd
  public void get(@Opt('a') boolean a, @Opt('b') boolean b, @Opt('c') boolean c) {
  }
  ```

  对于`@Opt`注解的其他用法后面会提到，就这样的一个方法，每个方法参数上都有这个注解，并且有一个字符来标识这个参数，这样我们可以这样调用这个方法

  `get -a true -b true -c true`

  这条命令中，有三组参数项，第一组是`-a true`，其中`-a`是**参数名**，对应方法参数`a`，`true`是**参数的值**，这个值会被解析后映射到方法参数`a`上，后面的以此类推，在方法中可以观察到，abc三个传进来的参数都是`true`了。

  当然，对于布尔值有更简便的写法，如：

  `get -a -b -c`

  还能更简单：

  `get -abc`

  这些可选参数的出现顺序是没有要求的，而且参数属性的方式可以在一条命令中混合出现，例如:

  `get -b -ac`  等于 `get -a true -b -c true`  等于 `get -c -b -a`

  **注意:** 标记了`@Opt`注解后，如果命令中没有对此方法参数设置值，则这个方法参数为默认值。布尔类型为`false`，数值类型为`0`，引用类型为`null`，举例子: 还是上面的方法，如果这个时候输入命令`get -ac`，在方法中观察，则`a`和`c`为`true`，`b`为`false`

- **参数的全称和简称:** 在`@Opt`注解中，可以指定一个属性`fullName` , 比如下面的一个方法：

  ```java
  @Cmd
  public void find(@Opt(value = 's', fullName = "name") String name, @Opt('a') int age) {}
  ```

  这样的一个方法，可以这样调用:

  `find -s jack`  或者 `find --name jack` ， 这两个命令效果相同。

  **注意:** 如果一个方法上的参数都有`@Opt`注解，那么可以按照方法参数的顺序来填充属性，例如上面的方法可以这样调用

  `find jack`  等于在代码中调用 `find("jack", 0)`; `find jack 11` 等于`find("jack", 11)`
  
- **集合:**  可以在方法参数中使用集合，例如数组，列表、集合

  ```java
  @Cmd
  public void show(int[] arr, List<Double> list, Set<String> set) {
      System.out.println(arr);
      System.out.println(list);
      System.out.println(set);
  }
  ```

  命令中用英文逗号将各个数据项分隔

  `show 1,2,3,1 1,2,3,1 1,1,1,2`

  

### 启动配置

有两种开始方式，第一种启动后会在控制台等待输入，根据控制台的输入进行处理命令，将结果在控制台输出。另外一种是获取一个解释器对象，由解释器在解析字符串格式的命令。

- **启动控制台应用**

  需要编写一个用于启动的`main`方法，然后在`main`方法中调用`ApplicationRunner.consoleApplication(conf)`这个方法，方法参数是对于控制台应用的一些配置，以下是完整的启动及配置示例:

  ```java
  public static void main(String[] args) {
      ApplicationRunner.consoleApplication(
              Commons.config()
                      // 应用信息
                      .appName("测试应用示例") // 应用的名称
                      .printWelcome(false)  // 是否打印欢迎信息
                      .prompt("example> ")  // 控制台输入的提示符
                      .printStackTrace(false) // 遇到异常时是否打印调用栈
                      .exitCmd(new String[] {"exit", "e.", "q"}) // 使用这些命令可以退出应用
                      .maxHistory(128) // 最多保存的历史记录，
          			// 编辑作者信息，当printWelcome设置为false时，这些信息不会被输出
                      .editAuthorInfo()
                          .authorName("fd")
                          .email("~~")
                          .comment("备注: ~~")
                          .createDate("2020/12/27")
                          .updateDate("2021/1/11")
                          .ok()
                      // 设置系统启动时执行的命令
                      .addInitCommands()
                          .getFromFile("init.txt") // 从文件中读取
                          .add("find --tag usr")   // 查询所有的用户命令
                          .add("help --name help") // 获取 help 命令的使用帮助
                          .ok()
                      // 增加命令工厂，enable参数决定是否启用该命令工厂，将false修改为true可以开启对应命令工厂的测试，
                      // 但是为了方便功能演示，建议测试以下几个类的时候，每次只有一个工厂类enable为true
                      .addCommandFactories()
                          .add(QuickStart.class, true)
                          .add(AdvancedDemo.class, false)
                          .add(ListenerDemo.class, false)
                          .add(LoginDemo.class, false)
                          .ok()
                      .addHelpFactory(HelpForDemo.INSTANCE) // 加入命令帮助
                      // 设置完成，应用启动
                      .build());
  }
  ```

  启动效果

  ![启动](./imgs/启动.png)
  `example> `上面的这些内容是配置中执行了`find --tag usr` 和`help --name help`这两个命令输出的结果，这两个命令都是系统预置的，无需编写任何代码就能用的命令，像这样的命令还有好几个，后面会写。
  
  先说一下，这里`find --tag usr` 是输出当前注册到系统中的用户命令，右边的命令对于的方法信息，方法所在的类，参数，和返回值。查询系统命令使用`find --tag sys`
  
- **创建命令工厂**

  新建一个类做为命令工厂，甚至无需新建类，任何类都可以做为命令工厂，在这个类中需要用命令来调用的方法上标记`@Cmd`注解，然后启动应用后就能在控制台上根据命令调用这些方法了

  有两点需要注意：

  - 标记`@Cmd`注解的方法必须是非静态方法才能被系统识别，方法的访问属性不做要求，`private`或者`public`都行。
  - 做为命令工厂，这个类必须提供无参的构造方法，否则系统无非实例化这个类。

  ![命令工厂示例](./imgs/命令工厂示例.png)

![命令工厂设置](./imgs/命令工厂设置.png)

![测试命令方法](./imgs/测试命令方法.png)





----

## 扩展功能

### @Opt 注解

```java
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface Opt {

    char value();
    String fullName() default "";
    boolean required() default false;
    String defVal() default "";

}
```

这段代码是这个注解的源码，使用在方法参数上，有4个属性:

- **value:**  在命令中可以用value来为指定的参数赋值，这个功能前面已经介绍过了。
- **fullName:**  这个在之前也介绍过了，指定一个参数的全称，一般使用`--`做为参数名的前缀，效果与value相同。
- **required:**  标记这个参数是否是必选项，默认这个属性是`false`，假如标记为`true`则当命令缺少此参数时方法不会被执行，且抛出异常。
- **defVal:**  默认值功能，用于在命令中没有选中此参数，则这个参数应用默认值，虽然是这个属性是`String`类型，但是会由系统向目标类型进行转换，只需要写上自己需要的数值的字符串形式就可以了。



### @Cmd 注解

### 命令工厂的生命周期

### 系统事件监听器



