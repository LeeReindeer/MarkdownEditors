/*
 * Copyright 2016. SHENQINCI(沈钦赐)<dev@qinc.me>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package moe.leer.markdowneditor.view;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.view.Menu;
import android.view.MenuItem;

import moe.leer.markdowneditor.AppContext;
import moe.leer.markdowneditor.AppManager;
import moe.leer.markdowneditor.R;
import moe.leer.markdowneditor.base.BaseDrawerLayoutActivity;
import moe.leer.markdowneditor.base.BaseFragment;
import moe.leer.markdowneditor.utils.Toast;

/**
 * The type Main activity.
 */
public class MainActivity extends BaseDrawerLayoutActivity {

  private final static int REQUEST_PERMISSION = 915;

  private BaseFragment mCurrentFragment;


  private int currentMenuId;
  private long customTime = 0;

  @Override
  protected void onDestroy() {
    super.onDestroy();
  }

  @Override
  public int getLayoutId() {
    return R.layout.activity_main;
  }

  //阴影的高度
  @Override
  protected float getElevation() {
    return 0;
  }

  @Override
  public void onCreateAfter(Bundle savedInstanceState) {
    if (savedInstanceState == null) {
      setDefaultFragment(R.id.content_fragment_container);
    }
    requestPermission();
    initUpdate(false);
  }

  private void setDefaultFragment(@IdRes int fragmentId) {
    mCurrentFragment = new FolderManagerFragment();
    getSupportFragmentManager()
        .beginTransaction()
        .replace(fragmentId, mCurrentFragment)
        .commit();
  }

  private void requestPermission() {
    if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
        PackageManager.PERMISSION_GRANTED) {

      ActivityCompat.requestPermissions(
          this,
          new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
          REQUEST_PERMISSION);
    }
  }

  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    if (requestCode == REQUEST_PERMISSION) {
      if (grantResults.length == 0
          || grantResults[0] == PackageManager.PERMISSION_DENIED) {
        android.widget.Toast.makeText(this, R.string.turn_on_permission, Toast.LENGTH_SHORT).show();
        String permission = permissions[0];
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
          boolean showRationale = shouldShowRequestPermissionRationale(permission);
          if (!showRationale) {
            // user checked never ask again
            // goto setting
            android.widget.Toast.makeText(this, R.string.turn_on_permission, Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.setData(Uri.fromParts("package", getPackageName(), null));
            startActivity(intent);
          } else {
            requestPermission();
          }
        } else {
          // close app
          AppManager.getAppManager().AppExit(this);
        }
      }
    }
  }

  @Override
  public void initData() {
  }

  @Override
  public boolean onNavigationItemSelected(MenuItem item) {
    int id = item.getItemId();
    if (id == R.id.menu_local) {//|| id == R.id.other
      if (id == currentMenuId) {
        return false;
      }
      currentMenuId = id;
      getDrawerLayout().closeDrawer(GravityCompat.START);
      return true;
    }

    if (onOptionsItemSelected(item)) {
      getDrawerLayout().closeDrawer(GravityCompat.START);
    }
    return false;
  }

  @Override
  protected int getDefaultMenuItemId() {
    currentMenuId = R.id.menu_local;
    return currentMenuId;
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    return super.onCreateOptionsMenu(menu);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case R.id.menu_helper:
        CommonMarkdownActivity.startHelper(this);
        return true;
      case R.id.menu_about:
        AboutActivity.startAboutActivity(this);
        return true;
//            case R.id.menu_setting:
//                return true;
      case R.id.menu_update:
        initUpdate(true);
        return true;
      case R.id.menu_nutstore:
        AppContext.showSnackbar(getWindow().getDecorView(), "敬请期待");
        return true;
    }
    return super.onOptionsItemSelected(item);// || mCurrentFragment.onOptionsItemSelected(item);
  }

  @Override
  public void onBackPressed() {//返回按钮
    if (getDrawerLayout().isDrawerOpen(GravityCompat.START)) {//侧滑菜单打开，关闭菜单
      getDrawerLayout().closeDrawer(GravityCompat.START);
      return;
    }

    if (mCurrentFragment != null && mCurrentFragment.onBackPressed()) {//如果Fragment有处理，则不据需执行
      return;
    }

    //没有东西可以返回了，剩下软件退出逻辑
    if (Math.abs(customTime - System.currentTimeMillis()) < 2000) {
      finish();
    } else {// 提示用户退出
      customTime = System.currentTimeMillis();
      Toast.showShort(mContext, "再按一次退出软件");
    }
  }

  private void initUpdate(boolean isShow) {
    if (isShow) {
      android.widget.Toast.makeText(application, "已经是最新版", Toast.LENGTH_SHORT).show();
    }
  }

    /*
    private void initUpdate(boolean isShow) {
        PgyUpdateManager.register(MainActivity.this,
                new UpdateManagerListener() {
                    @Override
                    public void onUpdateAvailable(final String result) {
                        final AppBean appBean = getAppBeanFromString(result);
                        if (appBean.getReleaseNote().startsWith("####")) {
//                            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this, R.style.DialogTheme);
//                            builder
//                                    .setTitle("当前版本已经停用了")
//                                    .setCancelable(false)
//                                    .setMessage("更新到最新版?")
//                                    .setNegativeButton("取消", (dialog, which) -> {
//                                       finish();
//                                    })
//                                    .setPositiveButton("确定", (dialog1, which) -> {
//                                        startDownloadTask(
//                                                MainActivity.this,
//                                                appBean.getDownloadURL());
//                                        dialog1.dismiss();
//                                    }).show();
                            //强制更新
                            startDownloadTask(
                                    MainActivity.this,
                                    appBean.getDownloadURL());
                        } else {
                            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this, R.style.DialogTheme);
                            builder
                                    .setTitle("更新")
                                    .setMessage(appBean.getReleaseNote() + "")
                                    .setNegativeButton("先不更新", (dialog, which) -> {
                                        dialog.dismiss();
                                    })
                                    .setPositiveButton("更新", (dialog1, which) -> {
                                        startDownloadTask(
                                                MainActivity.this,
                                                appBean.getDownloadURL());
                                        dialog1.dismiss();
                                    }).show();

                        }
                    }

                    @Override
                    public void onNoUpdateAvailable() {
                        if (isShow) {
                            android.widget.Toast.makeText(application, "已经是最新版", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
    */
}
