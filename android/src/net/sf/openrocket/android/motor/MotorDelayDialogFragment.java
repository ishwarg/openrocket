package net.sf.openrocket.android.motor;

import java.util.ArrayList;

import net.sf.openrocket.R;
import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

public class MotorDelayDialogFragment extends DialogFragment 
implements View.OnClickListener, TextView.OnEditorActionListener {

	public interface OnDelaySelectedListener {
		public void onDelaySelected( double delay );
	}

	private OnDelaySelectedListener delaySelectedListener;

	public void setDelaySelectedListener(OnDelaySelectedListener delaySelectedListener) {
		this.delaySelectedListener = delaySelectedListener;
	}

	private final static String delaysArg = "delaysArg";
	
	public static MotorDelayDialogFragment newInstance( double[] delays ) {
		MotorDelayDialogFragment f = new MotorDelayDialogFragment();
		Bundle b = new Bundle();
		b.putDoubleArray(delaysArg, delays);
		f.setArguments(b);
		return f;
	}

	@Override
	public void onClick(View v) {
		String s = ((TextView)v).getText().toString();
		long value = Long.parseLong(s);
		if ( delaySelectedListener != null ) {
			delaySelectedListener.onDelaySelected(value);
		}
	}

	@Override
	public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
		if ( event.getAction() == KeyEvent.ACTION_UP && event.getKeyCode() == KeyEvent.KEYCODE_ENTER ) {
			String s = v.getText().toString();
			if ( s != null ) { // note requires ems=10
				long value = Long.parseLong(s);
				if ( delaySelectedListener != null ) {
					delaySelectedListener.onDelaySelected(value);
				}
			}
			return true;
		}
		return false;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		
		if (savedInstanceState == null ) {
			savedInstanceState = getArguments();
		}
		double[] delays = savedInstanceState.getDoubleArray(delaysArg);
		ArrayList<Long> delayList = new ArrayList<Long>(delays.length);
		for( int i =0; i< delays.length; i++ ) {
			delayList.add( Math.round(delays[i]) );
		}
		
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle("Enter or Choose Delay");
		
		LayoutInflater li = getActivity().getLayoutInflater();
		View v = li.inflate(R.layout.motor_config_delay_dialog, null);
		builder.setView(v);
		
		ArrayAdapter<Long> listAdapter = new ArrayAdapter<Long>(getActivity(),android.R.layout.simple_list_item_1,delayList) {

			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				if ( convertView == null ) {
					convertView = getActivity().getLayoutInflater().inflate( android.R.layout.simple_list_item_1, null);
				}
				TextView tv = (TextView) convertView.findViewById(android.R.id.text1);
				tv.setText( String.valueOf(getItem(position)) );
				tv.setOnClickListener( MotorDelayDialogFragment.this );
				return convertView;
			}
			
			
		};
		
		ListView lv = (ListView) v.findViewById(R.id.motor_config_delay_diag_list);
		lv.setAdapter(listAdapter);
		
		EditText et = (EditText) v.findViewById(R.id.motor_config_delay_diag_edit);
		et.setOnEditorActionListener(MotorDelayDialogFragment.this);
		return builder.create();
		
	}
	
	
	
}
