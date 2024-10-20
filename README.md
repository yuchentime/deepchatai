## 使用前置
- 安装Neo4j
- 智谱清言api key

## Neo4j相关配置
访问http://{IP地址}:7474/browser/，在命令输入栏中，执行如下**创建索引**命令。

**创建索引**
```
CREATE VECTOR INDEX deepReflectionChat IF NOT EXISTS
FOR (m:DeepReflectionChat)
ON m.embedding
OPTIONS { indexConfig: {
 `vector.dimensions`: 1024,
 `vector.similarity_function`: 'cosine'
}}
```
**其他常用语句**
```
-- 查询label是‘DeepReflectionChat’的前25条记录
MATCH (n:DeepReflectionChat) RETURN n LIMIT 25

-- 删除索引
drop index deepReflectionChat

-- 查询所有索引
SHOW VECTOR INDEXES
```
