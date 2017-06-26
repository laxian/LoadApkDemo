# LoadApkDemo
学习加载另一个apk中的资源，以及更多未知功能探索学习。

## host
宿主apk,主要测试了从一个已安装的app中,从另一个未安装的apk包中读取资源, 和启动自身未在Manifest.xml注册的Activity

1. 读取一个apk里的资源，需要知道apk的路径，名字，包名，还需要资源的类型和资源的名字。
以drawable类型资源为例，资源的id定义在R$drawable里。资源的名字就是R$drawable的一个字段，
我们可以通过反射，获取这个字段。不过首先得创建一个classloader：
```
    // 1、包含dex的apk文件或jar文件的路径;
    // 2、apk、jar解压缩生成dex存储的目录;
    // 3、本地library库目录，一般为null;
    // 4、父ClassLoader;
    DexClassLoader dexClassLoader = new DexClassLoader(
            apkDir + File.separator + apkName,
            optimizedDirectoryFile.getPath(), 
            null, 
            ClassLoader.getSystemClassLoader());
            
    //使用apk自己的类加载器，反射出R类中相应的内部类进而获取我们需要的资源id
    Class<?> clazz = dexClassLoader.loadClass(apkPackageName + ".R$" + resType);
    
    //得到相应资源字段field
    Field field = clazz.getDeclaredField(resName);
    //得到资源id
    return field.getInt(R.id.class);
```


2. 获得apk的Resource，将apk的资源添加到classpath。AssetManager有一个隐藏的方法addAssetPath，通过反射调用，
将apk路径添加进去。
```
private Resources getPluginResources(String apkName) {
    try {
        //创建AssetManager对象
        AssetManager assetManager = AssetManager.class.newInstance();
        
        //反射调用方法addAssetPath(String path)，这是一个@hide方法
        Method addAssetPath = assetManager.getClass().getMethod("addAssetPath", String.class);
        
        //将未安装的Apk文件的添加进AssetManager中，第二个参数为apk文件的路径带apk名
        addAssetPath.invoke(assetManager, apkDir + File.separator + apkName);
        
        //获得apk的Resource对象
        Resources superRes = this.getResources();
        return new Resources(assetManager, superRes.getDisplayMetrics(),
                superRes.getConfiguration());
    } catch (Exception e) {
        e.printStackTrace();
    }
    return null;
}
```


3. 使用资源：
获得了资源id和Resourde对象，就可以获得资源，然后就可以使用了。
        
```
resources = getPluginResources(apkName);
    try {
        int imgId = getResIdFromApk(apkDir, apkName, pkgName, "drawable", "guest_img");
        Drawable drawable = resources.getDrawable(imgId);
        imageView.setImageDrawable(drawable);

        int txtId = getResIdFromApk(apkDir, apkName, pkgName, "string", "guest_text");
        String txt = resources.getString(txtId);
        textView.setText(txt);
    } catch (Exception e) {
        e.printStackTrace();
    }
```
    
4. 加载类
        
```
    Class<?> demoClass = getClassFromApk(dexClassLoader, pkgName, "Demo");

    // 反射调用对象方法
    Method func1 = demoClass.getMethod("func1");
    String func1Msg = (String) func1.invoke(demoClass.newInstance());
    editText.setText(func1Msg);

    // 反射调用类方法
    Method func2 = demoClass.getMethod("func2");
    String func2Msg = (String) func2.invoke(demoClass);
    editText.setText(String.format("func1 -> %s \nfunc2 -> %s", editText.getText(), func2Msg));
    Log.d("abc", demoClass.toString());
```

5. 启动未注册的Activity

这种方式启动hook ActivityManagerService,用StubActivity代替未注册Activity,绕过AMS检查.
在ActivityThread 中hook H(extends Handler)成员变量,换回真实启动的Activity,完成启动未注册Activity.
同样,也可以启动guest apk的Activity. 但是guest apk里的资源无法加载.和host apk里资源id重复的,会加载host apk里的资源,
在host apk 找不到的,抛出Resources$NotFoundException异常.

        
6. 启动guest apk(未安装apk)的Activity
    该方式启动Activity能成功,但是无法加载xml资源.

## classloader-hook
解决合并pathList方法启动guest apk无法加载资源的问题,
1. 思路:

通过反射PackageParser(@hide类,不同版本需要适配)加载apk,生成ApplicationInfo对象
通过反射调用ActivityThread.getPackageInfoNoCheck, 传入ApplicationInfo生成LoadedApk对象
通过反射,将guest apk生成LoadedApk对象,添加到ActivityThread.mPackages.

2. 验证如下问题
+ 从host app跳转到guest apk, 
+ xml资源加载, 
+ guest apk 内部跳转
+ 以及从guest apk跳回host app

跳转代码:
```
    Intent intent = new Intent();
    intent.setComponent(new ComponentName(pkgName, pkgName + ".MainActivity"));
    startActivity(intent);
```

## guestapk
被加载的apk:
+ `getFileStreamPath("test_dir");`
> /data/data/com.zhouweixian.host/files/test_dir
+ `intent.setClassName("com.zhouweixian.host", "com.zhouweixian.host.MainActivity");`
> 正常跳转
+ `startActivity(new Intent(this, HotFixActivity.class));`
> 正常跳转


## runapp
代码来自:https://github.com/bangelua/DynamicLoadApk

## instrumentation-hook
[DroidPlugin](https://github.com/DroidPluginTeam/DroidPlugin)代码学习小demo