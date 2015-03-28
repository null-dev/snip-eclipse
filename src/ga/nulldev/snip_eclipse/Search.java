package ga.nulldev.snip_eclipse;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.commons.io.IOUtils;
import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.ModifyEvent;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;

public class Search {

	protected Shell shell;
	private Text searchBox;
	
	Future<?> futureThread = null;
	String oldValue = "";
	String newValue = "";
	HashMap<String, Element> arrayMap = new HashMap<String, Element>();

	/**
	 * Launch the application.
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			Search window = new Search();
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
		shell.setSize(344, 293);
		shell.setText("SWT Application");

		searchBox = new Text(shell, SWT.BORDER);
		searchBox.setBounds(0, 0, 333, 25);

		final List list = new List(shell, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		list.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDoubleClick(MouseEvent e) {
				Element entry = arrayMap.get(list.getSelection()[0]);
				int id = Integer.parseInt(entry.getElementsByTag("a").get(1).attr("href").split("\\/")[2]);
				System.out.println("Opening: " + id + "...");
				
				//Wire the request through XML-RPC, the HTML is too hard to parse...
				XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
				try {
					config.setServerURL(new URL("http://snipplr.com/xml-rpc.php"));
				} catch (MalformedURLException e1) {
					//URL is corrupt :/
					System.out.println("URL is corrupt?");
					e1.printStackTrace();
					return;
				}
				XmlRpcClient client = new XmlRpcClient();
				client.setConfig(config);
				Object[] params = new Object[]{new Integer(id)};
				HashMap<String,?> result;
				try {
					result = (HashMap<String,?>) client.execute("snippet.get", params);
				} catch (XmlRpcException e1) {
					//Unable to contact server!
					System.out.println("Unable to contact server?");
					e1.printStackTrace();
					return;
				}
				//FINALLY SHOW CODEBOX WAHOO
				CodeBox.main(new String[]{(String)result.get("title"),(String)result.get("username"),(String)result.get("source")});
			}
		});
		list.setBounds(0, 31, 333, 229);
		final ExecutorService pool = Executors.newCachedThreadPool();
		
		final Thread searchThread = new Thread(new Runnable() {
			public void run() {
				final String internalChangeTracker = newValue;
				Document doc = null;
				InputStream in = null;
				URLConnection c = null;
				try {
					//Search for it!
					c = new URL("http://snipplr.com/search.php?q=" + newValue.replaceAll(" ", "+")).openConnection();
					c.setRequestProperty("User-Agent", "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.4; en-US; rv:1.9.2.2) Gecko/20100316 Firefox/3.6.2");
					in = c.getInputStream();
					doc = Jsoup.parse(IOUtils.toString(in));
				} catch (IOException e) {
					// TODO Auto-generated catch block
					System.out.println("Unable to fetch search results!");
					e.printStackTrace();
					return;
				} finally {
					IOUtils.closeQuietly(in);
				}
				//Filter out the results table
				Element resultsTable = null;
				try {
					resultsTable = doc.getElementsByTag("ol").get(0);
				} catch(IndexOutOfBoundsException iobe) {
					//No results :(
					Display.getDefault().syncExec(new Runnable() {
						public void run() {
							if(internalChangeTracker != newValue) {System.out.println("Change detected, terminated!"); return;}
							list.removeAll();
							list.add("No results found!");
						}
					});
					return;
				}
				Display.getDefault().syncExec(new Runnable() {
					public void run() {
						if(internalChangeTracker != newValue) {System.out.println("Change detected, terminated!"); return;}
						list.removeAll();
						arrayMap.clear();
					}
				});
				//Take the risk and assume there is only one results table!
				for(final Element entry : resultsTable.children()) {
					if(internalChangeTracker != newValue) {System.out.println("Change detected, terminated!"); return;}
					//Execute many one-liners to get various infoz
					final String language = entry.getElementsByClass("l").get(0).getElementsByTag("a").get(0).text();
					final String title = entry.getElementsByTag("h3").get(0).getElementsByTag("a").get(1).text();
					
					//JAVA AND JAVA ONLY >:)
					if(language.equals("Java")) {
						Display.getDefault().syncExec(new Runnable() {
							public void run() {
								list.add(title);
								//Put it in the arraymap
								arrayMap.put(title, entry.getElementsByTag("h3").get(0));
							}
						});
					}
				}
				Display.getDefault().syncExec(new Runnable() {
					public void run() {
						//Say no results found if no results have been added
						if(list.getItemCount() == 0) {
							if(internalChangeTracker != newValue) {System.out.println("Change detected, terminated!"); return;}
							list.removeAll();
							list.add("No results found!");
						}
					}
				});
			}
		});
		
		searchBox.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				//Grab URL and parse async
				if(!oldValue.equals(searchBox.getText()) && searchBox.getText() != "") {
					newValue = searchBox.getText();
					//Cancel previous search
					if(futureThread != null) {
						futureThread.cancel(true);
					}
					//Start searching again!
					futureThread = pool.submit(searchThread);
					oldValue = searchBox.getText();
				}
			}
		});
	}
}
