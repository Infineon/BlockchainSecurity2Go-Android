package co.coinfinity.infineonandroidapp.utils;

import android.app.Activity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import org.web3j.utils.Convert;

import java.math.BigDecimal;

public class UnitSpinnerUtils implements AdapterView.OnItemSelectedListener {

    private static final String[] units = {"wei", "kwei", "mwei", "gwei", "szabo", "finney", "ether", "kether", "mether", "gether"};
    private BigDecimal multiplier = new BigDecimal("1");

    public void addSpinnerAdapter(Activity activity, Spinner spinner) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(activity,
                android.R.layout.simple_spinner_item, units);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);

        spinner.setSelection(3);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        switch (position) {
            case 0:
                break;
            case 1:
                multiplier = Convert.toWei(multiplier, Convert.Unit.KWEI);
                break;
            case 2:
                multiplier = Convert.toWei(multiplier, Convert.Unit.MWEI);
                break;
            case 3:
                multiplier = Convert.toWei(multiplier, Convert.Unit.GWEI);
                break;
            case 4:
                multiplier = Convert.toWei(multiplier, Convert.Unit.SZABO);
                break;
            case 5:
                multiplier = Convert.toWei(multiplier, Convert.Unit.FINNEY);
                break;
            case 6:
                multiplier = Convert.toWei(multiplier, Convert.Unit.ETHER);
                break;
            case 7:
                multiplier = Convert.toWei(multiplier, Convert.Unit.KETHER);
                break;
            case 8:
                multiplier = Convert.toWei(multiplier, Convert.Unit.METHER);
                break;
            case 9:
                multiplier = Convert.toWei(multiplier, Convert.Unit.GETHER);
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    public BigDecimal getMultiplier() {
        return multiplier;
    }
}
