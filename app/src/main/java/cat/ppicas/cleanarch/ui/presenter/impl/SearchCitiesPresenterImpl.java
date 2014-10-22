package cat.ppicas.cleanarch.ui.presenter.impl;

import android.os.Bundle;
import android.text.TextUtils;

import java.util.List;

import cat.ppicas.cleanarch.domain.City;
import cat.ppicas.cleanarch.repository.CityRepository;
import cat.ppicas.cleanarch.task.FindCityTask;
import cat.ppicas.cleanarch.ui.presenter.SearchCitiesPresenter;
import cat.ppicas.cleanarch.ui.view.SearchCitiesView;
import cat.ppicas.cleanarch.util.ShowErrorTaskCallback;
import cat.ppicas.cleanarch.util.TaskExecutor;

public class SearchCitiesPresenterImpl extends AbstractPresenter<SearchCitiesView>
        implements SearchCitiesPresenter {

    private static final String STATE_LAST_SEARCH = "lastSearch";

    private TaskExecutor mTaskExecutor;
    private CityRepository mCityRepository;
    private FindCityTask mFindCityTask;

    private String mLastSearch;
    private List<City> mLastResults;

    public SearchCitiesPresenterImpl(TaskExecutor taskExecutor, CityRepository cityRepository) {
        mTaskExecutor = taskExecutor;
        mCityRepository = cityRepository;
    }

    @Override
    public void bindView(SearchCitiesView view) {
        super.bindView(view);

        if (mLastResults != null) {
            view.showCities(mLastResults);
        } else if (!TextUtils.isEmpty(mLastSearch)) {
            if (mFindCityTask == null || !mTaskExecutor.isRunning(mFindCityTask)) {
                onCitySearch(mLastSearch);
            }
        }
    }

    @Override
    public void onCitySearch(String cityName) {
        getView().showProgress(true);
        mLastSearch = cityName;

        if (mFindCityTask != null && mTaskExecutor.isRunning(mFindCityTask)) {
            mFindCityTask.cancel();
        }
        mFindCityTask = new FindCityTask(cityName, mCityRepository);
        mTaskExecutor.execute(mFindCityTask, new ShowErrorTaskCallback(this) {
            @Override
            public void onSuccess(List<City> result) {
                if (getView() != null) {
                    getView().showProgress(false);
                    if (result.isEmpty()) {
                        getView().showCitiesNotFound();
                    } else {
                        getView().showCities(result);
                    }
                }
                mLastResults = result;
            }
        });
    }

    @Override
    public void saveState(Bundle state) {
        state.putString(STATE_LAST_SEARCH, mLastSearch);
    }

    @Override
    public void restoreState(Bundle state) {
        mLastSearch = state.getString(STATE_LAST_SEARCH);
    }
}