package ru.urururu.cmakeedit.lsp;

public class Server {
    public static void main(String... args) {
        LanguageServer server = new LanguageServer {} ;
Launcher<LanguageClient> launcher = 
    LSPLauncher.createServerLauncher(server,
                                     inputstream, 
                                     outputstream);
    }
}