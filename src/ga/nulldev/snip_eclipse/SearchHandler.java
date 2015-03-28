package ga.nulldev.snip_eclipse;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

public class SearchHandler extends AbstractHandler{

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		//Show the search box
		Search.main(null);
		System.out.println("Opened search popup!");
		return null;
	}

}
