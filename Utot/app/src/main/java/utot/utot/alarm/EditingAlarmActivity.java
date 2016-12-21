package utot.utot.alarm;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.facebook.FacebookSdk;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;

import org.json.JSONArray;
import org.json.JSONException;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

import io.realm.Realm;
import io.realm.RealmResults;
import utot.utot.R;
import utot.utot.customobjects.Alarm;
import utot.utot.helpers.Computations;
import utot.utot.helpers.DialogSize;


public class EditingAlarmActivity extends AppCompatActivity {
    private TextView timeSet;
    private Date alarmTime;
    private SimpleDateFormat fmt;
    private ToggleButton[] daysToggle;
    private ToggleButton everydayButton, weekendsButton, weekdaysButton;
    private Realm realm;
    private Alarm alarm;
    private CheckBox repeatingSwitch;
    private String ringtoneText;
    Calendar mcurrentTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editing_alarm);
        ringtoneText="";
        realm = Realm.getDefaultInstance();
        RealmResults<Alarm> alarms = realm.where(Alarm.class).findAll();
        alarm = alarms.get(getIntent().getIntExtra("POS", 0));
        fmt = new SimpleDateFormat("hh:mm a");

        mcurrentTime = Calendar.getInstance();
        mcurrentTime.setTime(alarm.getAlarmTime());
        final int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
        final int minute = mcurrentTime.get(Calendar.MINUTE);

        timeSet = (TextView) findViewById(R.id.time);
        timeSet.setText(fmt.format(alarm.getAlarmTime()));
        alarmTime = alarm.getAlarmTime();

        findViewById(R.id.timePicker).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TimePickerDialog mTimePicker = TimePickerDialog.newInstance(
                        new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePickerDialog timePicker, int selectedHour, int selectedMinute, int second) {
                                String time = selectedHour + ":" + selectedMinute;
                                fmt = new SimpleDateFormat("HH:mm");
                                try {
                                    alarmTime = fmt.parse(time);
                                } catch (ParseException e) {

                                    e.printStackTrace();
                                }
                                SimpleDateFormat fmt2 = new SimpleDateFormat("hh:mm a");

                                timeSet.setText(fmt2.format(alarmTime));
                            }
                        },
                        getHour(mcurrentTime),
                        getMinute(mcurrentTime),
                        false);
                mTimePicker.show(getFragmentManager(), "Timepickerdialog");
                mTimePicker.setTitle("Set Time");
            }
        });

        Typeface customFont = Typeface.createFromAsset(this.getAssets(), getResources().getString(R.string.toggle_butons_font));

        ImageButton ringtoneButton = (ImageButton) findViewById(R.id.ringtoneButton);
        final CheckBox vibrateSwitch = (CheckBox) findViewById(R.id.vibrateButton);
        repeatingSwitch = (CheckBox) findViewById(R.id.isRepeating);
        everydayButton = (ToggleButton) findViewById(R.id.everydayButton);
        weekendsButton = (ToggleButton) findViewById(R.id.weekendsButton);
        weekdaysButton = (ToggleButton) findViewById(R.id.weekdaysButton);


        daysToggle = new ToggleButton[7];
        daysToggle[0] = (ToggleButton) findViewById(R.id.mondButton);;
        daysToggle[1] = (ToggleButton) findViewById(R.id.tuesButton);
        daysToggle[2] = (ToggleButton) findViewById(R.id.wedButton);
        daysToggle[3] = (ToggleButton) findViewById(R.id.thursButton);
        daysToggle[4] = (ToggleButton) findViewById(R.id.friButton);
        daysToggle[5] = (ToggleButton) findViewById(R.id.satButton);
        daysToggle[6] = (ToggleButton) findViewById(R.id.sunButton);

        everydayButton.setTypeface(customFont);
        weekendsButton.setTypeface(customFont);
        weekdaysButton.setTypeface(customFont);
        for (int i = 0; i < daysToggle.length; i++) {
            daysToggle[i].setTypeface(customFont);
        }


        String alarmFrequency = alarm.getAlarmFrequency();
        if(!alarm.isRepeating()){
            repeatingSwitch.setChecked(false);
            setEnabledRepeatButtons(false);
        } else{
            repeatingSwitch.setChecked(true);
            setEnabledRepeatButtons(true);

        }

        boolean[] days = Computations.transformToBooleanArray(alarmFrequency.trim());
        for (int i = 0; i < days.length; i++) {
            daysToggle[i].setChecked(days[i]);
        }
        checkOtherToggles();
        ringtoneText = alarm.getAlarmAudio();

        ringtoneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                System.out.println("CHECK: ringtonetext "+ringtoneText + " alarm audio " + alarm.getAlarmAudio());
                RingtoneDialog dialog = new RingtoneDialog(EditingAlarmActivity.this, ringtoneText);
                dialog.show();
                dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        ringtoneText = RingtoneDialog.ringtoneName;
                        System.out.println("CHECK: " + RingtoneDialog.ringtoneName + " = "+ ringtoneText);
                    }
                });
                DialogSize.setSize(EditingAlarmActivity.this, dialog);
            }
        });

        vibrateSwitch.setChecked(alarm.isVibrate());
        repeatingSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                setEnabledRepeatButtons(b);
                checkOtherToggles();
            }
        });

        everydayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                for (int i = 0; i < daysToggle.length; i++) {
                    daysToggle[i].setChecked(everydayButton.isChecked());
                }
                weekdaysButton.setChecked(everydayButton.isChecked());
                weekendsButton.setChecked(everydayButton.isChecked());

            }
        });
        weekdaysButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                for (int i = 0; i <= 4; i++) {
                    daysToggle[i].setChecked(weekdaysButton.isChecked());
                }
                checkOtherToggles();

            }
        });

        weekendsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                daysToggle[5].setChecked(weekendsButton.isChecked());
                daysToggle[6].setChecked(weekendsButton.isChecked());
                checkOtherToggles();

            }
        });

        findViewById(R.id.cancelButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditingAlarmActivity.this.startActivity(new Intent(EditingAlarmActivity.this, TabbedAlarm.class));
                EditingAlarmActivity.this.finish();
            }
        });
        findViewById(R.id.saveAlarmButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String alarmDays = "";
//                int dayOfWeek = -1;
                boolean[] days = new boolean[7];

                for (int i = 0; i < daysToggle.length; i++) {
                    days[i] = daysToggle[i].isChecked();
                }
                boolean selectedDate = false;
                for (int i = 0; i < days.length; i++) {
                    if (days[i]) {
                        selectedDate = true;
                        break;
                    }

                }

                if(selectedDate){
                    alarmDays = (new JSONArray(Arrays.asList(days))).toString();

                    realm.beginTransaction();
                    alarm.setPrimaryKey((int)System.currentTimeMillis());
                    alarm.setAlarmFrequency(alarmDays);
                    alarm.setAlarmTime(alarmTime);
                    alarm.setIsOn(true);
                    alarm.setIsVibrate(vibrateSwitch.isChecked());
                    alarm.setRepeating(repeatingSwitch.isChecked());
                    alarm.setAlarmAudio(ringtoneText);
                    realm.commitTransaction();

                    Calendar now = Calendar.getInstance();

                    Computations.makeAlarm(EditingAlarmActivity.this, alarm, now);

                    EditingAlarmActivity.this.startActivity(new Intent(EditingAlarmActivity.this, TabbedAlarm.class));
                    EditingAlarmActivity.this.finish();

                } else{
                    Toast.makeText(EditingAlarmActivity.this, "Please select a day to set the alarm", Toast.LENGTH_SHORT).show();
                }


            }
        });

    }

    private void checkOtherToggles() {
        boolean everdayCheck = true;

        for (int i = 0; i < daysToggle.length; i++) {
            if (!daysToggle[i].isChecked()) {
                everdayCheck = false;
                break;
            }
        }
        if (everdayCheck) {
            everydayButton.setChecked(true);
            weekdaysButton.setChecked(true);
            weekendsButton.setChecked(true);
        } else {
            everydayButton.setChecked(false);
            boolean weekdayCheck = true;
            for (int i = 0; i <= 4; i++) {
                if (!daysToggle[i].isChecked()) {
                    weekdayCheck = false;
                    break;
                }
            }
            weekdaysButton.setChecked(weekdayCheck);

            if (daysToggle[5].isChecked() && daysToggle[6].isChecked())
                weekendsButton.setChecked(true);
            else
                weekendsButton.setChecked(false);

        }

    }

    public void weekNamesClick(View view){
        if(repeatingSwitch.isChecked()){
            checkOtherToggles();
        }
        else{
            for(ToggleButton otherdays: daysToggle){
                if(otherdays != view){
                    otherdays.setChecked(false);
                }
            }
        }
    }

    public void setEnabledRepeatButtons(boolean b){
        everydayButton.setEnabled(b);
        weekdaysButton.setEnabled(b);
        weekendsButton.setEnabled(b);
    }

    public int getHour(Calendar mcurrentTime){
        String time = timeSet.getText().toString();
        Date date = null;
        try {
            date = fmt.parse(time);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        mcurrentTime.setTime(date);
        return mcurrentTime.get(Calendar.HOUR_OF_DAY);
    }

    public int getMinute(Calendar mcurrentTime){
        String time = timeSet.getText().toString();
        Date date = null;
        try {
            date = fmt.parse(time);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        mcurrentTime.setTime(date);
        return mcurrentTime.get(Calendar.MINUTE);
    }

}
