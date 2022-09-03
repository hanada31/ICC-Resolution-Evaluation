package ch.hgdev.toposuite.calculation.activities.polarsurvey;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import ch.hgdev.toposuite.App;
import ch.hgdev.toposuite.R;
import ch.hgdev.toposuite.utils.DisplayUtils;
import ch.hgdev.toposuite.utils.MathUtils;
import ch.hgdev.toposuite.utils.ViewUtils;

public class EditDeterminationDialogFragment extends DialogFragment {
    /**
     * The activity that creates an instance of EditDeterminationDialogFragment
     * must implement this interface in order to receive event callbacks. Each
     * method passes the DialogFragment in case the host needs to query it.
     * 
     * @author HGdev
     * 
     */
    public interface EditDeterminationDialogListener {
        /**
         * Define what to do when the "Cancel" button is clicked
         * 
         * @param dialog
         *            Dialog with NO useful information to fetch from.
         */
        void onDialogCancel(EditDeterminationDialogFragment dialog);

        /**
         * Define what to do when the "Edit" button is clicked.
         * 
         * @param dialog
         *            Dialog to fetch information from.
         */
        void onDialogEdit(EditDeterminationDialogFragment dialog);
    }

    EditDeterminationDialogListener listener;
    private String                  determinationNo;
    private double                  horizDir;
    private double                  distance;
    private double                  zenAngle;
    private double                  s;
    private double                  latDepl;
    private double                  lonDepl;

    private LinearLayout            layout;
    private ScrollView              scrollView;
    private EditText                determinationNoEditText;
    private EditText                horizDirEditText;
    private EditText                distanceEditText;
    private EditText                zenAngleEditText;
    private EditText                sEditText;
    private EditText                latDeplEditText;
    private EditText                lonDeplEditText;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        this.initAttributes();
        this.genAddDeterminationView();
        AlertDialog.Builder builder = new AlertDialog.Builder(this.getActivity());
        builder.setTitle(this.getActivity().getString(R.string.measure_edit))
                .setView(this.scrollView)
                .setPositiveButton(R.string.edit, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // overridden below because the dialog dismiss itself
                        // without a call to dialog.dismiss()...
                        // thus, it is impossible to handle error on user input
                        // without closing the dialog otherwise
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        EditDeterminationDialogFragment.this.listener
                                .onDialogCancel(EditDeterminationDialogFragment.this);
                    }
                });
        Dialog dialog = builder.create();
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(final DialogInterface dialog) {
                Button addButton = ((AlertDialog) dialog)
                        .getButton(DialogInterface.BUTTON_POSITIVE);
                addButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (EditDeterminationDialogFragment.this.checkDialogInputs()) {
                            if (EditDeterminationDialogFragment.this.zenAngleEditText.length() > 0) {
                                EditDeterminationDialogFragment.this.zenAngle = ViewUtils
                                        .readDouble(EditDeterminationDialogFragment.this.zenAngleEditText);
                            } else {
                                EditDeterminationDialogFragment.this.zenAngle = MathUtils.IGNORE_DOUBLE;
                            }
                            if (EditDeterminationDialogFragment.this.sEditText.length() > 0) {
                                EditDeterminationDialogFragment.this.s = ViewUtils
                                        .readDouble(EditDeterminationDialogFragment.this.sEditText);
                            } else {
                                EditDeterminationDialogFragment.this.s = MathUtils.IGNORE_DOUBLE;
                            }
                            if (EditDeterminationDialogFragment.this.latDeplEditText.length() > 0) {
                                EditDeterminationDialogFragment.this.latDepl = ViewUtils
                                        .readDouble(EditDeterminationDialogFragment.this.latDeplEditText);
                            } else {
                                EditDeterminationDialogFragment.this.latDepl = MathUtils.IGNORE_DOUBLE;
                            }
                            if (EditDeterminationDialogFragment.this.lonDeplEditText.length() > 0) {
                                EditDeterminationDialogFragment.this.lonDepl = ViewUtils
                                        .readDouble(EditDeterminationDialogFragment.this.lonDeplEditText);
                            } else {
                                EditDeterminationDialogFragment.this.lonDepl = MathUtils.IGNORE_DOUBLE;
                            }

                            EditDeterminationDialogFragment.this.determinationNo =
                                    EditDeterminationDialogFragment.this.determinationNoEditText
                                            .getText().toString();
                            EditDeterminationDialogFragment.this.horizDir = ViewUtils
                                    .readDouble(EditDeterminationDialogFragment.this.horizDirEditText);
                            EditDeterminationDialogFragment.this.distance = ViewUtils
                                    .readDouble(EditDeterminationDialogFragment.this.distanceEditText);
                            EditDeterminationDialogFragment.this.listener
                                    .onDialogEdit(EditDeterminationDialogFragment.this);
                            dialog.dismiss();
                        } else {
                            ViewUtils.showToast(
                                    EditDeterminationDialogFragment.this.getActivity(),
                                    EditDeterminationDialogFragment.this.getActivity().getString(
                                            R.string.error_fill_data));
                        }
                    }
                });
            }
        });
        return dialog;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            this.listener = (EditDeterminationDialogListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement EditDeterminationDialogListener");
        }
    }

    /**
     * Initializes class attributes.
     */
    private void initAttributes() {
        Bundle bundle = this.getArguments();

        this.determinationNo = bundle.getString(PolarSurveyActivity.DETERMINATION_NUMBER);
        this.horizDir = bundle.getDouble(PolarSurveyActivity.HORIZ_DIR);
        this.distance = bundle.getDouble(PolarSurveyActivity.DISTANCE);
        this.zenAngle = bundle.getDouble(PolarSurveyActivity.ZEN_ANGLE);
        this.s = bundle.getDouble(PolarSurveyActivity.S);
        this.latDepl = bundle.getDouble(PolarSurveyActivity.LAT_DEPL);
        this.lonDepl = bundle.getDouble(PolarSurveyActivity.LON_DEPL);

        this.layout = new LinearLayout(this.getActivity());
        this.layout.setOrientation(LinearLayout.VERTICAL);

        this.determinationNoEditText = new EditText(this.getActivity());
        this.determinationNoEditText.setHint(
                this.getActivity().getString(R.string.determination_sight_3dots));
        this.determinationNoEditText.setText(this.determinationNo);

        this.horizDirEditText = new EditText(this.getActivity());
        this.horizDirEditText.setHint(
                this.getActivity().getString(R.string.horiz_direction_3dots)
                        + this.getActivity().getString(R.string.unit_gradian));
        this.horizDirEditText.setText(DisplayUtils.toStringForEditText(this.horizDir));
        this.horizDirEditText.setInputType(App.getInputTypeCoordinate());

        this.distanceEditText = new EditText(this.getActivity());
        this.distanceEditText.setHint(this.getActivity().getString(
                R.string.distance_3dots)
                + this.getActivity().getString(R.string.unit_meter));
        this.distanceEditText.setText(DisplayUtils.toStringForEditText(this.distance));
        this.distanceEditText.setInputType(App.getInputTypeCoordinate());

        this.zenAngleEditText = new EditText(this.getActivity());
        this.zenAngleEditText.setHint(this.getActivity().getString(
                R.string.zenithal_angle_3dots)
                + this.getActivity().getString(R.string.unit_gradian)
                + this.getActivity().getString(R.string.optional_prths));
        this.zenAngleEditText.setText(DisplayUtils.toStringForEditText(this.zenAngle));
        this.zenAngleEditText.setInputType(App.getInputTypeCoordinate());

        this.sEditText = new EditText(this.getActivity());
        this.sEditText.setHint(this.getActivity().getString(
                R.string.prism_height_3dots)
                + this.getActivity().getString(R.string.unit_meter)
                + this.getActivity().getString(R.string.optional_prths));
        this.sEditText.setText(DisplayUtils.toStringForEditText(this.s));
        this.sEditText.setInputType(App.getInputTypeCoordinate());

        this.latDeplEditText = new EditText(this.getActivity());
        this.latDeplEditText.setHint(this.getActivity().getString(
                R.string.lateral_displacement_3dots)
                + this.getActivity().getString(R.string.unit_meter)
                + this.getActivity().getString(R.string.optional_prths));
        this.latDeplEditText.setText(DisplayUtils.toStringForEditText(this.latDepl));
        this.latDeplEditText.setInputType(App.getInputTypeCoordinate());

        this.lonDeplEditText = new EditText(this.getActivity());
        this.lonDeplEditText.setHint(this.getActivity().getString(
                R.string.longitudinal_displacement_3dots)
                + this.getActivity().getString(R.string.unit_meter)
                + this.getActivity().getString(R.string.optional_prths));
        this.lonDeplEditText.setText(DisplayUtils.toStringForEditText(this.lonDepl));
        this.lonDeplEditText.setInputType(App.getInputTypeCoordinate());
    }

    /**
     * Create a view to get information from the user.
     */
    private void genAddDeterminationView() {
        this.layout.addView(this.determinationNoEditText);
        this.layout.addView(this.horizDirEditText);
        this.layout.addView(this.distanceEditText);
        this.layout.addView(this.zenAngleEditText);
        this.layout.addView(this.sEditText);
        this.layout.addView(this.latDeplEditText);
        this.layout.addView(this.lonDeplEditText);

        this.scrollView = new ScrollView(this.getActivity());
        this.scrollView.addView(this.layout);
    }

    /**
     * Verify that the user has entered all required data.
     * 
     * @return True if every required data has been filled, false otherwise.
     */
    private boolean checkDialogInputs() {
        if ((this.determinationNoEditText.length() == 0) || (this.horizDirEditText.length() == 0)
                || (this.distanceEditText.length() == 0)) {
            return false;
        }
        return true;
    }

    public String getDeterminationNo() {
        return this.determinationNo;
    }

    public double getHorizDir() {
        return this.horizDir;
    }

    public double getDistance() {
        return this.distance;
    }

    public double getZenAngle() {
        return this.zenAngle;
    }

    public double getS() {
        return this.s;
    }

    public double getLatDepl() {
        return this.latDepl;
    }

    public double getLonDepl() {
        return this.lonDepl;
    }
}
