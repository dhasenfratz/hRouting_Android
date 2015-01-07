package ch.ethz.tik.hrouting;

import android.content.Context;
import android.content.res.TypedArray;
import android.preference.DialogPreference;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.NumberPicker;

import ch.ethz.tik.hrouting.providers.HistoryDbHelper;
import ch.ethz.tik.hrouting.util.HistoryDBContract.HistoryEntry;

/**
 * A {@link android.preference.Preference} that displays a number picker as a
 * dialog.
 */
public class NumberPickerPreference extends DialogPreference {

    public static final int MAX_VALUE = 99;
    public static final int MIN_VALUE = 1;

    private NumberPicker picker;
    private int value;

    public NumberPickerPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected View onCreateDialogView() {
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.gravity = Gravity.CENTER;

        picker = new NumberPicker(getContext());
        picker.setLayoutParams(layoutParams);

        FrameLayout dialogView = new FrameLayout(getContext());
        dialogView.addView(picker);

        return dialogView;
    }

    @Override
    protected void onBindDialogView(@NonNull View view) {
        super.onBindDialogView(view);
        picker.setMinValue(MIN_VALUE);
        picker.setMaxValue(MAX_VALUE);
        picker.setValue(getValue());
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        if (positiveResult) {
            setValue(picker.getValue());
            HistoryDbHelper dbHelper = new HistoryDbHelper(getContext());
            HistoryEntry.checkHistorySize(dbHelper, getContext());
        }
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getInt(index, MIN_VALUE);
    }

    @Override
    protected void onSetInitialValue(boolean restorePersistedValue,
                                     Object defaultValue) {
        if (restorePersistedValue) {
            try {
                String value = getPersistedString(Integer.toString(MIN_VALUE));
                setValue(Integer.parseInt(value));
            } catch (ClassCastException e) {
                setValue(getPersistedInt(MIN_VALUE));
            }
        } else {
            setValue((Integer) defaultValue);
        }
    }

    public void setValue(int value) {
        this.value = value;
        persistInt(this.value);
    }

    public int getValue() {
        return this.value;
    }
}
