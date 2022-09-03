/*
 * Copyright (C) 2016  Tobias Bielefeld
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * If you want to contact me, send me an e-mail at tobias.bielefeld@gmail.com
 */

package de.tobiasbielefeld.solitaire.ui.statistics;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

import de.tobiasbielefeld.solitaire.R;
import de.tobiasbielefeld.solitaire.helper.Scores;

import static de.tobiasbielefeld.solitaire.SharedData.currentGame;
import static de.tobiasbielefeld.solitaire.SharedData.scores;

/**
 * Shows the recent scores of the current game
 */

public class RecentScoresFragment extends Fragment{

    private String dollar;

    /**
     * Loads the high score list
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_statistics_tab3, container, false);

        //if the app got killed while the statistics are open and then the user restarts the app,
        //my helper classes aren't initialized so they can't be used. In this case, simply
        //close the statistics
        try {
            loadData();
        } catch (NullPointerException e) {
            getActivity().finish();
            return view;
        }

        TableLayout tableLayout = (TableLayout) view.findViewById(R.id.statisticsTableHighScores);
        TextView textNoEntries = (TextView) view.findViewById(R.id.statisticsTextNoEntries);

        if (scores.getRecentScore(0, 2) != 0) {
            textNoEntries.setVisibility(View.GONE);
        }

        for (int i = 0; i < Scores.MAX_SAVED_SCORES; i++) {                                         //for each entry in highScores, add a new view with it
            if (scores.getRecentScore(i, 2) == 0) {                                                //if the score is zero, don't show it
                continue;
            }

            TableRow row = (TableRow) LayoutInflater.from(getContext()).inflate(R.layout.statistics_scores_row, null);

            TextView textView1 = (TextView) row.findViewById(R.id.row_cell_1);
            TextView textView2 = (TextView) row.findViewById(R.id.row_cell_2);
            TextView textView3 = (TextView) row.findViewById(R.id.row_cell_3);
            TextView textView4 = (TextView) row.findViewById(R.id.row_cell_4);

            textView1.setText(String.format(Locale.getDefault(), "%s %s", scores.getRecentScore(i, 0),dollar));
            long time = scores.getRecentScore(i, 1);
            textView2.setText(String.format(Locale.getDefault(), "%02d:%02d:%02d",time / 3600, (time % 3600) / 60, (time % 60)));
            textView3.setText(DateFormat.getDateInstance(DateFormat.SHORT).format(scores.getRecentScore(i, 2)));
            textView4.setText(new SimpleDateFormat("HH:mm", Locale.getDefault()).format(scores.getRecentScore(i, 2)));

            tableLayout.addView(row);
        }

        return view;
    }

    /**
     * loads the other shown data
     */
    private void loadData() {
        dollar = currentGame.isPointsInDollar() ? "$" : "";
    }
}