package com.maotong.getapk;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.lapism.searchview.SearchAdapter;
import com.lapism.searchview.SearchHistoryTable;
import com.lapism.searchview.SearchItem;
import com.lapism.searchview.SearchView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.File;
import java.text.Collator;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private SearchView mSearchView;
    private DrawerLayout mDrawerLayout;
    private FloatingActionButton mFab;
    private PackageManager mPackageManager;
    private List<AppInfo> mAppInfoList;
    private Handler mHandler;
    public final int GET_APK_FINISH = 9527;
    private FrameLayout mRootFrameLayout;
    public ProgressBar mProgressBar;
    private SearchHistoryTable mHistoryDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        EventBus.getDefault().register(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final ActionBar ab = getSupportActionBar();
        ab.setHomeAsUpIndicator(R.mipmap.ic_menu);
        ab.setDisplayHomeAsUpEnabled(true);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mAppInfoList = new ArrayList<AppInfo>();
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        if (navigationView != null) {
            setupDrawerContent(navigationView);
        }

        final ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
        if (viewPager != null) {
            setupViewPager(viewPager);
        }

        mFab = (FloatingActionButton) findViewById(R.id.fab);
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mHistoryDatabase.clearDatabase();
                Snackbar.make(view, "Search history deleted", Snackbar.LENGTH_LONG).setAction("Action", null).show();
            }
        });

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);

        mSearchView = (SearchView) findViewById(R.id.searchView);
        mSearchView.setVersion(SearchView.VERSION_MENU_ITEM);

        mPackageManager = getApplicationContext().getPackageManager();

        showProgressBar();
        new Thread(new Runnable() {
            @Override
            public void run() {
                mAppInfoList.clear();
                mAppInfoList.addAll(getAllAppInfo());
                mHandler.sendEmptyMessage(GET_APK_FINISH);
            }
        }).start();

        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (msg.what == GET_APK_FINISH) {
                    dismissProgressBar();
                    setupViewPager(viewPager);
                    setSearchView();
                }
            }
        };
    }

    @Subscribe
    public void onEvent(List<String> list){
    }

    private void setSearchView() {
        mHistoryDatabase = new SearchHistoryTable(this);
        if (mSearchView != null) {
            mSearchView.setVersion(SearchView.VERSION_MENU_ITEM);
            mSearchView.setVersionMargins(SearchView.VERSION_MARGINS_MENU_ITEM);
            mSearchView.setTheme(SearchView.THEME_LIGHT, true);
            mSearchView.setHint(R.string.search);
            mSearchView.setTextSize(16);
            mSearchView.setHint("Search");
            mSearchView.setDivider(false);
            mSearchView.setVoice(true);
            mSearchView.setVoiceText("Set permission on Android 6+ !");
            mSearchView.setAnimationDuration(SearchView.ANIMATION_DURATION);
            mSearchView.setShadowColor(ContextCompat.getColor(this, R.color.search_shadow_layout));
            mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    mSearchView.close(true);
                    getData(query, 0);
                    return true;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    return false;
                }
            });
            mSearchView.setOnOpenCloseListener(new SearchView.OnOpenCloseListener() {
                @Override
                public void onOpen() {
                    if (mFab != null) {
                        mFab.hide();
                    }
                }

                @Override
                public void onClose() {
                    if (mFab != null) {
                        mFab.show();
                    }
                }
            });


            final SearchAdapter searchAdapter = new SearchAdapter(this, getSuggestionsList());
            searchAdapter.setOnItemClickListener(new SearchAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(View view, int position) {
                    TextView textView = (TextView) view.findViewById(R.id.textView_item_text);
                    String query = textView.getText().toString();
                    mSearchView.close(true);
                    getData(query, position);
                }
            });
            mSearchView.setAdapter(searchAdapter);
        }
    }

    private void getData(String text, int position) {
        mHistoryDatabase.addItem(new SearchItem(text));

        for (int i = 0; i < mAppInfoList.size(); ++i) {
            if (text.equals(mAppInfoList.get(i).getAppName().toLowerCase())) {
                //操作fragment的方法
                EventBus.getDefault().post(new PositionEvent(i));
            }
        }

/*      Intent intent = new Intent(getApplicationContext(), ResultActivity.class);
        intent.putExtra(EXTRA_KEY_VERSION, SearchView.VERSION_TOOLBAR_ICON);
        intent.putExtra(EXTRA_KEY_VERSION_MARGINS, SearchView.VERSION_MARGINS_TOOLBAR_SMALL);
        intent.putExtra(EXTRA_KEY_THEME, SearchView.THEME_LIGHT);
        intent.putExtra(EXTRA_KEY_TEXT, text);
        startActivity(intent);*/
    }

    private List<SearchItem> getSuggestionsList() {
        List<SearchItem> suggestionsList = new ArrayList<>();
        for (AppInfo appInfo : mAppInfoList) {
            suggestionsList.add(new SearchItem(appInfo.getAppName()));
        }
        return suggestionsList;
    }

    private void setupViewPager(ViewPager viewPager) {
        Adapter adapter = new Adapter(getSupportFragmentManager());
        adapter.addFragment(AppListFragment.newInstance(mAppInfoList), "Installed");
        viewPager.setAdapter(adapter);
    }

    private void setupDrawerContent(NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        menuItem.setChecked(true);
                        mDrawerLayout.closeDrawers();
                        return true;
                    }
                });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.sample_actions, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_search:
                mSearchView.open(true);
                return true;
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
            case R.id.menu_night_mode_system:
                setNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                break;
            case R.id.menu_night_mode_day:
                setNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            case R.id.menu_night_mode_night:
                setNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
            case R.id.menu_night_mode_auto:
                setNightMode(AppCompatDelegate.MODE_NIGHT_AUTO);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        switch (AppCompatDelegate.getDefaultNightMode()) {
            case AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM:
                menu.findItem(R.id.menu_night_mode_system).setChecked(true);
                break;
            case AppCompatDelegate.MODE_NIGHT_AUTO:
                menu.findItem(R.id.menu_night_mode_auto).setChecked(true);
                break;
            case AppCompatDelegate.MODE_NIGHT_YES:
                menu.findItem(R.id.menu_night_mode_night).setChecked(true);
                break;
            case AppCompatDelegate.MODE_NIGHT_NO:
                menu.findItem(R.id.menu_night_mode_day).setChecked(true);
                break;
        }
        return true;
    }

    private void setNightMode(@AppCompatDelegate.NightMode int nightMode) {
        AppCompatDelegate.setDefaultNightMode(nightMode);

        if (Build.VERSION.SDK_INT >= 11) {
            recreate();
        }
    }

    static class Adapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragments = new ArrayList<>();
        private final List<String> mFragmentTitles = new ArrayList<>();

        public Adapter(FragmentManager fm) {
            super(fm);
        }

        public void addFragment(Fragment fragment, String title) {
            mFragments.add(fragment);
            mFragmentTitles.add(title);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragments.get(position);
        }

        @Override
        public int getCount() {
            return mFragments.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitles.get(position);
        }
    }

    /**
     * 获取已经安装的应用
     */
    private List<AppInfo> getAllAppInfo() {
        File apkFile;
        AppInfo appInfo;
        mAppInfoList = new ArrayList<AppInfo>();
        List<AppInfo> appInfoList = new ArrayList<>();

        List<String> appNameArrList = new ArrayList<>();

        //获取已安装所有应用对应的PackageInfo
        List<PackageInfo> packageInfoList = mPackageManager.getInstalledPackages(0);
        for (int i = 0; i < packageInfoList.size(); i++) {
            appInfo = new AppInfo();
            PackageInfo packageInfo = packageInfoList.get(i);
            //获取应用名称
            appInfo.setAppName(getApplicationName(packageInfo.packageName, mPackageManager));
            appNameArrList.add(appInfo.getAppName());
            //获取应用ICON
            appInfo.setAppIcon(packageInfo.applicationInfo.loadIcon(mPackageManager));
            //获取应用的apk文件
            apkFile = new File(packageInfo.applicationInfo.sourceDir);
            appInfo.setApkFile(apkFile);
            //获取到应用大小
            appInfo.setAppSize(((float) apkFile.length() / 1024 / 1024) + "MB");
            //获取应用的更新时间
            appInfo.setAppTime(getDate(packageInfo.lastUpdateTime));
            appInfoList.add(appInfo);
        }

        Collator collator = Collator.getInstance(Locale.CHINA);
        Collections.sort(appNameArrList, collator);

        for (String appName : appNameArrList) {
            for (AppInfo appInfo1 : appInfoList) {
                if (appName.equals(appInfo1.getAppName())) {
                    mAppInfoList.add(appInfo1);
                }
            }
        }

        return mAppInfoList;
    }


    /**
     * 获取应用的Icon
     */
    public Drawable getAppliactionIcon(PackageInfo packageInfo, PackageManager packageManager) {
        Drawable appliactionIcon = packageInfo.applicationInfo.loadIcon(packageManager);
        return appliactionIcon;
    }


    /**
     * 获取应用的名称
     */
    public String getApplicationName(String packageName, PackageManager packageManager) {
        String applicationName = null;
        try {
            ApplicationInfo applicationInfo = packageManager.getApplicationInfo(packageName, 0);
            applicationName = (String) packageManager.getApplicationLabel(applicationInfo);
        } catch (PackageManager.NameNotFoundException e) {

        }
        return applicationName;
    }

    /**
     * 生成时间
     */
    public static String getDate(long time) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date(time);
        String formatedDate = simpleDateFormat.format(date);
        return formatedDate;
    }

    /**
     * 在屏幕中间显示风火轮
     */
    private void showProgressBar() {
        mRootFrameLayout = (FrameLayout) findViewById(android.R.id.content);
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams
                (FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.gravity = Gravity.CENTER;
        mProgressBar = new ProgressBar(this);
        mProgressBar.setLayoutParams(layoutParams);
        mProgressBar.setVisibility(View.VISIBLE);
        mRootFrameLayout.addView(mProgressBar);
    }

    /**
     * 隐藏风火轮
     */
    private void dismissProgressBar() {
        if (null != mProgressBar && null != mRootFrameLayout) {
            mRootFrameLayout.removeView(mProgressBar);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
}
