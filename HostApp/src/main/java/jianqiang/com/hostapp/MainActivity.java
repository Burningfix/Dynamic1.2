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

public class MainActivity extends Activity {

    private String dexpath = null;    //apk文件地址
    private File fileRelease = null;  //释放目录
    private DexClassLoader classLoader = null;

    private String apkName = "plugin1.apk";    //apk名称

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

        File extractFile = this.getFileStreamPath(apkName);
        dexpath = extractFile.getPath();

        fileRelease = getDir("dex", 0); //0 表示Context.MODE_PRIVATE

        classLoader = new DexClassLoader(dexpath,
                fileRelease.getAbsolutePath(), null, getClassLoader());

        Button btn_1 = (Button) findViewById(R.id.btn_1);
        Button btn_2 = (Button) findViewById(R.id.btn_2);
        Button btn_3 = (Button) findViewById(R.id.btn_3);
        Button btn_4 = (Button) findViewById(R.id.btnSanbo);

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
        //自定义测试
        btn_4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Class mLoadClassBean;
                try {
                    mLoadClassBean = classLoader.loadClass("cn.sanbo.WTF");
//                    Object beanObject = mLoadClassBean.newInstance();

                    Method init = mLoadClassBean.getMethod("init", Context.class, String.class);
                    init.setAccessible(true);
//                    String name = (String) getNameMethod.invoke(beanObject);
                    init.invoke(null, getApplicationContext(), "我是谁?");
                    log("init over");
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
    }

    private void log(String log) {
        Log.i("sanbo", log);
    }

    private void loge(Throwable e) {
        Log.e("sanbo", Log.getStackTraceString(e));
    }
}