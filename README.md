# 分布式ID生成工具

## UID添加到项目中
1. 添加依赖
```xml
    <dependency>
      <groupId>com.zcckj.uid</groupId>
      <artifactId>uid-spring-boot-starter</artifactId>
      <version>1.1.0</version>
    </dependency>
```


2. apollo public config  `development.uid.generator`



## UID配置说明

推荐配置

* uid.timeBits 32
* uid.seqBits 10
* uid.type cached
* uid.epochStr 2014-12-31
* uid.service-ip.preferred-networks 指定一个IP前缀 
* uid.service-ip.default-ip-address 强制指定一个IP（优先级最高）
```yaml
uid:
  timeBits: 32
  workerBits: 21
  seqBits: 10
  zookeeperConnection: 171.188.0.161:2181
  type: cached
  epochStr: 2014-12-31
  service-ip:
    preferred-networks: 10.1.80
server:
  port: 8084
```
## workId分配的思路

workId的分配是根据服务的IP#PORT来的，如果之前在zookeeper中有分配过，会使用已经分配过的workId,未分配过那么会使用zookeeper的持久顺序节点生成一个节点(路径为/uid-generator/workId/sequence/服务IP#服务PORT-数字序列)，
然后使用节点的序列后缀作为workId,并将此workId缓存在zookeeper中(路径为/uid-generator/workId/storage/服务IP#服务PORT)。