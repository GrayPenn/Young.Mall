# Young.Mall
秒杀商城项目-知识点总结


1、分布式 session
  登录成功-给用户生成类似 sessionId 的东西（token）用来标识用户-写入 cookie 中传入客户端-客户端在随后的访问中都会在 cookie 中带上这个token-服务端拿到 token 后根据 token 获取用户信息


2、秒杀优化方案
	 2.1页面缓存+URL 缓存（指的是不同的详情 ID 对应的页面缓存）+对象缓存（比如用户信息step1 更新数据库，step2 更新缓存等）
	 2.2页面静态化（304），前后端分离
         页面为纯的 HTML，通过异步请求服务端获取数据，渲染页面，浏览器会缓存 html 到客户端
   2.3静态资源优化
	 2.4CDN 优化


3、超卖处理
   在执行 update 的时候使用 update 的原子性特征，
   3.1where 条件中添加条件 stock_count > 0
   3.2防止下单多个，加一个唯一索引



4、秒杀接口优化
  思路：减少数据库访问
  4.1 系统初始化，把商品库存数量加载到 Redis（Controller implements InitializingBean 接口 重写 afterPropertiesSet 方法，系统启动的时候就会自动执行；使用内存标记，将已经秒杀结束的商品添加到内存中，new HashMap ）
  4.2 收到请求，redis 预减库存，库存不足，直接返回，否则进入3
  4.3 请求入队，立即返回排队中
  4.4 请求出队，生成订单，减少库存
  4.5 客户端轮询，是否秒杀成功


5、Rabbit
  三种消费模式
  direct 交换器（最简单的一种）
  fanout 交换器 广播模式
  topic 交换器
   消息放到 Exchage 中，通过绑定，再将消息放到 Queue 中
  headers 交换器（其中headers交换器允许你匹配AMQP消息的header而非路由键，除此之外headers交换器和direct交换器完全一致，但性能却很差，几乎用不到，所以我们本文也不做讲解。） 



6.接口防刷
  6.1秒杀接口地址隐藏
   秒杀开始之前，先去请求接口获取秒杀地址
   6.1.1接口改造，带上 PathVariable 参数（uuId+随机参数进行 MD5生成验证参数，根据用户的 id+商品 id 存入 Redis）
   6.1.2添加生成地址的接口
   6.1.3秒杀收到请求，先验证 PathVariable（根据用户id+商品 id，在Redis 中查询）
  6.2数学公式验证码
  6.3接口限流防刷
   6.3.1通过拦截器，将ur i+用户 id为 key，存入 redis，在拦截器中计算用户访问次数

