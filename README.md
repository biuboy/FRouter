# FRouter 
implementation 'com.github.biuboy:FRouter:v2.2.2' annotationProcessor 'com.github.biuboy.FRouter:arouter-compiler:v2.2.2'

在Activity或者Fragment添加注解@ARouter(path = "/app/MainActivity")

跳转的地方使用 RouterManager.getInstance().build("/app/NextActivity") .navigation(this);
