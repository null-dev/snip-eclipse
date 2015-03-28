package ga.nulldev.snip_eclipse;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Text;

public class CodeBox {

	protected Shell shell;
	private Text txtCode;
	private static String title;
	private static String author;
	private static String code;
	/**
	 * Launch the application.
	 * @param args
	 */
	public static void main(String[] args) {
		title = args[0];
		author = args[1];
		code = args[2];
		try {
			CodeBox window = new CodeBox();
			window.open();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Open the window.
	 */
	public void open() {
		Display display = Display.getDefault();
		createContents();
		shell.open();
		shell.layout();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}

	/**
	 * Create contents of the window.
	 */
	protected void createContents() {
		shell = new Shell(Display.getDefault(), SWT.SHELL_TRIM & (~SWT.RESIZE));
		shell.setSize(450, 300);
		shell.setText("SWT Application");
		
		Label lblTitle = new Label(shell, SWT.NONE);
		lblTitle.setBounds(10, 10, 195, 15);
		lblTitle.setText(title);
		
		Label lblAuthor = new Label(shell, SWT.NONE);
		lblAuthor.setAlignment(SWT.RIGHT);
		lblAuthor.setBounds(211, 10, 229, 15);
		lblAuthor.setText(author);
		
		txtCode = new Text(shell, SWT.BORDER | SWT.READ_ONLY | SWT.H_SCROLL | SWT.V_SCROLL | SWT.CANCEL | SWT.MULTI);
		txtCode.setText(code);
		txtCode.setBounds(10, 31, 430, 229);
	}

}
