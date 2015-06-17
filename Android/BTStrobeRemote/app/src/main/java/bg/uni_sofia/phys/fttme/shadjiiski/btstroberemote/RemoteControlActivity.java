package bg.uni_sofia.phys.fttme.shadjiiski.btstroberemote;

import android.content.SharedPreferences;
import android.os.PersistableBundle;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import bg.uni_sofia.phys.fttme.shadjiiski.btstroberemote.connection.BTConnectionHandler;
import bg.uni_sofia.phys.fttme.shadjiiski.btstroberemote.connection.BTStrobeConnectionHandler;
import bg.uni_sofia.phys.fttme.shadjiiski.btstroberemote.connection.ConnectionState;
import bg.uni_sofia.phys.fttme.shadjiiski.btstroberemote.connection.ConnectionStateListener;


public class RemoteControlActivity extends ActionBarActivity implements SeekBar.OnSeekBarChangeListener, ConnectionStateListener {
    private static final String SAVED_STATE_FREQUENCY = "bg.uni_sofia.phys.fttme.shadjiiski.btstroberemote.SAVED_STATE_FREQUENCY";
    private static final String SAVED_STATE_DUTY = "bg.uni_sofia.phys.fttme.shadjiiski.btstroberemote.SAVED_STATE_DUTY";
    private static final String PREFERENCES = "bg.uni_sofia.phys.fttme.shadjiiski.btstroberemote.PREFERENCES";
    private static final String PREF_FREQ_PROGRESS = "frequency_progress";
    private static final String PREF_DUTY_PROGRESS = "duty_progress";

    private static final int FREQUENCY_SLIDER_MAX_VALUE = 990;
    private static final int DUTY_SLIDER_MAX_VALUE = 98;
    private static final double MIN_FREQUENCY = 1.0; //Hz
    private static final double MAX_FREQUENCY = 100.0; //Hz
    private static final int MIN_DUTY = 1; //percent
    private static final int MAX_DUTY = 99; //percent
    private static final double DEFAULT_FREQUENCY = 30.0; //Hz
    private static final int DEFAULT_DUTY = 5; //percent

    private int frequencyProgress;
    private int dutyProgress;
    private boolean btnsInputEnabled; //fine control buttons input is disabled while dragging seekers
    private SeekBar freqSlider, dutySlider;
    private TextView freqText, dutyText;

    private SharedPreferences preferences;

    private BTStrobeConnectionHandler connectionHandler;

    private boolean validFrequencySliderValue(int frequency){
        return (frequency >= 0) && (frequency <= FREQUENCY_SLIDER_MAX_VALUE);
    }

    private boolean validFrequencyValue(double frequency){
        return (frequency >= MIN_FREQUENCY) && (frequency <= MAX_FREQUENCY);
    }

    private boolean validDutySliderValue(int duty){
        return (duty >= 0) && (duty <= DUTY_SLIDER_MAX_VALUE);
    }

    private boolean validDutyValue(int duty){
        return (duty >= MIN_DUTY) && (duty <= MAX_DUTY);
    }

    private int frequencyToProgress(double frequency){
        return (int) (10 * frequency) - 10;
    }

    private double progressToFrequency(int progress){
        return 0.1 * (progress + 10);
    }

    private int dutyToProgress(int duty){
        return duty - 1;
    }

    private int progressToDuty(int progress){
        return progress + 1;
    }

    private void setAndSendParameters(){
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(PREF_FREQ_PROGRESS, frequencyProgress);
        editor.putInt(PREF_DUTY_PROGRESS, dutyProgress);
        editor.commit();
        connectionHandler.setStrobeParameters(progressToFrequency(frequencyProgress), progressToDuty(dutyProgress));
    }

    private void updateFrequencyText(int progress){
        int freq = (int)(0.5 + 10 * progressToFrequency(progress));
        freqText.setText(freq / 10 + "." + freq % 10 + " Hz");
    }

    private void updateDutyText(int progress){
        dutyText.setText(progressToDuty(progress) + " %");
    }

    public void minusOneHertz(View view){
        if(btnsInputEnabled && validFrequencySliderValue(frequencyProgress - 10)){
            frequencyProgress -= 10;
            freqSlider.setProgress(frequencyProgress);
            updateFrequencyText(frequencyProgress);
            setAndSendParameters();
        }
    }
    public void minusPointOneHertz(View view){
        if(btnsInputEnabled && validFrequencySliderValue(frequencyProgress - 1)){
            frequencyProgress -= 1;
            freqSlider.setProgress(frequencyProgress);
            updateFrequencyText(frequencyProgress);
            setAndSendParameters();
        }
    }
    public void plusOneHertz(View view){
        if(btnsInputEnabled && validFrequencySliderValue(frequencyProgress + 10)){
            frequencyProgress += 10;
            freqSlider.setProgress(frequencyProgress);
            updateFrequencyText(frequencyProgress);
            setAndSendParameters();
        }
    }
    public void plusPointOneHertz(View view){
        if(btnsInputEnabled && validFrequencySliderValue(frequencyProgress + 1)){
            frequencyProgress += 1;
            freqSlider.setProgress(frequencyProgress);
            updateFrequencyText(frequencyProgress);
            setAndSendParameters();
        }
    }
    public void minusOnePercent(View view){
        if(btnsInputEnabled && validDutySliderValue(dutyProgress - 1)){
            dutyProgress -= 1;
            dutySlider.setProgress(dutyProgress);
            updateDutyText(dutyProgress);
            setAndSendParameters();
        }
    }
    public void plusOnePercent(View view){
        if(btnsInputEnabled && validDutySliderValue(dutyProgress + 1)){
            dutyProgress += 1;
            dutySlider.setProgress(dutyProgress);
            updateDutyText(dutyProgress);
            setAndSendParameters();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        outState.putInt(SAVED_STATE_FREQUENCY, frequencyProgress);
        outState.putInt(SAVED_STATE_DUTY, dutyProgress);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(savedInstanceState != null){
            frequencyProgress = savedInstanceState.getInt(SAVED_STATE_FREQUENCY);
            dutyProgress = savedInstanceState.getInt(SAVED_STATE_DUTY);
        }
        else{
            preferences = getSharedPreferences(PREF_FREQ_PROGRESS, MODE_PRIVATE);
            frequencyProgress = preferences.getInt(PREF_FREQ_PROGRESS, frequencyToProgress(DEFAULT_FREQUENCY));
            dutyProgress = preferences.getInt(PREF_DUTY_PROGRESS, dutyToProgress(DEFAULT_DUTY));
        }
        setContentView(R.layout.activity_remote_control);

        dutyText = (TextView)findViewById(R.id.text_duty);
        freqText = (TextView)findViewById(R.id.text_frequency);
        freqSlider = (SeekBar)findViewById(R.id.freq_slider);
        dutySlider = (SeekBar)findViewById(R.id.duty_slider);
        freqSlider.setOnSeekBarChangeListener(this);
        dutySlider.setOnSeekBarChangeListener(this);
        btnsInputEnabled = true;
        freqSlider.setProgress(frequencyProgress);
        dutySlider.setProgress(dutyProgress);
        updateFrequencyText(frequencyProgress);
        updateDutyText(dutyProgress);
    }

    @Override
    protected void onResume() {
        super.onResume();
        connectionHandler = new BTStrobeConnectionHandler(this, false);
        connectionHandler.addConnectionStateListener(this);
        connectionHandler.connect();
    }

    @Override
    protected void onPause() {
        super.onPause();
        connectionHandler.disconnect();
        connectionHandler.removeConnectionStateListener(this);
        connectionHandler = null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_remote_control, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if(seekBar.getId() == R.id.freq_slider)
            updateFrequencyText(progress);
        else if(seekBar.getId() == R.id.duty_slider)
            updateDutyText(progress);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        btnsInputEnabled = false; //disable fine control while dragging
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        if(seekBar.getId() == R.id.freq_slider)
            frequencyProgress = seekBar.getProgress();
        else if(seekBar.getId() == R.id.duty_slider)
            dutyProgress = seekBar.getProgress();
        btnsInputEnabled = true; //re-enable fine control
        setAndSendParameters();
    }

    @Override
    public void connectionStateChanged(BTConnectionHandler source, ConnectionState oldState, ConnectionState newState) {
        if(newState == ConnectionState.CONNECTED)
           setAndSendParameters();
    }
}
