package tw.clotai.weaklib.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import com.actionbarsherlock.app.SherlockDialogFragment;

public class ConfirmDialogFrag extends SherlockDialogFragment {

	private final static String TITLE 	= "tw.clotai.weaklib.dialog.ConfirmDialogFrag.TITLE";
	private final static String CONTENT = "tw.clotai.weaklib.dialog.ConfirmDialogFrag.CONTENT";
	
	private OnConfirmListener mListener = null;

	public static ConfirmDialogFrag newInstance(String title, String content) {
		ConfirmDialogFrag c = new ConfirmDialogFrag();

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

		builder.setTitle(title);

		builder.setMessage(content);

		builder.setPositiveButton(android.R.string.ok,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						if (mListener != null) {
							mListener.confirm();
						}
					}
				});

		builder.setNegativeButton(android.R.string.cancel,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {

					}
				});

		return builder.create();
	}

	public void setOnConfirmListener(OnConfirmListener listener) {
		mListener = listener;
	}
	
	public interface OnConfirmListener {
		public void confirm();
	}
	
}
