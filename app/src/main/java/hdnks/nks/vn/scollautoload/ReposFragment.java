package hdnks.nks.vn.scollautoload;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.mugen.Mugen;
import com.mugen.MugenCallbacks;
import com.mugen.attachers.BaseAttacher;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;

/**
 * Created by loc on 06/08/2015.
 */
public class ReposFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {
    // sự kiện onload cho các giá trị của nó
    interface OnLoadingListener {
        void onLoadingStarted();

        void onLoadingFinished();
    }

    //query android repos
    private String query = "android";
    private String language = "java";
    private String queryString = "%s+language:%s";
    final int DEFAULT_PER_PAGE = 10;

    //
    private BaseAttacher mBaseAttacher;
    // giá trị của một pages
    int currentPage = 1;
    int perPage = DEFAULT_PER_PAGE;
    boolean isLoading = false;


    // khởi tạọ một swper
    // các giá trị của swiper
    SwipeRefreshLayout mSwipeRefreshLayout;
    RecyclerView mRecyclerView;
    RepoAdapter mRepoAdapter;
    OnLoadingListener mListener;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof OnLoadingListener) {
            // khơi tạo onload
            mListener = (OnLoadingListener) activity;

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // khởi tạo các gía trị của slide
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        // gọi các giá trị con của nó
        mSwipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipe_container);
        mSwipeRefreshLayout.setOnRefreshListener(this);

        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerView);
        // khowit tạo một ie
        LinearLayoutManager manager = new LinearLayoutManager(getActivity(),
                LinearLayoutManager.VERTICAL,
                false);

        mRecyclerView.setHasFixedSize(true);
        // nhaanj casi gias tri cua layout
        mRecyclerView.setLayoutManager(manager);
        // set adater cho nos
        mRecyclerView.setAdapter(mRepoAdapter = new RepoAdapter());
        perPage = getPerPage(rootView.getContext());
        loadData(query, language, currentPage, perPage);
        return rootView;
    }

    /**
     * Get items to load per page onScroll.
     *
     * @param context {@link Context}
     * @return int of num of items that can be loaded onto the screen with scroll enabled
     */
    // lấy page khi nó  load buttom
    private int getPerPage(Context context) {
        //fixed item size in recyclerview. Adding 3 enables recyclerview scrolling.
        return (context.getResources().getDisplayMetrics().heightPixels
                / context.getResources().getDimensionPixelSize(R.dimen.repo_item_height)) + 3;
    }


    //  khi khởi tao vet
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // là một tư viện của android
        mBaseAttacher = Mugen.with(mRecyclerView, new MugenCallbacks() {
            @Override
            public void onLoadMore() {
                if (currentPage <= 5) {
                    loadData(query, language, currentPage + 1, perPage); // ne gia tri của cu ren page <5
                }
            }

            @Override
            public boolean isLoading() {
                return isLoading;
            }

            @Override
            public boolean hasLoadedAllItems() {
                return false;
            }
        }).start();

    }
    /* nơi dder laod dữ liệu cho bạn*/

    private void loadData(final String query, final String language, final int page, final int perPage) {
        new AsyncTask<Integer, Void, List<GitHubClient.Repo>>() {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                if (mListener != null) {
                    //to demo loading..
                    mListener.onLoadingStarted();
                }
            }


            // gọi lớp git hut clit
            @Override
            protected List<GitHubClient.Repo> doInBackground(Integer... params) {
                String q = String.format(Locale.ENGLISH,
                        queryString,
                        query,
                        language);
                isLoading = true;
                //  lây giá trị
                return GitHubClient.getClient()
                        .searchRepos(q,
                                GitHubClient.DEFAULT_SORT,
                                GitHubClient.DEFAULT_ORDER,
                                params[0],
                                perPage).repos;
            }

            @Override
            protected void onPostExecute(List<GitHubClient.Repo> repos) {
                isLoading = false;
                if (repos != null) {
                    // neys nhuw gia tri con cuar no khong rong thi
                    mRepoAdapter.onNext(repos, page);
                }
                // neeus nw lisstener ko rong thi ket thuc gia tri con cura nos
                if (mListener != null) {
                    //to demo loading finished..
                    mListener.onLoadingFinished();
                }

                currentPage = page;
                mSwipeRefreshLayout.setRefreshing(false);
            }
        }.execute(page);
    }

    // khi  refresh nó thì
    @Override
    public void onRefresh() {
        loadData(query, language, 1, perPage);
    }


    // datapter cura cac gia tri con cua no
    private static class RepoAdapter extends RecyclerView.Adapter<RepoHolder> {

        LinkedHashMap<Integer, List<GitHubClient.Repo>> repoMap;
        List<GitHubClient.Repo> repoList;

        public RepoAdapter() {
            repoMap = new LinkedHashMap<Integer, List<GitHubClient.Repo>>();
            repoList = new ArrayList<GitHubClient.Repo>();
        }

        @Override
        public RepoHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            View view = LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.item_repo, viewGroup, false);
            return new RepoHolder(view);
        }

        @Override
        public void onBindViewHolder(RepoHolder repoHolder, int i) {
            GitHubClient.Repo repo = getItem(i);
            if (repo == null) {
                return;
            }
            repoHolder.loadRepo(repo);
        }

        private GitHubClient.Repo getItem(int position) {
            int listSize = 0;

            if (repoList.size() > position) {
                return repoList.get(position);
            }

            repoList = new ArrayList<GitHubClient.Repo>();
            for (List<GitHubClient.Repo> list : repoMap.values()) {
                repoList.addAll(list);
                listSize = listSize + list.size();
                if (listSize > position) {
                    break;
                }
            }
            if (repoList.size() > 0) {
                return repoList.get(position);
            }
            return null;
        }

        @Override
        public int getItemCount() {
            int count = 0;
            for (List<GitHubClient.Repo> list : repoMap.values()) {
                count = count + list.size();
            }
            return count;
        }

        public void onNext(List<GitHubClient.Repo> repos, int page) {
            if (repos == null) {
                return;
            }
            repoMap.put(page, repos);
            notifyDataSetChanged();
        }
    }

    private static class RepoHolder extends RecyclerView.ViewHolder {

        ImageView imageAvatar;
        TextView textRepo;
        TextView textUser;
        TextView textStars;
        TextView textForks;

        public RepoHolder(View itemView) {
            super(itemView);
            // khơi tạo các gia trị con cho nó
            imageAvatar = (ImageView) itemView.findViewById(R.id.imageView_avatar);
            textRepo = (TextView) itemView.findViewById(R.id.textView_repo_name);
            textUser = (TextView) itemView.findViewById(R.id.textView_user_name);
            textStars = (TextView) itemView.findViewById(R.id.textView_stars);
            textForks = (TextView) itemView.findViewById(R.id.textView_forks);
            ((ImageView) itemView.findViewById(R.id.imageView_triangle)).
                    setColorFilter(itemView
                            .getContext()
                            .getResources()
                            .getColor(R.color.blue_light));
        }

        public void loadRepo(GitHubClient.Repo repo) {
            textRepo.setText(repo.name);
            textUser.setText(repo.owner.login);
            textStars.setText(repo.starsGazers + "");
            textForks.setText(repo.forks + "");

            String imgUrl = repo.owner.avatarUrl;
            if (imgUrl != null && !imgUrl.equals("")) {
                Picasso.with(imageAvatar.getContext())
                        .load(imgUrl)
                        .resize(200, 200)
                        .error(R.drawable.ic_github_placeholder)
                        .placeholder(R.drawable.ic_github_placeholder)
                        .centerCrop()
                        .into(imageAvatar);
            }
        }
    }


}
