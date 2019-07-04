package jianqiang.com.hostapp;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.jianqiang.mypluginlibrary.IBean;
import com.example.jianqiang.mypluginlibrary.ICallback;

import java.io.File;
import java.lang.reflect.Method;

import dalvik.system.DexClassLoader;
import jianqiang.com.hostapp.utils.Utils;

public class MainActivity extends Activity {

    private String dexpath = null;    //apk文件地址
    private File fileRelease = null;  //释放目录
    private DexClassLoader classLoader = null;

    //三种方式均支持: apk/apk改jar后缀/抽出其中的dex
//    private String apkName = "classes.dex";
//    private String apkName = "plugin1.jar";
    private String apkName = "classes.dex";

    TextView tv;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(newBase);
        try {
            Utils.extractAssets(newBase, apkName);
        } catch (Throwable e) {
            loge(e);
        }
    }

    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        initClassLoader();
        initUiAndClick();
    }

    private void initClassLoader() {
        log("apkName: " + apkName);
        File extractFile = this.getFileStreamPath(apkName);
        log("extractFile: " + extractFile.getAbsolutePath());
        dexpath = extractFile.getPath();
        log("dexpath: " + dexpath);

        fileRelease = getDir("dex", 0); //0 表示Context.MODE_PRIVATE
        log("fileRelease: " + fileRelease);
        classLoader = new DexClassLoader(dexpath,
                fileRelease.getAbsolutePath(), null, getClassLoader());
    }

    private void initUiAndClick() {
        Button btn_1 = (Button) findViewById(R.id.btn_1);
        Button btn_2 = (Button) findViewById(R.id.btn_2);
        Button btn_3 = (Button) findViewById(R.id.btn_3);
        Button btn_4 = (Button) findViewById(R.id.btnSanbo);
//        Button btn_5 = (Button) findViewById(R.id.btn_5);

        tv = (TextView) findViewById(R.id.tv);

        //普通调用，反射的方式
        btn_1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Class mLoadClassBean;
                try {
                    mLoadClassBean = classLoader.loadClass("jianqiang.com.plugin1.Bean");
                    Object beanObject = mLoadClassBean.newInstance();

                    Method getNameMethod = mLoadClassBean.getMethod("getName");
                    getNameMethod.setAccessible(true);
                    String name = (String) getNameMethod.invoke(beanObject);

                    tv.setText(name);
                    Toast.makeText(getApplicationContext(), name, Toast.LENGTH_LONG).show();
                    log("name:" + name);
                } catch (Exception e) {
                    loge(e);
                }
            }
        });


        //带参数调用
        btn_2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                try {
                    Class mLoadClassBean = classLoader.loadClass("jianqiang.com.plugin1.Bean");
                    Object beanObject = mLoadClassBean.newInstance();

                    IBean bean = (IBean) beanObject;
                    bean.setName("Hello");
                    log("带参数Hello: " + bean.getName());
                    tv.setText(bean.getName());
                } catch (Exception e) {
                    loge(e);

                }

            }
        });

        //带回调函数的调用
        btn_3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                try {
                    Class mLoadClassBean = classLoader.loadClass("jianqiang.com.plugin1.Bean");
                    Object beanObject = mLoadClassBean.newInstance();

                    IBean bean = (IBean) beanObject;

                    ICallback callback = new ICallback() {
                        @Override
                        public void sendResult(String result) {
                            log("回调:" + result);
                            tv.setText(result);
                        }
                    };
                    bean.register(callback);
                } catch (Exception e) {
                    loge(e);
                }

            }
        });

        //自定义测试
        btn_4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Class mLoadClassBean;
                try {
                    log("调用..cn.sanbo.WTF.init()....开始");
                    mLoadClassBean = classLoader.loadClass("cn.sanbo.WTF");
//                    Object beanObject = mLoadClassBean.newInstance();

                    Method init = mLoadClassBean.getMethod("init", Context.class, String.class);
                    init.setAccessible(true);
//                    String name = (String) getNameMethod.invoke(beanObject);
                    init.invoke(null, getApplicationContext(), "我是谁?");
                    log("调用..cn.sanbo.WTF.init()....结束");

                } catch (Exception e) {
                    loge(e);
                }
            }
        });

//        // 更新自己调用
//        btn_5.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View arg0) {
//
//                try {
//                    Utils.extractAssets(getApplicationContext(), "testactivity.apk");
//                    File dexFile = getFileStreamPath("testactivity.apk");
//                    log("");
//                    File optDexFile = getFileStreamPath("testactivity.dex");
//                    BaseDexClassLoaderHookHelper.patchClassLoader(getClassLoader(), dexFile, optDexFile);
//                    startActivity(new Intent(MainActivity.this, MainActivity.class));
//
//                } catch (Exception e) {
//                    loge(e);
//                }
//            }
//        });
    }

    private void log(String log) {
        Log.i("sanbo", log);
    }

    private void loge(Throwable e) {
        Log.e("sanbo", Log.getStackTraceString(e));
    }
}