# jmeter-remote-tool
### 项目结构
1. controller中放置页面响应方法,和其他rest api代码
2. service中放置具体业务逻辑
3. application中jmeter.config下配置意义:
    1. work_path:工作路径,指jmeter脚本上传后存放路径,以及测试完成后生成的报告存放路径
    2. hosts_file:当前机器hosts文件路径
    3. base_dir:当前机器jmeter程序bin路径
    4. slave_port:从机监听的端口号
    5. jtl_file:测试报告名称
    6. jmx_file:测试脚本名称    

### 执行顺序
1. 启动项目后,访问localhost:8080/index.html
2. 首先上传jmeter脚本文件,文件上传完成后页面会提示true,jmeter脚本文件默认存放路径/root/jmeter-remote-tool/目录下,可以修改application中的work_path的值来改变
3. 设置slave机器的数量
4. 在虚拟机域名输入框中写入集群的虚拟域名或类似的东西
5. 点击提交按钮执行测试

### 残留问题
1. 各种异常情况处理代码没有编写
2. 测试页面还需要重新编写,样式,js等