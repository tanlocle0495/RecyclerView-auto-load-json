package hdnks.nks.vn.scollautoload;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

public class MainActivity extends AppCompatActivity implements ReposFragment.OnLoadingListener {
    private ProgressBar mProgressBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // nơi replace frament vô ứng dụng của bạn
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new ReposFragment())
                    .commit();
        }
    }
    @Override
    public void onLoadingStarted() {
        mProgressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void onLoadingFinished() {
        mProgressBar.setVisibility(View.GONE);
    }


}
