# snip-eclipse
An alternative to snipplr4e intended to allow searching of the entire Snipplr database right inside Eclipse!

##What?##
snip-eclipse is a very simple application to search for code snippts in the Snipplr database without having to open a web browser!

##Usage##
The window can be opened with "<code>CTRL + ~</code>". This will open up a simple square dialog window with a text box and a list. Type in the text box the terms you would like to search for. The list will automatically refresh every time you type a new character. Double click on an entry to open it.

##NOTE##
It currently only fetches <strong>JAVA</strong> code snippets! This can be changed via the "<code>if(language.equals("Java"))</code>" in the "Search" class.