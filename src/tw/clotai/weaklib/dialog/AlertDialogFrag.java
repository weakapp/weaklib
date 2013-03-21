package tw.clotai.weaklib.dialog;

import tw.clotai.weaklib.R;
import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;

import com.actionbarsherlock.app.SherlockDialogFragment;

public class AlertDialogFrag extends SherlockDialogFragment {

	private final static String TITLE 	= "tw.clotai.weaklib.dialog.AlertDialogFrag.TITLE";
	private final static String CONTENT = "tw.clotai.weaklib.dialog.AlertDialogFrag.CONTENT";
	
	public static AlertDialogFrag newInstance(String title, String content) {
		
		AlertDialogFrag c = new AlertDialogFrag();

		Bundle b = new Bundle();

		b.putString(TITLE, title);
		b.putString(CONTENT, content);

		c.setArguments(b);

		return c;
	}
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		Bundle b = getArguments();

		String title = b.getString(TITLE);
		String content = b.getString(CONTENT);

		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

		builder.setIcon(R.drawable.warning);
		builder.setTitle(title);

		builder.setMessage(content);

		builder.setPositiveButton(android.R.string.ok, null);
		return builder.create();
	}
}
