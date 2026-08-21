package za.co.beeswins;

import android.app.Activity;
import android.app.Application;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.EditText;
import android.widget.TextView;

import java.util.Calendar;
import java.util.Locale;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * App-wide quality-of-life helpers for BeesWins.
 * Date fields are converted into tap-only calendar fields so dates do not need
 * to be typed manually at an auction.
 */
public class BeesWinsApp extends Application {
    private final Map<Activity, ViewTreeObserver.OnGlobalLayoutListener> layoutListeners = new WeakHashMap<>();
    private final Map<Activity, View> roots = new WeakHashMap<>();

    @Override
    public void onCreate() {
        super.onCreate();
        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override public void onActivityCreated(Activity activity, Bundle state) {}
            @Override public void onActivityStarted(Activity activity) {}
            @Override public void onActivityResumed(Activity activity) {
                installDateFieldWatcher(activity);
            }
            @Override public void onActivityPaused(Activity activity) {}
            @Override public void onActivityStopped(Activity activity) {}
            @Override public void onActivitySaveInstanceState(Activity activity, Bundle outState) {}
            @Override public void onActivityDestroyed(Activity activity) {
                View root = roots.remove(activity);
                ViewTreeObserver.OnGlobalLayoutListener listener = layoutListeners.remove(activity);
                if (root != null && listener != null && root.getViewTreeObserver().isAlive()) {
                    root.getViewTreeObserver().removeOnGlobalLayoutListener(listener);
                }
            }
        });
    }

    private void installDateFieldWatcher(Activity activity) {
        View root = activity.getWindow().getDecorView();
        wireDateFields(activity, root);

        if (layoutListeners.containsKey(activity)) return;

        ViewTreeObserver.OnGlobalLayoutListener listener = () -> wireDateFields(activity, root);
        roots.put(activity, root);
        layoutListeners.put(activity, listener);
        root.getViewTreeObserver().addOnGlobalLayoutListener(listener);
    }

    private void wireDateFields(Activity activity, View view) {
        if (view instanceof EditText) {
            EditText edit = (EditText) view;
            String label = dateLabel(edit);
            if (label != null && !Boolean.TRUE.equals(edit.getTag())) {
                edit.setTag(Boolean.TRUE);
                edit.setFocusable(false);
                edit.setFocusableInTouchMode(false);
                edit.setCursorVisible(false);
                edit.setLongClickable(false);
                edit.setInputType(InputType.TYPE_NULL);
                edit.setHint("Tik om datum te kies");
                edit.setCompoundDrawablesWithIntrinsicBounds(0, 0, android.R.drawable.ic_menu_my_calendar, 0);
                edit.setCompoundDrawablePadding(dp(edit, 8));
                edit.setContentDescription(label + ", kies datum");
                edit.setOnClickListener(v -> showDatePicker(activity, edit));
            }
        }

        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int i = 0; i < group.getChildCount(); i++) {
                wireDateFields(activity, group.getChildAt(i));
            }
        }
    }

    private String dateLabel(EditText edit) {
        CharSequence hint = edit.getHint();
        if (hint != null && hint.toString().toLowerCase(Locale.ROOT).contains("dd/mm")) {
            return labelFromParent(edit);
        }

        String label = labelFromParent(edit);
        if (label == null) return null;
        String lower = label.toLowerCase(Locale.ROOT);
        return (lower.contains("aankoopdatum") || lower.contains("verkoopdatum")) ? label : null;
    }

    private String labelFromParent(EditText edit) {
        if (!(edit.getParent() instanceof ViewGroup)) return null;
        ViewGroup parent = (ViewGroup) edit.getParent();
        int index = parent.indexOfChild(edit);
        if (index > 0) {
            View previous = parent.getChildAt(index - 1);
            if (previous instanceof TextView) {
                CharSequence text = ((TextView) previous).getText();
                if (text != null) return text.toString();
            }
        }
        return null;
    }

    private void showDatePicker(Activity activity, EditText target) {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        // Existing records open on their saved date; new records open on the current month.
        String current = target.getText() == null ? "" : target.getText().toString().trim();
        String[] parts = current.split("/");
        if (parts.length == 3) {
            try {
                int parsedDay = Integer.parseInt(parts[0]);
                int parsedMonth = Integer.parseInt(parts[1]) - 1;
                int parsedYear = Integer.parseInt(parts[2]);
                Calendar parsed = Calendar.getInstance();
                parsed.setLenient(false);
                parsed.set(parsedYear, parsedMonth, parsedDay);
                parsed.getTime();
                year = parsedYear;
                month = parsedMonth;
                day = parsedDay;
            } catch (Exception ignored) {
                // Keep today's date/current month if an old value cannot be parsed.
            }
        }

        DatePickerDialog dialog = new DatePickerDialog(
                activity,
                (picker, selectedYear, selectedMonth, selectedDay) -> target.setText(
                        String.format(Locale.US, "%02d/%02d/%04d", selectedDay, selectedMonth + 1, selectedYear)
                ),
                year,
                month,
                day
        );
        dialog.setTitle("Kies datum");
        dialog.show();
    }

    private int dp(View view, int value) {
        return (int) (value * view.getResources().getDisplayMetrics().density + 0.5f);
    }
}
