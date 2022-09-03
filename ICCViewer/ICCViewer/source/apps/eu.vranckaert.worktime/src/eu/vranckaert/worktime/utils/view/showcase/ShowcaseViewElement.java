package eu.vranckaert.worktime.utils.view.showcase;

import android.app.Activity;
import android.view.View;
import com.github.espiandev.showcaseview.ShowcaseView;

/**
 * Date: 18/03/13
 * Time: 8:37
 *
 * @author Dirk Vranckaert
 */
public class ShowcaseViewElement {
    private View viewToShowcase;
    private int title;
    private int text;
    private boolean actionBarItem;
    private ShowcaseView.ConfigOptions options;

    private int actionBarElementId;
    private int actionBarType = ShowcaseView.ITEM_ACTION_ITEM;

    public ShowcaseViewElement(View viewToShowcase, int title, int text, ShowcaseView.ConfigOptions options) {
        this.viewToShowcase = viewToShowcase;
        this.title = title;
        this.text = text;
        this.options = options;
        this.actionBarItem = false;
    }

    public ShowcaseViewElement(int actionBarElementId, int title, int text, ShowcaseView.ConfigOptions options) {
        this.actionBarElementId = actionBarElementId;
        this.title = title;
        this.text = text;
        this.options = options;
        this.actionBarItem = true;
    }

    public View getViewToShowcase() {
        return viewToShowcase;
    }

    public void setViewToShowcase(View viewToShowcase) {
        this.viewToShowcase = viewToShowcase;
    }

    public int getTitle() {
        return title;
    }

    public void setTitle(int title) {
        this.title = title;
    }

    public int getText() {
        return text;
    }

    public void setText(int text) {
        this.text = text;
    }

    public boolean isActionBarItem() {
        return actionBarItem;
    }

    public void setActionBarItem(boolean actionBarItem) {
        this.actionBarItem = actionBarItem;
    }

    public ShowcaseView.ConfigOptions getOptions() {
        return options;
    }

    public void setOptions(ShowcaseView.ConfigOptions options) {
        this.options = options;
    }

    public int getActionBarType() {
        return actionBarType;
    }

    public void setActionBarType(int actionBarType) {
        this.actionBarType = actionBarType;
    }

    public ShowcaseView getShowcaseView(Activity activity) {
        if (actionBarItem) {
            return ShowcaseView.insertShowcaseViewWithType(actionBarType, actionBarElementId, activity, title, text, options);
        } else {
            return ShowcaseView.insertShowcaseView(viewToShowcase, activity, title, text, options);
        }
    }
}
