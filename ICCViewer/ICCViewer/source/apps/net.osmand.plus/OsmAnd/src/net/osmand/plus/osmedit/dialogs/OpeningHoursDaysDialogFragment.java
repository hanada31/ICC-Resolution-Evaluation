package net.osmand.plus.osmedit.dialogs;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.text.format.DateFormat;

import net.osmand.PlatformUtil;
import net.osmand.plus.R;
import net.osmand.plus.osmedit.BasicEditPoiFragment;
import net.osmand.util.OpeningHoursParser;

import org.apache.commons.logging.Log;

import java.util.Calendar;

public class OpeningHoursDaysDialogFragment extends DialogFragment {
	private static final Log LOG = PlatformUtil.getLog(OpeningHoursDaysDialogFragment.class);
	public static final String POSITION_TO_ADD = "position_to_add";
	public static final String ITEM = "item";

	@NonNull
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		final OpeningHoursParser.BasicOpeningHourRule item =
				(OpeningHoursParser.BasicOpeningHourRule) getArguments().getSerializable(ITEM);
		final int positionToAdd = getArguments().getInt(POSITION_TO_ADD);

		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

		final boolean createNew = positionToAdd == -1;
		Calendar inst = Calendar.getInstance();
		final int first = inst.getFirstDayOfWeek();
		final boolean[] dayToShow = new boolean[7];
		String[] daysToShow = new String[7];
		for (int i = 0; i < 7; i++) {
			int d = (first + i - 1) % 7 + 1;
			inst.set(Calendar.DAY_OF_WEEK, d);
			CharSequence dayName = DateFormat.format("EEEE", inst);
			String result = "" + Character.toUpperCase(dayName.charAt(0)) +
					dayName.subSequence(1, dayName.length());
			daysToShow[i] = result; //$NON-NLS-1$
			final int pos = (d + 5) % 7;
			dayToShow[i] = item.getDays()[pos];
		}
		builder.setTitle(getResources().getString(R.string.working_days));
		builder.setMultiChoiceItems(daysToShow, dayToShow, new DialogInterface.OnMultiChoiceClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which, boolean isChecked) {
				dayToShow[which] = isChecked;

			}

		});
		builder.setPositiveButton(createNew ? R.string.next_proceed
						: R.string.shared_string_save,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						boolean[] days = item.getDays();
						for (int i = 0; i < 7; i++) {
							days[(first + 5 + i) % 7] = dayToShow[i];
						}
						if (createNew) {
							OpeningHoursHoursDialogFragment.createInstance(item, positionToAdd, true, 0)
									.show(getFragmentManager(), "TimePickerDialogFragment");
						} else {
							((BasicEditPoiFragment) getParentFragment())
									.setBasicOpeningHoursRule(item, positionToAdd);
						}
					}

				});

		builder.setNegativeButton(getActivity().getString(R.string.shared_string_cancel), null);
		return builder.create();
	}

	public static OpeningHoursDaysDialogFragment createInstance(
			@NonNull final OpeningHoursParser.BasicOpeningHourRule item,
			final int positionToAdd) {
		LOG.debug("createInstance(" + "item=" + item + ", positionToAdd=" + positionToAdd + ")");
		OpeningHoursDaysDialogFragment daysDialogFragment = new OpeningHoursDaysDialogFragment();
		Bundle bundle = new Bundle();
		bundle.putSerializable(ITEM, item);
		bundle.putInt(POSITION_TO_ADD, positionToAdd);
		daysDialogFragment.setArguments(bundle);
		return daysDialogFragment;
	}
}