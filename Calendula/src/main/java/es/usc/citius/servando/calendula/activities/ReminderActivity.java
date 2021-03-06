package es.usc.citius.servando.calendula.activities;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.util.List;

import es.usc.citius.servando.calendula.R;
import es.usc.citius.servando.calendula.persistence.DailyScheduleItem;
import es.usc.citius.servando.calendula.persistence.Medicine;
import es.usc.citius.servando.calendula.persistence.Routine;
import es.usc.citius.servando.calendula.persistence.ScheduleItem;
import es.usc.citius.servando.calendula.scheduling.AlarmScheduler;
import es.usc.citius.servando.calendula.scheduling.ScheduleUtils;

public class ReminderActivity extends FragmentActivity {

    public static final String TAG = ReminderActivity.class.getName();

    LinearLayout list;

    Button delayButton = null;
    Button doneButton = null;
    List<ScheduleItem> doses;
    Spinner delaySpinner;
    Long routineId;
    Routine routine;
    boolean totalChecked = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        getActionBar().hide();
        setContentView(R.layout.activity_reminder);

        list = (LinearLayout) findViewById(R.id.reminder_list);
        delayButton = (Button) findViewById(R.id.reminder_delay_button);
        doneButton = (Button) findViewById(R.id.reminder_done_button);

        routineId = getIntent().getLongExtra("routine_id", -1);

        Log.d(TAG, "Reminder received routine_id " + routine);

        if (routineId != -1) {

            routine = Routine.findById(routineId);
            doses = ScheduleUtils.getRoutineScheduleItems(routine, true);

            setupScheduleSpinner();

            delayButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    delaySpinner.performClick();
                }
            });

            doneButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // cancel reminder notification
                    ReminderNotification.cancel(getApplicationContext());
                    finish();
                }
            });

            fillReminderList();
        } else {
            Toast.makeText(this, "Error: " + routineId, Toast.LENGTH_SHORT).show();
            finish();
        }
        //opening transition animations
        overridePendingTransition(R.anim.activity_open_scale, 0);
    }


    private void setupScheduleSpinner() {
        delaySpinner = (Spinner) findViewById(R.id.delays_spinner);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.delays_array, R.layout.delay_spinner);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        // Apply the adapter to the spinner
        delaySpinner.setAdapter(adapter);
        // Set selection listener
        delaySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                String selected = (String) adapterView.getItemAtPosition(i);
                Log.d(TAG, "Item selected (" + i + "), " + selected);
                // prevent automatic delay when the spinner is initialized
                if (i != 0) {

                    selected = selected.replace("min", "").trim();
                    int minutes = Integer.valueOf(selected);
                    delay(minutes);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                // do nothing
            }

        });
    }


    void delay(int minutes) {
        ReminderNotification.cancel(getApplicationContext());
        AlarmScheduler.instance().onDelayRoutine(routine, getApplicationContext(), minutes * 60 * 1000);
        Toast.makeText(getApplicationContext(), "Reminder delayed " + minutes + " mins", Toast.LENGTH_SHORT).show();
        finish();
    }

    public String getDisplayableDose(int dose, Medicine m, Routine r) {
        return dose
                + " "
                + m.presentation().units(getResources())
                + " - "
                + r.time().toString("kk:mm")
                + "h";
    }


    void fillReminderList() {

        LayoutInflater inflater = getLayoutInflater();

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );

        params.setMargins(0, 0, 0, 15);

        for (ScheduleItem scheduleItem : doses) {
            final Routine r = scheduleItem.routine();
            final Medicine med = scheduleItem.schedule().medicine();
            final DailyScheduleItem dsi = DailyScheduleItem.findByScheduleItem(scheduleItem);
            final View entry = inflater.inflate(R.layout.reminder_item, null);
            final View background = entry.findViewById(R.id.reminder_item_container);
            final ToggleButton checkButton = (ToggleButton) entry.findViewById(R.id.check_button);

            ((TextView) entry.findViewById(R.id.med_item_name)).setText(med.name());
            ((TextView) entry.findViewById(R.id.med_item_dose)).setText(getDisplayableDose((int) scheduleItem.dose(), med, r));

            entry.setTag(dsi);

            checkButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                    background.setSelected(checked);
                    DailyScheduleItem dailyScheduleItem = (DailyScheduleItem) entry.getTag();
                    dailyScheduleItem.setTakenToday(true);
                    dailyScheduleItem.save();
                    onReminderChecked();
                }
            });

            if (dsi.takenToday()) {
                checkButton.setChecked(true);
                background.setSelected(true);
            }

            list.addView(entry, params);
        }

    }

    private void onReminderChecked() {
        int total = doses.size();
        int checked = 0;

        for (ScheduleItem s : doses) {
            boolean taken = DailyScheduleItem.findByScheduleItem(s).takenToday();
            if (taken)
                checked++;
        }
        if (checked == total) {
            delayButton.setVisibility(View.INVISIBLE);
            doneButton.getBackground().setLevel(1);
            totalChecked = true;
        } else {
            delayButton.setVisibility(View.VISIBLE);
            doneButton.getBackground().setLevel(0);
            totalChecked = false;
        }
        Log.d(TAG, "Checked " + checked + " meds. Total:" + total);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.reminder, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


}
