package burp;

@SuppressWarnings("unused")
public class BurpExtender implements IBurpExtender
{
    @Override
    public void registerExtenderCallbacks(IBurpExtenderCallbacks callbacks)
    {
        callbacks.setExtensionName("Content-Type Converter");
        callbacks.registerContextMenuFactory(new Menu(callbacks));
    }
}