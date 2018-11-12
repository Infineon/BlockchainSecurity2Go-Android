package co.coinfinity.infineonandroidapp.adapter;

import android.content.Context;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import org.web3j.utils.Convert;

import java.math.BigDecimal;

/**
 * Adapter class used to calculate gas price depending on selected Ethereum unit.
 */
public class UnitSpinnerAdapter implements AdapterView.OnItemSelectedListener {

    private static final String[] units = {"wei", "kwei", "mwei", "gwei", "szabo", "finney", "ether", "kether", "mether", "gether"};
    private BigDecimal multiplier = new BigDecimal("1");

    /**
     * Needs to be called at the beginning to add this adapter to the spinner.
     *
     * @param context
     * @param spinner
     */
    public void addSpinnerAdapter(Context context, Spinner spinner) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(context,
                android.R.layout.simple_spinner_item, units);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);

        spinner.setSelection(3);
    }

    /**
     * Will be called when an item of the spinner is selected.
     *
     * @param parent
     * @param view
     * @param position
     * @param id
     */
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        multiplier = new BigDecimal("1");
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

    /**
     * Get the value that will be needed to calculate wei price.
     *
     * @return multiplier for wei price calculation
     */
    public BigDecimal getMultiplier() {
        return multiplier;
    }
}
